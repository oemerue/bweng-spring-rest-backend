package at.technikum.springrestbackend.service;

import at.technikum.springrestbackend.constant.AppConstants;
import at.technikum.springrestbackend.dto.ProfileDTO;
import at.technikum.springrestbackend.dto.ProfileUpdateDTO;
import at.technikum.springrestbackend.dto.PublicProfileDTO;
import at.technikum.springrestbackend.entity.Profile;
import at.technikum.springrestbackend.entity.Role;
import at.technikum.springrestbackend.repository.PostRepository;
import at.technikum.springrestbackend.repository.ProfileRepository;
import at.technikum.springrestbackend.util.ImageUtil;
import at.technikum.springrestbackend.util.SecurityUtil;
import at.technikum.springrestbackend.util.StringUtil;
import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ProfileService {

    private final ProfileRepository profileRepository;
    private final PostRepository postRepository;
    private final MinioService minioService;

    public ProfileService(ProfileRepository profileRepository,
                          PostRepository postRepository,
                          MinioService minioService) {
        this.profileRepository = profileRepository;
        this.postRepository = postRepository;
        this.minioService = minioService;
    }

    public List<PublicProfileDTO> getAllProfilesPublic() {
        return profileRepository.findAll().stream()
                .map(this::toPublicDTO)
                .toList();
    }

    public PublicProfileDTO getProfilePublic(Long id) {
        return toPublicDTO(findProfileOrThrow(id));
    }

    public ProfileDTO getMyProfile() {
        return toDTO(SecurityUtil.currentProfileOrThrow());
    }

    public ProfileDTO updateMyProfile(ProfileUpdateDTO dto) {
        Profile me = SecurityUtil.currentProfileOrThrow();
        return toDTO(updateProfile(me, dto));
    }

    public ProfileDTO uploadAvatar(MultipartFile file) {
        Profile me = SecurityUtil.currentProfileOrThrow();
        return toDTO(uploadAvatarForProfile(me, file));
    }

    @Transactional
    public void deleteMyProfile() {
        Profile me = SecurityUtil.currentProfileOrThrow();
        deleteProfileCompletely(me);
    }

    public Page<ProfileDTO> getAllProfilesForAdmin(Pageable pageable) {
        return profileRepository.findAll(pageable).map(this::toDTO);
    }

    public ProfileDTO getProfileForAdmin(Long id) {
        return toDTO(findProfileOrThrow(id));
    }

    public ProfileDTO updateProfileAsAdmin(Long id, ProfileUpdateDTO dto) {
        Profile profile = findProfileOrThrow(id);
        return toDTO(updateProfile(profile, dto));
    }

    public ProfileDTO changeRole(Long id, Role newRole) {
        Profile profile = findProfileOrThrow(id);
        Profile currentAdmin = SecurityUtil.currentProfileOrThrow();

        if (profile.getId().equals(currentAdmin.getId())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Cannot change own role"
            );
        }

        profile.setRole(newRole);
        return toDTO(profileRepository.save(profile));
    }

    public ProfileDTO setEnabledAsAdmin(Long id, boolean enabled) {
        Profile profile = findProfileOrThrow(id);
        Profile currentAdmin = SecurityUtil.currentProfileOrThrow();

        if (profile.getId().equals(currentAdmin.getId())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Cannot change own enabled state"
            );
        }

        profile.setEnabled(enabled);
        return toDTO(profileRepository.save(profile));
    }

    public ProfileDTO uploadAvatarAsAdmin(Long id, MultipartFile file) {
        Profile profile = findProfileOrThrow(id);
        return toDTO(uploadAvatarForProfile(profile, file));
    }

    @Transactional
    public void deleteProfileAsAdmin(Long id) {
        Profile profile = findProfileOrThrow(id);
        Profile currentAdmin = SecurityUtil.currentProfileOrThrow();

        if (profile.getId().equals(currentAdmin.getId())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Cannot delete own account via admin endpoint"
            );
        }

        deleteProfileCompletely(profile);
    }

    private Profile findProfileOrThrow(Long id) {
        return profileRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Profile not found with id: " + id
                ));
    }

    private Profile updateProfile(Profile profile, ProfileUpdateDTO dto) {
        updateProfileFields(profile, dto);
        return profileRepository.save(profile);
    }

    private void updateProfileFields(Profile profile,
                                     ProfileUpdateDTO dto) {
        StringUtil.getNonBlank(dto.getUsername()).ifPresent(username -> {
            boolean changed = !username.equals(profile.getUsernameDisplay());
            if (changed && profileRepository.existsByUsername(username)) {
                throw new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "Username already taken"
                );
            }
            profile.setUsernameDisplay(username);
        });

        if (dto.getBio() != null) {
            profile.setBio(StringUtil.getNonBlank(dto.getBio()).orElse(null));
        }

        if (dto.getAge() != null) {
            profile.setAge(dto.getAge());
        }

        if (dto.getCity() != null) {
            profile.setCity(StringUtil.getNonBlank(dto.getCity()).orElse(null));
        }

        if (dto.getGender() != null) {
            String normalizedGender = StringUtil.getNonBlank(dto.getGender())
                    .map(String::toUpperCase)
                    .orElse(null);
            profile.setGender(normalizedGender);
        }
    }

    private Profile uploadAvatarForProfile(Profile profile, MultipartFile file) {
        ImageUtil.validateImageFile(file);

        deleteObjectIfPresent(profile.getAvatarObjectKey());

        String objectKey = AppConstants.AVATARS_PATH
                + profile.getId()
                + "/"
                + UUID.randomUUID()
                + ImageUtil.getExtension(file.getContentType());

        minioService.upload(objectKey, file);

        profile.setAvatarObjectKey(objectKey);
        return profileRepository.save(profile);
    }

    private void deleteProfileCompletely(Profile profile) {
        postRepository.findByAuthorId(profile.getId())
                .forEach(post -> deleteObjectIfPresent(post.getImageObjectKey()));

        postRepository.deleteByAuthorId(profile.getId());
        deleteObjectIfPresent(profile.getAvatarObjectKey());
        profileRepository.delete(profile);
    }

    private void deleteObjectIfPresent(String objectKey) {
        StringUtil.getNonBlank(objectKey).ifPresent(minioService::delete);
    }

    private PublicProfileDTO toPublicDTO(Profile profile) {
        String avatarUrl = ImageUtil.buildFileUrl(
                profile.getAvatarObjectKey(),
                AppConstants.DEFAULT_AVATAR_URL
        );

        return new PublicProfileDTO(
                profile.getId(),
                profile.getUsernameDisplay(),
                profile.getBio(),
                profile.getAge(),
                profile.getCity(),
                profile.getCountry(),
                profile.getGender(),
                avatarUrl,
                profile.getCreatedAt()
        );
    }

    private ProfileDTO toDTO(Profile profile) {
        String avatarUrl = ImageUtil.buildFileUrl(
                profile.getAvatarObjectKey(),
                AppConstants.DEFAULT_AVATAR_URL
        );

        return new ProfileDTO(
                profile.getId(),
                profile.getUsernameDisplay(),
                profile.getEmail(),
                profile.getCountry(),
                profile.getBio(),
                profile.getAge(),
                profile.getCity(),
                profile.getGender(),
                avatarUrl,
                profile.getRole().name(),
                profile.getCreatedAt(),
                profile.getUpdatedAt()
        );
    }
}
