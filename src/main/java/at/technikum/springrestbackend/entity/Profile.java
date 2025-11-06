package at.technikum.springrestbackend.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "profiles")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Profile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", unique = true, nullable = false)
    private User user;

    @NotNull
    @Min(value = 18, message = "Alter muss mindestens 18 Jahre sein")
    @Max(value = 120, message = "Alter kann nicht mehr als 120 Jahre sein")
    private Integer age;

    @Column(length = 100)
    private String location; // z.B. "Vienna, Austria" oder "Linz"

    @Column(columnDefinition = "TEXT")
    private String about; // Bio/About-Text (z.B. "Art student with a love for contemporary art...")

    @Builder.Default
    private Long profileViews = 0L; // Anzahl der Profile-Aufrufe

    @Builder.Default
    private Long likesReceived = 0L; // Anzahl Likes bekommen

    @Builder.Default
    private Double responseRate = 0.0; // Antwort-Rate in % (0.0 - 100.0)

    @Builder.Default
    private Integer interests_match = 0; // Prozentuale Übereinstimmung der Interessen

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    // One-to-Many Beziehung für Likes dieses Profils
    @OneToMany(mappedBy = "profileLiked", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<Like> likesReceived_relation = new HashSet<>();

    // One-to-Many Beziehung für Profile Views
    @OneToMany(mappedBy = "profileViewed", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<ProfileView> profileViews_relation = new HashSet<>();
}
