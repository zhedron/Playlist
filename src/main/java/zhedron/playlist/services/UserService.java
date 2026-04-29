package zhedron.playlist.services;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;
import zhedron.playlist.dto.PlaylistDTO;
import zhedron.playlist.dto.request.UserRequest;
import zhedron.playlist.dto.request.UserUpdateRequest;
import zhedron.playlist.entity.User;
import zhedron.playlist.enums.Role;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.List;

public interface UserService {
    User save(UserRequest requestUser);

    User findByEmail(String email);

    User getById(long id);

    List<PlaylistDTO> getPlaylists(long userId);

    void deleteSongFromPlaylist(long playlistId,  long songId);

    User getCurrentUser();

    void blockUser(long userId);

    Resource getProfilePicture(long id) throws MalformedURLException;

    void updateUser(UserUpdateRequest updateUser, long userId) throws Exception;

    void changeRole(Role role, long userId);

    void uploadAvatar(MultipartFile file) throws IOException;
}
