package at.technikum.springrestbackend.repository;

import at.technikum.springrestbackend.entity.Message;
import at.technikum.springrestbackend.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {

    @Query("SELECT m FROM Message m WHERE "
            + "(m.sender = :user1 AND m.receiver = :user2) OR "
            + "(m.sender = :user2 AND m.receiver = :user1) "
            + "ORDER BY m.createdAt DESC")
    List<Message> findConversation(@Param("user1") User user1,
                                   @Param("user2") User user2);

    Page<Message> findByReceiver(User receiver, Pageable pageable);

    Page<Message> findBySender(User sender, Pageable pageable);

    @Query("SELECT m FROM Message m WHERE m.receiver = :user "
            + "AND m.isRead = false")
    List<Message> findUnreadMessages(@Param("user") User user);

    @Query("SELECT COUNT(m) FROM Message m WHERE m.receiver = :user "
            + "AND m.isRead = false")
    long countUnreadMessages(@Param("user") User user);

    @Query("UPDATE Message m SET m.isRead = true WHERE m.receiver = :user "
            + "AND m.sender = :sender")
    void markMessagesAsRead(@Param("user") User user,
                            @Param("sender") User sender);
}
