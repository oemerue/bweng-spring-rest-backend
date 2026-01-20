package at.technikum.springrestbackend.util;

import java.util.Optional;

public final class StringUtil {

    private StringUtil() {
    }

    public static Optional<String> getNonBlank(String value) {
        return Optional.ofNullable(value)
                .map(String::trim)
                .filter(s -> !s.isBlank());
    }

    public static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}