package zhedron.playlist.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import zhedron.playlist.entity.Playlist;
import zhedron.playlist.entity.User;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    @Query("SELECT p from User u JOIN u.playlists p WHERE u.id = :userId")
    List<Playlist> findByUserId(long userId);
}