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

    // Prüfe ob User ein Profile bereits angesehen hat
    boolean existsByViewerAndProfileViewed(User viewer, Profile profileViewed);

    // Finde alle Profile Views eines Users (wer hat mein Profile angesehen)
    List<ProfileView> findByProfileViewed(Profile profileViewed);

    // Zähle Profile Views eines Profiles
    long countByProfileViewed(Profile profileViewed);

    // Finde View per Viewer und Profile
    @Query("SELECT pv FROM ProfileView pv WHERE pv.viewer.id = :viewerId AND pv.profileViewed.id = :profileId")
    Optional<ProfileView> findByViewerIdAndProfileId(@Param("viewerId") Long viewerId, @Param("profileId") Long profileId);

    // Finde alle Profile Views eines Users (wen hat dieser User angesehen)
    List<ProfileView> findByViewer(User viewer);
}
