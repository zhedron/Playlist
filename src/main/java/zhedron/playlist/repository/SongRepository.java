package zhedron.playlist.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import zhedron.playlist.entity.Song;

public interface SongRepository extends JpaRepository<Song, Long> {
    Page<Song> findAll(Pageable pageable);
}
