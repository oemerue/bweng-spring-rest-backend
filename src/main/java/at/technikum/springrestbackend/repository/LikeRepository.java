package at.technikum.springrestbackend.repository;

import at.technikum.springrestbackend.entity.Like;
import at.technikum.springrestbackend.entity.Profile;
import at.technikum.springrestbackend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LikeRepository extends JpaRepository<Like, Long> {

    // Prüfe ob User ein Profile bereits geliked hat
    boolean existsByUserIdAndProfileLiked(User userId, Profile profileLiked);

    // Finde alle Likes eines Users (was er geliked hat)
    List<Like> findByUserId(User userId);

    // Finde alle Likes eines Profiles (wer hat ihn geliked)
    List<Like> findByProfileLiked(Profile profileLiked);

    // Lösche ein Like
    void deleteByUserIdAndProfileLiked(User userId, Profile profileLiked);

    // Zähle Likes eines Profiles
    long countByProfileLiked(Profile profileLiked);

    // Finde Like per User ID und Profile ID
    @Query("SELECT l FROM Like l WHERE l.userId.id = :userId AND l.profileLiked.id = :profileId")
    Optional<Like> findByUserIdAndProfileId(@Param("userId") Long userId, @Param("profileId") Long profileId);
}
