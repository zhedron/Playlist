package zhedron.playlist.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "songs")
@Data
public class Song {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @NotBlank(message = "Name must not be empty")
    @NotNull(message = "Name must not be null")
    private String artistName;

    @NotBlank(message = "Album must not be empty")
    @NotNull(message = "Album must not be null")
    private String albumName;

    private long views;

    @ManyToOne
    @JoinColumn(name = "creator_id")
    private User creator;

    private LocalDateTime createdAt;

    private String contentType;

    private String fileName;

    private int duration;
}
