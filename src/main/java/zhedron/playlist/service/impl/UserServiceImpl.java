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
import zhedron.playlist.enums.Provider;
import zhedron.playlist.enums.Role;
import zhedron.playlist.exceptions.*;
import zhedron.playlist.mappers.UserMapper;
import zhedron.playlist.repository.PlaylistRepository;
import zhedron.playlist.repository.UserRepository;
import zhedron.playlist.service.UserService;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
public class UserServiceImpl implements UserService {
    private final UserRepository repository;

    private final PasswordEncoder passwordEncoder;

    private final UserMapper userMapper;

    private final PlaylistRepository playlistRepository;

    @Autowired
    public UserServiceImpl(UserRepository repository, PasswordEncoder passwordEncoder, UserMapper userMapper, PlaylistRepository playlistRepository) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
        this.userMapper = userMapper;
        this.playlistRepository = playlistRepository;
    }

    @Override
    public User save(User user) {
        if (repository.existsByEmail(user.getEmail())) {
            throw new UserExistException("Email already exists, use other email");
        }

        log.info("Saved user {}", user);
        user.setRole(Role.ADMIN);
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setCreatedAt(LocalDateTime.now());
        user.setBlocked(false);
        user.setProvider(Provider.local);

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

    @Override
    public void deletePlaylist(long playlistId) {
        User user = getCurrentUser();

        Playlist playlist = playlistRepository.findById(playlistId);

        if (!user.getPlaylists().contains(playlist)) {
            throw new PlaylistNotFoundException("Playlist not found with " + playlistId);
        }

        playlistRepository.delete(playlist);

        log.info("Deleted playlist {}, {}, {}", playlist.getSongs().get(0).getArtistName(), playlist.getSongs().get(0).getAlbumName(), playlist.getSongs().get(0).getViews());
    }

    @Override
    public User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        String email = auth.getName();

        User user = findByEmail(email);

        if (user.isBlocked()) {
            throw new UserBlockedException("User " + user.getEmail() + " is blocked");
        }

        return user;
    }

    @Override
    public void blockUser(long userId) {
        UserDTO userDTO = getById(userId);

        User user = userMapper.userDTOToUser(userDTO);

        user.setBlocked(true);

        repository.save(user);

        log.info("Blocked user {}", user);
    }
}
