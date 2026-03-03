package zhedron.playlist.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import zhedron.playlist.entity.Playlist;
import zhedron.playlist.entity.User;

import java.util.List;

public interface PlaylistRepository extends JpaRepository<Playlist, Long> {
    @Query("SELECT p from Playlist p JOIN p.songs s WHERE s.id = :songId")
    Playlist findByIdAndSongId(long id, long songId);

    List<Playlist> findAllBySongsId(long songsId);

    Playlist findByUser(User user);

    @Query("SELECT p FROM Playlist p JOIN p.songs s JOIN p.user u WHERE (s.artistName = :artistName OR s.albumName = :albumName) AND u.id = :userId")
    List<Playlist> findByArtistNameOrAlbumNameAndUserId(String artistName, String albumName, long userId);

    @Query("SELECT COUNT(p) > 0 FROM Playlist p JOIN p.songs s JOIN p.user u  WHERE (s.artistName = :artistName OR s.albumName = :albumName) AND u.id = :userId")
    boolean existsPlaylistByArtistNameOrAlbumNameAndUserId(String artistName, String albumName, long userId);
}
