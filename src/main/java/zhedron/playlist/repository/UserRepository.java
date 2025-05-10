package zhedron.playlist.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import zhedron.playlist.entity.Playlist;
import zhedron.playlist.entity.User;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    @Query("SELECT p from User u JOIN u.playlists p WHERE u.id = :userId")
    List<Playlist> findByUserId(long userId);

    @Query("SELECT p from User u JOIN u.playlists p WHERE u.id = :userId AND (p.artistName = :artistName OR p.albumName = :albumName)")
    List<Playlist> findPlaylistsByUserIdAndArtistNameOrAlbumName(@Param("artistName") String artistName, @Param("albumName") String albumName, @Param("userId") long userId);

    @Query("SELECT COUNT(u) > 0 from User u JOIN u.playlists p WHERE u.id = :userId AND (p.artistName = :artistName OR p.albumName = :albumName)")
    boolean existsPlaylistsByArtistNameOrAlbumName(@Param("artistName") String artistName, @Param("albumName") String albumName, @Param("userId") long userId);
}
