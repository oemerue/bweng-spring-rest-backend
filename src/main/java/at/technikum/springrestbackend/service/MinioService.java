package at.technikum.springrestbackend.service;

import io.minio.*;
import io.minio.errors.ErrorResponseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

@Service
public class MinioService {

    private static final Logger LOG =
            LoggerFactory.getLogger(MinioService.class);

    public record DownloadedFile(byte[] bytes, String contentType) {
    }

    private final MinioClient minioClient;
    private final String bucket;

    public MinioService(MinioClient minioClient,
                        @Value("${minio.bucket}") String bucket) {
        this.minioClient = minioClient;
        this.bucket = bucket;
        ensureBucketExists();
    }

    public String upload(String objectKey, MultipartFile file) {
        try {
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucket)
                            .object(objectKey)
                            .contentType(file.getContentType())
                            .stream(file.getInputStream(),
                                    file.getSize(), -1)
                            .build());
            LOG.info("Uploaded file: {}", objectKey);
            return objectKey;
        } catch (Exception e) {
            LOG.error("Failed to upload file {}: {}",
                    objectKey, e.getMessage(), e);
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "File upload failed");
        }
    }

    public DownloadedFile download(String objectKey) {
        try (InputStream in = getObjectInputStream(objectKey)) {
            byte[] fileBytes = readInputStreamToBytes(in);
            String contentType = getContentType(objectKey);
            return new DownloadedFile(fileBytes, contentType);
        } catch (ErrorResponseException e) {
            handleErrorResponseException(e, objectKey);
        } catch (IOException e) {
            LOG.error("I/O error while downloading {}: {}",
                    objectKey, e.getMessage(), e);
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Failed to read file");
        } catch (Exception e) {
            LOG.error("Unexpected error while downloading {}: {}",
                    objectKey, e.getMessage(), e);
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Failed to download file");
        }
        return null;
    }

    private InputStream getObjectInputStream(String objectKey)
            throws Exception {
        return minioClient.getObject(
                GetObjectArgs.builder()
                        .bucket(bucket)
                        .object(objectKey)
                        .build());
    }

    private byte[] readInputStreamToBytes(InputStream in)
            throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        in.transferTo(bos);
        return bos.toByteArray();
    }

    private String getContentType(String objectKey)
            throws Exception {
        var stat = minioClient.statObject(
                StatObjectArgs.builder()
                        .bucket(bucket)
                        .object(objectKey)
                        .build());
        return stat.contentType() != null
                ? stat.contentType()
                : "application/octet-stream";
    }

    private void handleErrorResponseException(
            ErrorResponseException e,
            String objectKey) {
        int code = e.response() != null
                ? e.response().code()
                : -1;
        LOG.warn("MinIO error (objectKey={}): {} (http={})",
                objectKey, e.getMessage(), code);
        if (code == 404) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "File not found");
        } else {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "MinIO error");
        }
    }

    public void delete(String objectKey) {
        if (objectKey == null) {
            return;
        }

        try {
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(bucket)
                            .object(objectKey)
                            .build());
            LOG.info("Deleted file: {}", objectKey);
        } catch (Exception e) {
            LOG.warn("Failed to delete file {}: {}",
                    objectKey, e.getMessage());
        }
    }

    private void ensureBucketExists() {
        try {
            boolean exists = minioClient.bucketExists(
                    io.minio.BucketExistsArgs.builder()
                            .bucket(bucket)
                            .build());
            if (!exists) {
                minioClient.makeBucket(
                        io.minio.MakeBucketArgs.builder()
                                .bucket(bucket)
                                .build());
                LOG.info("Created bucket: {}", bucket);
            }
        } catch (Exception e) {
            LOG.error("MinIO initialization failed: {}",
                    e.getMessage(), e);
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "MinIO connection failed");
        }
    }
}
