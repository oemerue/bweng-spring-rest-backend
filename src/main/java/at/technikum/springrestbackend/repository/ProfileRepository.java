package at.technikum.springrestbackend.repository;

import at.technikum.springrestbackend.entity.Profile;
import at.technikum.springrestbackend.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProfileRepository extends JpaRepository<Profile, Long> {

    // Finde Profile per User
    Optional<Profile> findByUser(User user);

    // Finde Profile per User ID
    Optional<Profile> findByUserId(Long userId);

    // Alle Profiles mit Pagination
    Page<Profile> findAll(Pageable pageable);

    // Nach Alter filtern (z.B. 18-30)
    Page<Profile> findByAgeBetween(Integer minAge, Integer maxAge, Pageable pageable);

    // Nach Location filtern
    Page<Profile> findByLocation(String location, Pageable pageable);

    // Profiles sortiert nach Likes (populärste)
    @Query(value = "SELECT p FROM Profile p ORDER BY p.likesReceived DESC")
    Page<Profile> findMostLikedProfiles(Pageable pageable);

    // Profiles sortiert nach Profile Views (meistangesehen)
    @Query(value = "SELECT p FROM Profile p ORDER BY p.profileViews DESC")
    Page<Profile> findMostViewedProfiles(Pageable pageable);
}
