package at.technikum.springrestbackend.repository;

import at.technikum.springrestbackend.entity.Profile;
import at.technikum.springrestbackend.entity.ProfileView;
import at.technikum.springrestbackend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProfileViewRepository extends JpaRepository<ProfileView, Long> {

    boolean existsByViewerAndProfileViewed(User viewer, Profile profileViewed);

    List<ProfileView> findByProfileViewed(Profile profileViewed);

    long countByProfileViewed(Profile profileViewed);

    @Query("SELECT pv FROM ProfileView pv WHERE pv.viewer.id = :viewerId "
            + "AND pv.profileViewed.id = :profileId")
    Optional<ProfileView> findByViewerIdAndProfileId(
            @Param("viewerId") Long viewerId,
            @Param("profileId") Long profileId);

    List<ProfileView> findByViewer(User viewer);
}
