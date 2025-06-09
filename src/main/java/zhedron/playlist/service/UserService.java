package zhedron.playlist.service;

import zhedron.playlist.dto.PlaylistDTO;
import zhedron.playlist.dto.UserDTO;
import zhedron.playlist.entity.User;

import java.util.List;

public interface UserService {
    User save(User user);

    User findByEmail(String email);

    UserDTO getById(long id);

    List<PlaylistDTO> getPlaylists(long userId);

    List<PlaylistDTO> getPlaylistsByArtistNameOrAlbumName(String artistName, String albumName);

    void deletePlaylist(long playlistId);

    User getCurrentUser();
}
