package at.technikum.springrestbackend.repository;

import at.technikum.springrestbackend.entity.Interest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InterestRepository extends JpaRepository<Interest, Long> {

    // Finde Interest per Name
    Optional<Interest> findByName(String name);

    // Finde alle Interests (z.B. für Dropdown)
    List<Interest> findAll();

    // Prüfe ob Interest existiert
    boolean existsByName(String name);

    // Suche Interests per Name (LIKE)
    List<Interest> findByNameContainingIgnoreCase(String name);
}
