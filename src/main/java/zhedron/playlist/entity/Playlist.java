package zhedron.playlist.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "playlists")
@Data
public class Playlist {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @ManyToMany
    private Set<Song> songs = new HashSet<>();

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    private long duration;

    private boolean isPublic;

    private int counter;

    private LocalDateTime createdAt;
}
