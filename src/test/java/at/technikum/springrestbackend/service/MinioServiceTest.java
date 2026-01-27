package at.technikum.springrestbackend.service;

import io.minio.MinioClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
@DisplayName("MinioService Validation Tests")
class MinioServiceTest {

    @Mock
    private MinioClient minioClient;

    private MinioService minioService;

    @BeforeEach
    void setUp() {
        minioService = new MinioService(minioClient, "test-bucket");
    }

    // UPLOAD TESTS

    @Test
    @DisplayName("upload: null objectKey throws BAD_REQUEST")
    void test_upload_null_objectKey() {
        MockMultipartFile file = new MockMultipartFile("file", "test.png", "image/png", "data".getBytes());
        assertThrows(Exception.class, () -> minioService.upload(null, file));
    }

    @Test
    @DisplayName("upload: empty objectKey throws BAD_REQUEST")
    void test_upload_empty_objectKey() {
        MockMultipartFile file = new MockMultipartFile("file", "test.png", "image/png", "data".getBytes());
        assertThrows(Exception.class, () -> minioService.upload("", file));
    }

    @Test
    @DisplayName("upload: null file throws BAD_REQUEST")
    void test_upload_null_file() {
        assertThrows(Exception.class, () -> minioService.upload("test.png", null));
    }

    //DOWNLOAD TESTS

    @Test
    @DisplayName("download: null objectKey throws BAD_REQUEST")
    void test_download_null_objectKey() {
        assertThrows(Exception.class, () -> minioService.download(null));
    }

    @Test
    @DisplayName("download: empty objectKey throws BAD_REQUEST")
    void test_download_empty_objectKey() {
        assertThrows(Exception.class, () -> minioService.download(""));
    }

    @Test
    @DisplayName("download: whitespace objectKey throws BAD_REQUEST")
    void test_download_whitespace_objectKey() {
        assertThrows(Exception.class, () -> minioService.download("   "));
    }

    // DELETE TESTS

    @Test
    @DisplayName("delete: null objectKey does not throw")
    void test_delete_null_objectKey() {
        assertDoesNotThrow(() -> minioService.delete(null));
    }

    @Test
    @DisplayName("delete: empty objectKey does not throw")
    void test_delete_empty_objectKey() {
        assertDoesNotThrow(() -> minioService.delete(""));
    }

    @Test
    @DisplayName("delete: whitespace objectKey does not throw")
    void test_delete_whitespace_objectKey() {
        assertDoesNotThrow(() -> minioService.delete("   "));
    }

    @Test
    @DisplayName("delete: valid objectKey does not throw")
    void test_delete_valid_objectKey() {
        assertDoesNotThrow(() -> minioService.delete("avatars/1/test.png"));
    }

    @Test
    @DisplayName("delete: valid objectKey success")
    void test_delete_valid_success() {
        assertDoesNotThrow(() -> minioService.delete("avatars/test.png"));
    }

    @Test
    @DisplayName("delete: multiple keys success")
    void test_delete_chain() {
        assertDoesNotThrow(() -> minioService.delete("a.png"));
        assertDoesNotThrow(() -> minioService.delete("b.png"));
    }

    @Test
    @DisplayName("delete: multiple calls safe")
    void test_delete_multiple_calls() {
        assertDoesNotThrow(() -> {
            minioService.delete("a.png");
            minioService.delete("b.png");
            minioService.delete("c.png");
        });
    }

    @Test
    @DisplayName("upload: null objectKey validation")
    void test_upload_null_key_validation() {
        MockMultipartFile file = new MockMultipartFile("file", "test.png", "image/png", "data".getBytes());
        assertThrows(Exception.class, () -> minioService.upload(null, file));
    }
}
