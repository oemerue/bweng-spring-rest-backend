package at.technikum.springrestbackend.service;

import at.technikum.springrestbackend.dto.CreateProfileDTO;
import at.technikum.springrestbackend.dto.ProfileDTO;
import at.technikum.springrestbackend.entity.Interest;
import at.technikum.springrestbackend.entity.Profile;
import at.technikum.springrestbackend.entity.User;
import at.technikum.springrestbackend.repository.InterestRepository;
import at.technikum.springrestbackend.repository.ProfileRepository;
import at.technikum.springrestbackend.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class ProfileServiceImpl implements ProfileService {

    private final ProfileRepository profileRepository;
    private final UserRepository userRepository;
    private final InterestRepository interestRepository;

    public ProfileServiceImpl(ProfileRepository profileRepository,
                              UserRepository userRepository,
                              InterestRepository interestRepository) {
        this.profileRepository = profileRepository;
        this.userRepository = userRepository;
        this.interestRepository = interestRepository;
    }

    @Override
    public ProfileDTO create(CreateProfileDTO dto) {
        User user = userRepository.findById(dto.getUserId())
                .orElseThrow(() -> new RuntimeException("Benutzer nicht gefunden!"));

        profileRepository.findByUserId(user.getId()).ifPresent(p -> {
            throw new RuntimeException("Profil existiert bereits für diesen Benutzer!");
        });

        Profile profile = Profile.builder()
                .user(user)
                .age(dto.getAge())
                .location(dto.getLocation())
                .about(dto.getAbout())
                .build();

        profileRepository.save(profile);

        // Interessen hinzufügen (über Interest IDs)
        assignInterestsToUser(user, dto.getInterestIds());
        userRepository.save(user);

        return mapToDTO(profile);
    }

    @Override
    public ProfileDTO update(Long userId, CreateProfileDTO dto) {
        Profile profile = profileRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Profil nicht gefunden!"));

        profile.setAge(dto.getAge());
        profile.setLocation(dto.getLocation());
        profile.setAbout(dto.getAbout());
        profileRepository.save(profile);

        // Interessen aktualisieren
        User user = profile.getUser();
        assignInterestsToUser(user, dto.getInterestIds());
        userRepository.save(user);

        return mapToDTO(profile);
    }

    @Override
    public ProfileDTO getByUserId(Long userId) {
        Profile profile = profileRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Profil nicht gefunden!"));
        return mapToDTO(profile);
    }

    @Override
    public List<ProfileDTO> getAllProfiles(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Profile> pageResult = profileRepository.findAll(pageable);
        return pageResult.stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<ProfileDTO> getProfilesByAgeRange(Integer minAge, Integer maxAge,
                                                  int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Profile> pageResult = profileRepository.findByAgeBetween(minAge, maxAge, pageable);
        return pageResult.stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<ProfileDTO> getProfilesByLocation(String location, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Profile> pageResult = profileRepository.findByLocation(location, pageable);
        return pageResult.stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<ProfileDTO> getMostLikedProfiles(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Profile> pageResult = profileRepository.findMostLikedProfiles(pageable);
        return pageResult.stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<ProfileDTO> getMostViewedProfiles(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Profile> pageResult = profileRepository.findMostViewedProfiles(pageable);
        return pageResult.stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    /* -------------------- HILFSMETHODEN -------------------- */

    /**
     * Mapped die Interests des Users basierend auf einer Liste von Interest-IDs.
     * Alte Interests werden ersetzt.
     */
    private void assignInterestsToUser(User user, Set<Long> interestIds) {
        if (interestIds == null) {
            return;
        }

        Set<Interest> interests = interestIds.stream()
                .map(id -> interestRepository.findById(id)
                        .orElseThrow(() -> new RuntimeException(
                                "Interest mit ID " + id + " nicht gefunden!"
                        )))
                .collect(Collectors.toSet());

        user.setInterests(interests);
    }

    /**
     * Wandelt ein Profile-Objekt in ein ProfileDTO um.
     */
    private ProfileDTO mapToDTO(Profile p) {
        Set<String> interests = p.getUser().getInterests().stream()
                .map(Interest::getName)
                .collect(Collectors.toSet());

        // Щоб не мати дуже довгі рядки, за бажання — тимчасова змінна:
        String pictureUrl = p.getUser().getProfilePictureUrl();

        return ProfileDTO.builder()
                .id(p.getId())
                .userId(p.getUser().getId())
                .username(p.getUser().getUsername())
                .profilePictureUrl(pictureUrl)
                .age(p.getAge())
                .location(p.getLocation())
                .about(p.getAbout())
                .interests(interests)
                .likesReceived(p.getLikesReceived())
                .profileViews(p.getProfileViews())
                .responseRate(p.getResponseRate())
                .createdAt(p.getCreatedAt())
                .updatedAt(p.getUpdatedAt())
                .build();
    }
}
