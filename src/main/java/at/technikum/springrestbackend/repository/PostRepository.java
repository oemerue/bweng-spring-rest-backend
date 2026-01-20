package at.technikum.springrestbackend.repository;

import at.technikum.springrestbackend.entity.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface PostRepository extends JpaRepository<Post, Long> {


    @EntityGraph(attributePaths = {"author"})
    Optional<Post> findWithAuthorById(Long id);

    @EntityGraph(attributePaths = {"author"})
    Page<Post> findByTitleContainingIgnoreCaseOrContentContainingIgnoreCase(
            String title, String content, Pageable pageable);

    @Override
    @EntityGraph(attributePaths = {"author"})
    Page<Post> findAll(Pageable pageable);

    List<Post> findByAuthorId(Long authorId);

    @Modifying
    @Transactional
    @Query("DELETE FROM Post p WHERE p.author.id = :authorId")
    void deleteByAuthorId(Long authorId);

    long countByAuthorId(Long authorId);
}