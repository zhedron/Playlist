package zhedron.playlist.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import zhedron.playlist.entity.Song;

public interface SongRepository extends JpaRepository<Song, Long> {
}
