package at.technikum.springrestbackend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "profile_views", uniqueConstraints = @UniqueConstraint(columnNames = {"viewer_id", "profile_viewed_id"}))
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProfileView {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "viewer_id", nullable = false)
    private User viewer; // User der das Profil anschaut

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "profile_viewed_id", nullable = false)
    private Profile profileViewed; // Profil das angeschaut wurde

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
