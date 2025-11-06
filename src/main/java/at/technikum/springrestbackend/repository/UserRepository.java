package at.technikum.springrestbackend.repository;

import at.technikum.springrestbackend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // Finde User per Email
    Optional<User> findByEmail(String email);

    // Finde User per Username
    Optional<User> findByUsername(String username);

    // Prüfe ob Email existiert
    boolean existsByEmail(String email);

    // Prüfe ob Username existiert
    boolean existsByUsername(String username);

    // Alle Admin User finden
    @Query("SELECT u FROM User u WHERE u.role = 'ADMIN'")
    List<User> findAllAdmins();

    // User nach Country filtern
    List<User> findByCountry(String country);
}
