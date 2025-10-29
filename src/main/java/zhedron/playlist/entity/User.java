package zhedron.playlist.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import zhedron.playlist.enums.Provider;
import zhedron.playlist.enums.Role;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "users")
@Data
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @NotBlank(message = "Name must not be empty")
    @NotNull(message = "Name must not be null")
    private String name;

    private String about;

    @Email(message = "Write your email")
    @NotNull(message = "Email must not be null")
    @NotBlank(message = "Email must not be empty")
    private String email;

    private String password;

    private LocalDateTime createdAt;

    @Enumerated(EnumType.STRING)
    private Role role;

    @OneToMany(mappedBy = "user")
    private List<Playlist> playlists;

    private boolean blocked;

    @Enumerated(EnumType.STRING)
    private Provider provider;
}
