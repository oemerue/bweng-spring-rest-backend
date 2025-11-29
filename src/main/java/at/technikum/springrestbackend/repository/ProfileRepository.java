package at.technikum.springrestbackend.repository;

import at.technikum.springrestbackend.entity.Profile;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface ProfileRepository extends JpaRepository<Profile, Long> {

    boolean existsByUsername(String username);
    Optional<Profile> findByUsername(String username);
}
