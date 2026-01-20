package at.technikum.springrestbackend.constant;

import java.util.Arrays;
import java.util.Collections;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

public final class AppConstants {

    private AppConstants() {
    }

    public static final String AVATARS_PATH = "avatars/";
    public static final String POST_IMAGES_PATH =
            "post-images/";
    public static final String FILE_API_PREFIX = "/api/files/";

    public static final String DEFAULT_AVATAR_URL =
            "https://placehold.co/256x256?text=Avatar";

    public static final Set<String> VALID_ISO_COUNTRIES =
            Collections.unmodifiableSet(
                    Arrays.stream(Locale.getISOCountries())
                            .collect(Collectors.toSet()));

    public static final Set<String> ALLOWED_IMAGE_TYPES =
            Set.of(
                    "image/png",
                    "image/jpeg",
                    "image/webp"
            );

    public static final Set<String>
            ALLOWED_POST_SORT_FIELDS = Set.of(
            "createdAt", "updatedAt", "title"
    );

    public static final Set<String>
            ALLOWED_PROFILE_SORT_FIELDS = Set.of(
            "createdAt", "updatedAt", "username"
    );

    public static final String DEFAULT_SORT_FIELD =
            "createdAt";
}
