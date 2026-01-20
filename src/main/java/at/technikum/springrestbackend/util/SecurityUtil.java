package at.technikum.springrestbackend.util;

import at.technikum.springrestbackend.entity.Profile;
import at.technikum.springrestbackend.entity.Role;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

public final class SecurityUtil {

    private SecurityUtil() {
    }

    public static Profile currentProfileOrThrow() {
        return currentProfile()
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED,
                        "Not authenticated"));
    }

    public static Optional<Profile> currentProfile() {
        Authentication auth =
                SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof Profile profile) {
            return Optional.of(profile);
        }
        return Optional.empty();
    }

    public static boolean isAdmin(Profile profile) {
        return profile != null && profile.getRole() == Role.ADMIN;
    }

    public static boolean isCurrentUserAdmin() {
        return currentProfile()
                .map(SecurityUtil::isAdmin)
                .orElse(false);
    }

    public static void ensureOwnerOrAdmin(Long resourceOwnerId) {
        Profile me = currentProfileOrThrow();
        boolean isOwner = me.getId().equals(resourceOwnerId);
        if (!isOwner && !isAdmin(me)) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Access denied");
        }
    }
}
