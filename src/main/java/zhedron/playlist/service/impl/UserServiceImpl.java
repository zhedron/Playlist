package zhedron.playlist.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import zhedron.playlist.dto.PlaylistDTO;
import zhedron.playlist.dto.UserDTO;
import zhedron.playlist.entity.Playlist;
import zhedron.playlist.entity.User;
import zhedron.playlist.enums.Role;
import zhedron.playlist.exceptions.ArtistAndAlbumNotFoundException;
import zhedron.playlist.exceptions.UserExistException;
import zhedron.playlist.exceptions.UserNotFoundException;
import zhedron.playlist.mappers.UserMapper;
import zhedron.playlist.repository.UserRepository;
import zhedron.playlist.service.UserService;

import java.util.List;

@Service
@Slf4j
public class UserServiceImpl implements UserService {
    private final UserRepository repository;

    private final PasswordEncoder passwordEncoder;

    private final UserMapper userMapper;

    @Autowired
    public UserServiceImpl(UserRepository repository, PasswordEncoder passwordEncoder, UserMapper userMapper) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
        this.userMapper = userMapper;
    }

    @Override
    public User save(User user) {
        if (repository.existsByEmail(user.getEmail())) {
            throw new UserExistException("Email already exists, use other email");
        }

        log.info("Saved user {}", user);
        user.setRole(Role.ADMIN);
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return repository.save(user);
    }

    @Override
    public User findByEmail(String email) {
        return repository.findByEmail(email).orElseThrow(() -> new UserNotFoundException("User not found with " + email));
    }

    @Override
    public UserDTO getById(long id) {
        User user = repository.findById(id).orElseThrow(() -> new UserNotFoundException("User not found with " + id));

        return userMapper.userToUserDTO(user);
    }

    @Override
    public List<PlaylistDTO> getPlaylists(long userId) {
        if (!repository.existsById(userId)) {
            throw new UserNotFoundException("User not found with " + userId);
        }

        List<Playlist> playlists = repository.findByUserId(userId);

        return userMapper.playlistsToPlaylistDTOs(playlists);
    }

    @Override
    public List<PlaylistDTO> getPlaylistsByArtistNameOrAlbumName(String artistName, String albumName) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (!repository.existsPlaylistsByArtistNameOrAlbumName(artistName, albumName, findByEmail(auth.getName()).getId())) {
            throw new ArtistAndAlbumNotFoundException("Artist name or album name not found");
        }

        List<Playlist> playlists = repository.findPlaylistsByUserIdAndArtistNameOrAlbumName(artistName, albumName, findByEmail(auth.getName()).getId());

        return userMapper.playlistsToPlaylistDTOs(playlists);
    }
}
