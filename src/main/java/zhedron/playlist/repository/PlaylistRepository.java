package zhedron.playlist.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import zhedron.playlist.entity.Playlist;

public interface PlaylistRepository extends JpaRepository<Playlist, Long> {
}
