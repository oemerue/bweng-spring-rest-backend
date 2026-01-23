package at.technikum.springrestbackend.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.server.ResponseStatusException;

import static org.junit.jupiter.api.Assertions.*;

class ImageUtilTest {

    // ==================== isAllowedImageType ====================

    @ParameterizedTest
    @ValueSource(strings = {"image/png", "image/jpeg", "image/webp", "IMAGE/PNG", "Image/Jpeg"})
    void isAllowedImageType_validTypes_returnsTrue(String contentType) {
        assertTrue(ImageUtil.isAllowedImageType(contentType));
    }

    @ParameterizedTest
    @ValueSource(strings = {"application/pdf", "text/plain", "image/gif", "image/bmp", "video/mp4"})
    void isAllowedImageType_invalidTypes_returnsFalse(String contentType) {
        assertFalse(ImageUtil.isAllowedImageType(contentType));
    }

    @Test
    void isAllowedImageType_null_returnsFalse() {
        assertFalse(ImageUtil.isAllowedImageType(null));
    }

    @Test
    void isAllowedImageType_empty_returnsFalse() {
        assertFalse(ImageUtil.isAllowedImageType(""));
    }

    // ==================== validateImageFile ====================

    @Test
    void validateImageFile_validPng_noException() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "test.png", "image/png", new byte[]{1, 2, 3}
        );

        assertDoesNotThrow(() -> ImageUtil.validateImageFile(file));
    }

    @Test
    void validateImageFile_validJpeg_noException() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "test.jpg", "image/jpeg", new byte[]{1, 2, 3}
        );

        assertDoesNotThrow(() -> ImageUtil.validateImageFile(file));
    }

    @Test
    void validateImageFile_nullFile_throws400() {
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> ImageUtil.validateImageFile(null));
        assertEquals(400, ex.getStatusCode().value());
    }

    @Test
    void validateImageFile_emptyFile_throws400() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "empty.png", "image/png", new byte[]{}
        );

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> ImageUtil.validateImageFile(file));
        assertEquals(400, ex.getStatusCode().value());
    }

    @Test
    void validateImageFile_invalidType_throws400() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "doc.pdf", "application/pdf", new byte[]{1, 2, 3}
        );

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> ImageUtil.validateImageFile(file));
        assertEquals(400, ex.getStatusCode().value());
        assertTrue(ex.getReason().contains("PNG, JPEG, WEBP"));
    }

    // ==================== getExtension ====================

    @Test
    void getExtension_png_returnsPng() {
        assertEquals(".png", ImageUtil.getExtension("image/png"));
    }

    @Test
    void getExtension_jpeg_returnsJpg() {
        assertEquals(".jpg", ImageUtil.getExtension("image/jpeg"));
    }

    @Test
    void getExtension_webp_returnsWebp() {
        assertEquals(".webp", ImageUtil.getExtension("image/webp"));
    }

    @Test
    void getExtension_unknown_returnsJpg() {
        assertEquals(".jpg", ImageUtil.getExtension("image/unknown"));
    }

    @Test
    void getExtension_null_returnsJpg() {
        assertEquals(".jpg", ImageUtil.getExtension(null));
    }

    // ==================== buildFileUrl ====================

    @Test
    void buildFileUrl_validKey_returnsFullUrl() {
        String result = ImageUtil.buildFileUrl("avatars/1/test.png");

        assertEquals("/api/files/avatars/1/test.png", result);
    }

    @Test
    void buildFileUrl_nullKey_returnsNull() {
        assertNull(ImageUtil.buildFileUrl(null));
    }

    @Test
    void buildFileUrl_withDefault_validKey_returnsFullUrl() {
        String result = ImageUtil.buildFileUrl("avatars/1/test.png", "default.png");

        assertEquals("/api/files/avatars/1/test.png", result);
    }

    @Test
    void buildFileUrl_withDefault_nullKey_returnsDefault() {
        String result = ImageUtil.buildFileUrl(null, "https://example.com/default.png");

        assertEquals("https://example.com/default.png", result);
    }
}