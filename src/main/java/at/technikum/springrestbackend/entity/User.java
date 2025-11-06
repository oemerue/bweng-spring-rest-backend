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
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    @Email(message = "Email sollte gültig sein")
    private String email;

    @Column(unique = true, nullable = false)
    @Size(min = 5, message = "Username muss mindestens 5 Zeichen lang sein")
    private String username;

    @Column(nullable = false)
    @Size(min = 8, message = "Password muss mindestens 8 Zeichen lang sein, mit Groß-, Kleinbuchstaben und Zahlen")
    private String password; // BCrypt hashed

    @Column(length = 2)
    @Pattern(regexp = "^[A-Z]{2}$", message = "Country code sollte 2 Großbuchstaben sein (z.B. AT, DE)")
    private String country;

    @Column(name = "profile_picture_url")
    private String profilePictureUrl; // Pfad zum Bild in MinIO oder Placeholder

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private Role role = Role.USER; // USER oder ADMIN

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    // One-to-One Beziehung zu Profile
    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private Profile profile;

    // Many-to-Many Beziehung zu Interests
    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch = FetchType.EAGER)
    @JoinTable(
            name = "user_interests",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "interest_id")
    )
    @Builder.Default
    private Set<Interest> interests = new HashSet<>();

    // One-to-Many Beziehung für Likes die dieser User gegeben hat
    @OneToMany(mappedBy = "userId", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<Like> likesSent = new HashSet<>();

    // One-to-Many Beziehung für Messages (gesendet)
    @OneToMany(mappedBy = "sender", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<Message> messagesSent = new HashSet<>();

    // One-to-Many Beziehung für Messages (empfangen)
    @OneToMany(mappedBy = "receiver", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<Message> messagesReceived = new HashSet<>();

    // One-to-Many Beziehung für Profile Views
    @OneToMany(mappedBy = "viewer", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<ProfileView> profileViewsSent = new HashSet<>();

    public enum Role {
        USER, ADMIN
    }
}
