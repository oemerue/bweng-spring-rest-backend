package at.technikum.springrestbackend.repository;

import at.technikum.springrestbackend.entity.Profile;
import at.technikum.springrestbackend.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProfileRepository extends JpaRepository<Profile, Long> {

    Optional<Profile> findByUser(User user);

    Optional<Profile> findByUserId(Long userId);

    Page<Profile> findAll(Pageable pageable);

    Page<Profile> findByAgeBetween(Integer minAge, Integer maxAge,
                                   Pageable pageable);

    Page<Profile> findByLocation(String location, Pageable pageable);

    @Query(value = "SELECT p FROM Profile p ORDER BY p.likesReceived DESC")
    Page<Profile> findMostLikedProfiles(Pageable pageable);

    @Query(value = "SELECT p FROM Profile p ORDER BY p.profileViews DESC")
    Page<Profile> findMostViewedProfiles(Pageable pageable);
}
