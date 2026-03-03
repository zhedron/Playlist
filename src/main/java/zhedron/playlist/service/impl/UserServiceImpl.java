package zhedron.playlist.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import zhedron.playlist.dto.PlaylistDTO;
import zhedron.playlist.dto.UserDTO;
import zhedron.playlist.dto.request.UserRequest;
import zhedron.playlist.dto.request.UserUpdateRequest;
import zhedron.playlist.entity.Playlist;
import zhedron.playlist.entity.Song;
import zhedron.playlist.entity.User;
import zhedron.playlist.enums.Provider;
import zhedron.playlist.enums.Role;
import zhedron.playlist.exception.*;
import zhedron.playlist.mapper.UserMapper;
import zhedron.playlist.repository.PlaylistRepository;
import zhedron.playlist.repository.UserRepository;
import zhedron.playlist.service.AESEncryptionService;
import zhedron.playlist.service.UserService;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    private final UserMapper userMapper;

    private final PlaylistRepository playlistRepository;

    private final AESEncryptionService aesEncryptionService;

    private final String PATH = "profile_image/";

    @Autowired
    public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder, UserMapper userMapper, PlaylistRepository playlistRepository, AESEncryptionService aesEncryptionService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.userMapper = userMapper;
        this.playlistRepository = playlistRepository;
        this.aesEncryptionService = aesEncryptionService;
    }

    @Override
    public User save(UserRequest requestUser, MultipartFile profilePicture) throws IOException {
        if (userRepository.existsByEmail(requestUser.getEmail())) {
            throw new UserExistException("Email already exists, use other email");
        }

        User user = new User();

        if (requestUser.getAbout() == null || requestUser.getAbout().isEmpty()) {
            requestUser.setAbout("There is no description");
        }

        if (profilePicture != null && !profilePicture.isEmpty()) {

            String namePicture = profilePicture.getOriginalFilename();

            File createdPicture = new File(PATH + namePicture);

            user.setProfilePicture(namePicture);
            user.setContentType(profilePicture.getContentType());

            profilePicture.transferTo(createdPicture.toPath());
        } else {
            user.setProfilePicture("1646346915_1-abrakadabra-fun-p-standartnaya-avatarka-standoff-3.jpg");
            user.setContentType("image/jpeg");
        }

    //    user.setRole(Role.ADMIN);
        user.setPassword(passwordEncoder.encode(requestUser.getPassword()));
        user.setCreatedAt(LocalDateTime.now());
        user.setBlocked(false);
        user.setProvider(Provider.LOCAL);
        user.setPhone(requestUser.getPhone() != null ? aesEncryptionService.encrypt(requestUser.getPhone()) : null);
        user.setEmail(requestUser.getEmail());
        user.setName(requestUser.getName());
        user.setAbout(requestUser.getAbout());
        user.setHiddenPhone(true);

        log.info("Saved user {}", user);

        return userRepository.save(user);
    }

    @Override
    public User findByEmail(String email) {
        return userRepository.findByEmail(email).orElseThrow(() -> new UserNotFoundException("User not found with " + email));
    }

    @Override
    public User getById(long id) {
        return userRepository.findById(id).orElseThrow(() -> new UserNotFoundException("User not found with " + id));
    }

    @Override
    public List<PlaylistDTO> getPlaylists(long userId) {
        User user = getById(userId);

        List<Playlist> playlists = userRepository.findByUserId(userId);

        List<PlaylistDTO> playlistResponses = null;

        for (Playlist playlist : playlists) {
            if (user.getId() == playlist.getUser().getId()) {
                playlistResponses = playlists.stream().map(userMapper::playlistToPlaylistDTO).toList();
            } else {
                playlistResponses = playlists.stream().filter(Playlist::isPublic).map(userMapper::playlistToPlaylistDTO).toList();
            }
        }

        return playlistResponses;
    }

    @Override
    public void deleteSongFromPlaylist(long playlistId, long songId) {
        User user = getCurrentUser();

        for (Playlist userPlaylist : user.getPlaylists()) {
            if (userPlaylist.getId() != playlistId) {
                throw new PlaylistNotFoundException("Playlist not found with " + playlistId);
            }
        }

        Playlist playlist = playlistRepository.findByIdAndSongId(playlistId, songId);

        Song song = user.getPlaylists().stream()
                .flatMap(p -> p.getSongs().stream())
                .filter(s -> s.getId() == songId).findFirst().orElseThrow(() -> new SongNotFoundException("Song not found with " + songId));

        playlist.getSongs().remove(song);

        playlistRepository.save(playlist);

        log.info("Deleted songId {} from playlist {}", song.getId(), playlist.getId());
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
        User user = getById(userId);

        user.setBlocked(true);

        userRepository.save(user);

        log.info("Blocked user {}", user);
    }

    @Override
    public byte[] getProfilePicture(long id) {
        User user = getById(id);

        UserDTO userDTO = userMapper.userToUserDTO(user);

        try {
            URL url = new URL(userDTO.profilePicture());

            URLConnection conn = url.openConnection();

            String contentType = userDTO.contentType();

            ByteArrayOutputStream baos = new ByteArrayOutputStream();

            BufferedImage image = ImageIO.read(conn.getInputStream());

            ImageIO.write(image, contentType.substring("image/".length()), baos);

            baos.close();

            return baos.toByteArray();
        } catch (IOException e) {
            System.out.println(e.getMessage());

            return null;
        }
    }

    @Override
    public void updateUser(UserUpdateRequest userUpdate, long userId) throws Exception {
        User user = getById(userId);

        User currentUser = getCurrentUser();

        if (currentUser.getId() != userId && !currentUser.getRole().equals(Role.ADMIN)) {
            throw new Exception("You can't change.");
        }

        if (userUpdate.getEmail() != null) {
            user.setEmail(userUpdate.getEmail());
        }
        if (userUpdate.getPassword() != null) {
            if (passwordEncoder.matches(userUpdate.getPassword(), currentUser.getPassword())) {
                throw new Exception("You use the same password.");
            }

            user.setPassword(passwordEncoder.encode(userUpdate.getPassword()));
        }
        if (userUpdate.getAbout() != null) {
            user.setAbout(userUpdate.getAbout());
        }

        if (userUpdate.getName() != null) {
            user.setName(userUpdate.getName());
        }
        if (userUpdate.getPhone() != null) {
            user.setPhone(aesEncryptionService.encrypt(userUpdate.getPhone()));
        }
        if (userUpdate.getIsHiddenPhone() != null) {
            if (user.getPhone() == null && userUpdate.getIsHiddenPhone()) {
                throw new Exception("You can't hide phone, because your phone empty");
            }
            user.setHiddenPhone(userUpdate.getIsHiddenPhone());
        }

        log.info("Updated user.");

        userRepository.save(user);
    }

    @Override
    public void changeRole(Role role, long userId) {
        User user = getById(userId);

        user.setRole(role);

        userRepository.save(user);
    }
}
