package zhedron.playlist.entity;

import jakarta.persistence.*;
import lombok.Data;
import zhedron.playlist.enums.Type;

import java.time.LocalDateTime;

@Entity
@Table(name = "songs")
@Data
public class Song {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(nullable = false)
    private String artistName;

    @Column(nullable = false)
    private String albumName;

    private long views;

    @ManyToOne
    @JoinColumn(name = "creator_id")
    private User creator;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    private String contentType;

    private String fileName;

    private int duration;

    @Enumerated(EnumType.STRING)
    private Type type;
}
