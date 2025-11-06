package at.technikum.springrestbackend.service;

import at.technikum.springrestbackend.dto.LikeRequestDTO;
import at.technikum.springrestbackend.dto.ProfileDTO;
import at.technikum.springrestbackend.entity.Like;
import at.technikum.springrestbackend.entity.Profile;
import at.technikum.springrestbackend.entity.User;
import at.technikum.springrestbackend.repository.LikeRepository;
import at.technikum.springrestbackend.repository.ProfileRepository;
import at.technikum.springrestbackend.repository.UserRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class MatchServiceImpl implements MatchService {

    private final LikeRepository likeRepository;
    private final ProfileRepository profileRepository;
    private final UserRepository userRepository;

    public MatchServiceImpl(LikeRepository likeRepository,
                            ProfileRepository profileRepository,
                            UserRepository userRepository) {
        this.likeRepository = likeRepository;
        this.profileRepository = profileRepository;
        this.userRepository = userRepository;
    }

    @Override
    public void like(LikeRequestDTO req) {
        User user = userRepository.findById(req.getUserId())
                .orElseThrow(() -> new RuntimeException("Benutzer nicht gefunden"));

        Profile targetProfile = profileRepository.findById(req.getProfileId())
                .orElseThrow(() -> new RuntimeException("Profil nicht gefunden"));

        // Doppel-Like vermeiden (Repo erwartet User & Profile, nicht Longs)
        if (likeRepository.existsByUserIdAndProfileLiked(user, targetProfile)) return;

        Like like = Like.builder()
                .userId(user)                // Feldname deiner Like-Entity
                .profileLiked(targetProfile) // Feldname deiner Like-Entity
                .build();

        likeRepository.save(like);
    }

    @Override
    public List<ProfileDTO> myMatches(Long userId) {
        // eigenes Profil
        Profile myProfile = profileRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Eigenes Profil nicht gefunden"));

        // Likes, die ich vergeben habe
        User me = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Benutzer nicht gefunden"));
        var iLiked = likeRepository.findByUserId(me);

        // Likes auf mich
        var likedMe = likeRepository.findByProfileLiked(myProfile);

        // gegenseitig
        Set<Long> iLikedUserIds = iLiked.stream()
                .map(l -> l.getProfileLiked().getUser().getId())
                .collect(Collectors.toSet());

        Set<Long> likedMeUserIds = likedMe.stream()
                .map(l -> l.getUserId().getId())
                .collect(Collectors.toSet());

        iLikedUserIds.retainAll(likedMeUserIds);

        return iLikedUserIds.stream()
                .map(uid -> profileRepository.findByUserId(uid)
                        .orElseThrow(() -> new RuntimeException("Profil nicht gefunden")))
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<ProfileDTO> recommend(Long userId, int page, int size) {
        User me = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Benutzer nicht gefunden"));

        // bereits gelikte Profile
        Set<Long> alreadyLikedProfileIds = likeRepository.findByUserId(me).stream()
                .map(l -> l.getProfileLiked().getId())
                .collect(Collectors.toSet());

        return profileRepository.findMostLikedProfiles(PageRequest.of(page, size))
                .stream()
                .filter(p -> !p.getUser().getId().equals(userId))
                .filter(p -> !alreadyLikedProfileIds.contains(p.getId()))
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    private ProfileDTO mapToDTO(Profile p) {
        return ProfileDTO.builder()
                .id(p.getId())
                .userId(p.getUser().getId())
                .username(p.getUser().getUsername())
                .profilePictureUrl(p.getUser().getProfilePictureUrl())
                .age(p.getAge())
                .location(p.getLocation())
                .about(p.getAbout())
                .likesReceived(p.getLikesReceived())
                .profileViews(p.getProfileViews())
                .responseRate(p.getResponseRate())
                .createdAt(p.getCreatedAt())
                .updatedAt(p.getUpdatedAt())
                .build();
    }
}
