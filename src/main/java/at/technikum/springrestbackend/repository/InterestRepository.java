package at.technikum.springrestbackend.repository;

import at.technikum.springrestbackend.entity.Interest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InterestRepository extends JpaRepository<Interest, Long> {

    Optional<Interest> findByName(String name);

    List<Interest> findAll();

    boolean existsByName(String name);

    List<Interest> findByNameContainingIgnoreCase(String name);
}
