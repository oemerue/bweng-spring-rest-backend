package at.technikum.springrestbackend.controller;

import at.technikum.springrestbackend.service.MinioService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/api/files")
public class FileController {

    private final MinioService minioService;

    public FileController(MinioService minioService) {
        this.minioService = minioService;
    }

    @GetMapping("/**")
    public ResponseEntity<byte[]> getFile(HttpServletRequest request) {
        String uri = request.getRequestURI(); // /api/files/<objectKey>
        String prefix = "/api/files/";
        if (!uri.startsWith(prefix) || uri.length() <= prefix.length()) {
            return ResponseEntity.badRequest().build();
        }

        String rawKey = uri.substring(prefix.length());
        // URL decode and sanitize
        String objectKey = URLDecoder.decode(rawKey, StandardCharsets.UTF_8);
        objectKey = objectKey.trim();

        // basic sanitization: forbid path traversal and empty keys
        if (objectKey.isEmpty() || objectKey.contains("..") || objectKey.startsWith("/")) {
            return ResponseEntity.badRequest().build();
        }

        MinioService.DownloadedFile file = minioService.download(objectKey);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, file.contentType())
                .body(file.bytes());
    }
}