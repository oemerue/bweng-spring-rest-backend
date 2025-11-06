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

    boolean existsByUserIdAndProfileLiked(User userId, Profile profileLiked);

    List<Like> findByUserId(User userId);

    List<Like> findByProfileLiked(Profile profileLiked);

    void deleteByUserIdAndProfileLiked(User userId, Profile profileLiked);

    long countByProfileLiked(Profile profileLiked);

    @Query("SELECT l FROM Like l WHERE l.userId.id = :userId "
            + "AND l.profileLiked.id = :profileId")
    Optional<Like> findByUserIdAndProfileId(@Param("userId") Long userId,
                                            @Param("profileId") Long profileId);
}
