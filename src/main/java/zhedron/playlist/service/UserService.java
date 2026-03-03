package zhedron.playlist.service;

import org.springframework.web.multipart.MultipartFile;
import zhedron.playlist.dto.PlaylistDTO;
import zhedron.playlist.dto.request.UserRequest;
import zhedron.playlist.dto.request.UserUpdateRequest;
import zhedron.playlist.entity.User;
import zhedron.playlist.enums.Role;

import java.io.IOException;
import java.util.List;

public interface UserService {
    User save(UserRequest requestUser, MultipartFile profilePicture) throws IOException;

    User findByEmail(String email);

    User getById(long id);

    List<PlaylistDTO> getPlaylists(long userId);

    void deleteSongFromPlaylist(long playlistId,  long songId);

    User getCurrentUser();

    void blockUser(long userId);

    byte[] getProfilePicture(long id);

    void updateUser(UserUpdateRequest updateUser, long userId) throws Exception;

    void changeRole(Role role, long userId);
}
