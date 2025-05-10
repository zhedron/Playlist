package zhedron.playlist.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "playlists")
@Data
public class Playlist {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private String artistName;

    private String albumName;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
}
