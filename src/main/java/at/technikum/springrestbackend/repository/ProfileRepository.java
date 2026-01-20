package at.technikum.springrestbackend.repository;

import at.technikum.springrestbackend.entity.Profile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProfileRepository extends JpaRepository<Profile, Long> {

    Optional<Profile> findByEmail(String email);

    Optional<Profile> findByEmailIgnoreCase(String email);

    Optional<Profile> findByUsername(String username);

    boolean existsByEmail(String email);

    boolean existsByUsername(String username);
}
