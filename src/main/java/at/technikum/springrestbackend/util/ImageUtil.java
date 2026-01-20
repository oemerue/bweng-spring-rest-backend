package at.technikum.springrestbackend.util;

import at.technikum.springrestbackend.constant.AppConstants;
import org.springframework.http.HttpStatus;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.util.Locale;

public final class ImageUtil {

    private ImageUtil() {
    }

    public static boolean isAllowedImageType(String contentType) {
        return contentType != null
                && AppConstants.ALLOWED_IMAGE_TYPES.contains(contentType.toLowerCase(Locale.ROOT));
    }

    public static void validateImageFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "File is required");
        }
        if (!isAllowedImageType(file.getContentType())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Only PNG, JPEG, WEBP images are allowed");
        }
    }

    public static String getExtension(String contentType) {
        if (contentType == null) return ".jpg";
        return switch (contentType.toLowerCase(Locale.ROOT)) {
            case "image/png" -> ".png";
            case "image/webp" -> ".webp";
            default -> ".jpg";
        };
    }

    public static String buildFileUrl(String objectKey) {
        return objectKey != null ? AppConstants.FILE_API_PREFIX + objectKey : null;
    }

    public static String buildFileUrl(String objectKey, String defaultUrl) {
        return objectKey != null ? AppConstants.FILE_API_PREFIX + objectKey : defaultUrl;
    }
}