package zhedron.playlist.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "songs")
@Data
public class Song {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private String artistName;

    private String albumName;

    private long views;

    @ManyToOne
    @JoinColumn(name = "creator_id")
    private User creator;
}
