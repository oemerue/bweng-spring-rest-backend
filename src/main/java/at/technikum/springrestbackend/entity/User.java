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

    @NotBlank
    @Email(message = "Email sollte gültig sein")
    @Column(unique = true, nullable = false, length = 100)
    private String email;

    @NotBlank
    @Size(min = 5, message = "Username muss mindestens 5 Zeichen sein")
    @Column(unique = true, nullable = false, length = 50)
    private String username;

    @NotBlank
    @Size(min = 8, message = "Password muss mindestens 8 Zeichen lang sein")
    @Column(nullable = false)
    private String password;

    @Pattern(regexp = "^[A-Z]{2}$",
            message = "Country code sollte 2 Großbuchstaben sein")
    @Column(length = 2)
    private String country;

    @Column(length = 255)
    private String profilePictureUrl;

    @Column(nullable = false, length = 20)
    @Builder.Default
    private String role = "USER";

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;
}
