package at.technikum.springrestbackend.repository;

import at.technikum.springrestbackend.entity.Profile;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProfileRepository extends JpaRepository<Profile, Long> {
    boolean existsByUsername(String username);
}
