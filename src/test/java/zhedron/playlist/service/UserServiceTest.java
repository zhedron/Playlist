package zhedron.playlist.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;

import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.multipart.MultipartFile;
import zhedron.playlist.dto.PlaylistDTO;
import zhedron.playlist.dto.SongDTO;
import zhedron.playlist.dto.request.UserRequest;
import zhedron.playlist.dto.request.UserUpdateRequest;
import zhedron.playlist.entity.Playlist;
import zhedron.playlist.entity.Song;
import zhedron.playlist.entity.User;
import zhedron.playlist.enums.Role;
import zhedron.playlist.enums.Type;
import zhedron.playlist.exception.PlaylistNotFoundException;
import zhedron.playlist.exception.SongNotFoundException;
import zhedron.playlist.exception.UserNotFoundException;
import zhedron.playlist.mapper.UserMapper;
import zhedron.playlist.repository.PlaylistRepository;
import zhedron.playlist.repository.UserRepository;
import zhedron.playlist.service.impl.UserServiceImpl;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserServiceImpl userService;

    @Mock
    private Authentication auth;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AESEncryptionService aesEncryptionService;

    @Mock
    private UserMapper userMapper;

    @Mock
    private PlaylistRepository playlistRepository;

    @Test
    void getUserById_shouldReturnUser() {

        User user = new User();
        user.setId(1L);

        when(userRepository.findById(anyLong()))
                .thenReturn(Optional.of(user));

        User result = userService.getById(1L);

        assertEquals(1L, result.getId());
    }

    @Test
    public void getUserById_shouldThrowException() {

        when(userRepository.findById(1L))
                .thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class,
                () -> userService.getById(1L));
    }

    @Test
    public void uploadAvatar_shouldReturnOk() throws IOException {
        User user = new User();

        user.setId(1);
        user.setEmail("test@test.com");
        user.setProfilePicture("test.jpg");

        when(auth.getName()).thenReturn(user.getEmail());
        when(securityContext.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(securityContext);

        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));

        Path path = Paths.get("profile_image/Без названия (6).jpg");

        Resource resource = new UrlResource(path.toUri());

        byte[] fileBytes = Files.readAllBytes(path);

        MultipartFile multipartFile = new MockMultipartFile("file", resource.getFilename(), MediaType.IMAGE_JPEG_VALUE, fileBytes);

        userService.uploadAvatar(multipartFile);

        assertEquals(resource.getFilename(), multipartFile.getOriginalFilename());
    }

    @Test
    public void changeRole_shouldChangedRole() {
        User user = new User();
        user.setRole(Role.ADMIN);
        user.setId(1);
        user.setEmail("test@test.com");

        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user));

        userService.changeRole(Role.USER, user.getId());

        assertEquals(Role.USER, user.getRole());
    }

    @Test
    public void changeRole_shouldThrowException() {
        when(userRepository.findById(1L)).thenThrow(new UserNotFoundException("User not found with " + 1L));

        UserNotFoundException actualException = assertThrows(UserNotFoundException.class, () -> userService.changeRole(Role.USER, 1L));

        assertEquals("User not found with 1", actualException.getMessage());
    }

    @Test
    public void updateUser_shouldReturnOk() throws Exception {
        User currentUser = new User();
        currentUser.setId(1L);
        currentUser.setEmail("admin@test.com");
        currentUser.setRole(Role.ADMIN);

        when(auth.getName()).thenReturn(currentUser.getEmail());
        when(securityContext.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(securityContext);
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(currentUser));

        User user = new User();
        user.setId(2L);
        user.setEmail("test@test.com");
        user.setRole(Role.USER);
        user.setPassword("password");
        user.setPhone("+394111215988");
        user.setName("test");
        user.setAbout("about");

        UserUpdateRequest userUpdateRequest = new UserUpdateRequest();

        userUpdateRequest.setEmail("test@gmail.com");
        userUpdateRequest.setPassword("test");
        userUpdateRequest.setPhone("+394111215989");

        when(userRepository.findById(2L)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(userUpdateRequest.getPassword(), user.getPassword())).thenReturn(false);
        when(aesEncryptionService.encrypt(any())).thenReturn(userUpdateRequest.getPhone());
        when(passwordEncoder.encode(any())).thenReturn(userUpdateRequest.getPassword());

        userService.updateUser(userUpdateRequest, user.getId());

        assertEquals(userUpdateRequest.getEmail(), user.getEmail());
        assertEquals(userUpdateRequest.getPassword(), user.getPassword());
        assertEquals(userUpdateRequest.getPhone(), user.getPhone());
    }

    @Test
    public void updateUser_shouldThrowException() {
        User currentUser = new User();
        currentUser.setId(1L);
        currentUser.setEmail("admin@test.com");
        currentUser.setRole(Role.USER);

        when(auth.getName()).thenReturn(currentUser.getEmail());
        when(securityContext.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(securityContext);
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(currentUser));

        User user = new User();
        user.setId(2L);

        UserUpdateRequest userUpdateRequest = new UserUpdateRequest();

        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user));

        Exception exception = assertThrows(Exception.class, () -> userService.updateUser(userUpdateRequest, user.getId()));

        assertEquals("You can't change.", exception.getMessage());
    }

    @Test
    public void blockUser_shouldReturnOk() {
        User user = new User();

        user.setId(1L);
        user.setBlocked(true);

        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user));

        userService.blockUser(user.getId());

        assertTrue(user.isBlocked());
    }

    @Test
    public void blockUser_shouldThrowException() {
        when(userRepository.findById(999L)).thenThrow(new UserNotFoundException("User not found with " + 999L));

        UserNotFoundException actualException = assertThrows(UserNotFoundException.class,
                () -> userService.blockUser(999L));

        assertEquals("User not found with 999", actualException.getMessage());
    }

    @Test
    public void createUser_shouldReturnOk() {
        UserRequest userRequest = new UserRequest();
        userRequest.setEmail("test@test.com");
        userRequest.setPassword("password");
        userRequest.setPhone("+394111215988");
        userRequest.setName("test");
        userRequest.setAbout("about");

        when(passwordEncoder.encode(anyString())).thenReturn(userRequest.getPassword());
        when(aesEncryptionService.encrypt(anyString())).thenReturn(userRequest.getPhone());
        when(userRepository.save(any(User.class))).thenAnswer(i -> {
            User saved = i.getArgument(0);
            saved.setId(1L);
            return saved;
        });

        User user = userService.save(userRequest);

        assertNotNull(user);

        assertEquals(userRequest.getEmail(), user.getEmail());
        assertEquals(userRequest.getPassword(), user.getPassword());
        assertEquals(userRequest.getPhone(), user.getPhone());
        assertEquals(userRequest.getName(), user.getName());
        assertEquals(userRequest.getAbout(), user.getAbout());
    }

    @Test
    public void getPlaylistsByUser_shouldReturnOk() {
        User user = new User();
        user.setId(1L);
        user.setEmail("test@test.com");

        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user));

        Song song = new Song();

        song.setId(1L);
        song.setViews(0);
        song.setCreatedAt(LocalDateTime.now());
        song.setArtistName("test_artist");
        song.setAlbumName("test_album");
        song.setType(Type.SINGLE);

        Set<Song> songs = new HashSet<>();

        songs.add(song);

        Playlist playlist = new Playlist();

        playlist.setId(1L);
        playlist.setUser(user);
        playlist.setSongs(songs);
        playlist.setCreatedAt(LocalDateTime.now());
        playlist.setPublic(true);

        List<Playlist> playlists = new ArrayList<>();
        playlists.add(playlist);

        SongDTO songDTO = new SongDTO(song.getId(), song.getArtistName(), song.getAlbumName(), song.getViews(), song.getCreatedAt(), null, null, 0, song.getType(), null, null);

        Set<SongDTO> setSongDTO = Set.of(songDTO);

        PlaylistDTO playlistDTO = new PlaylistDTO(playlist.getId(), setSongDTO, 0, 0, playlist.isPublic(), 0, playlist.getCreatedAt());

        when(userRepository.findByUserId(anyLong())).thenReturn(playlists);
        when(userMapper.playlistToPlaylistDTO(any())).thenReturn(playlistDTO);

        List<PlaylistDTO> playlistDTOS = userService.getPlaylists(user.getId());

        assertNotNull(playlistDTOS);
        assertEquals(playlistDTOS.size(), playlists.size());
        assertEquals(playlistDTOS.get(0).songs().size(), songs.size());

        verify(userRepository).findByUserId(anyLong());
        verify(userMapper).playlistToPlaylistDTO(any());
    }

    @Test
    public void getPlaylistsByUser_shouldNotFoundUser() {
        when(userRepository.findById(anyLong())).thenThrow(new UserNotFoundException("User not found with 1"));

        UserNotFoundException exception = assertThrows(UserNotFoundException.class,
                () -> userService.getPlaylists(1L));

        assertEquals("User not found with 1", exception.getMessage());
    }

    @Test
    public void deleteSongFromPlaylist_shouldReturnOk() {
        User user = new User();
        user.setId(1L);
        user.setEmail("test@test.com");

        Song song = new Song();

        song.setId(1L);
        song.setViews(0);
        song.setCreatedAt(LocalDateTime.now());
        song.setArtistName("test_artist");
        song.setAlbumName("test_album");
        song.setType(Type.SINGLE);

        Set<Song> songs = new HashSet<>();

        songs.add(song);

        Playlist playlist = new Playlist();

        playlist.setId(1L);
        playlist.setUser(user);
        playlist.setSongs(songs);
        playlist.setCreatedAt(LocalDateTime.now());
        playlist.setPublic(true);

        List<Playlist> playlists = new ArrayList<>();
        playlists.add(playlist);

        user.setPlaylists(playlists);

        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));
        when(auth.getName()).thenReturn(user.getEmail());
        when(securityContext.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(securityContext);

        when(playlistRepository.findByIdAndSongId(playlist.getId(), song.getId())).thenReturn(playlist);

        userService.deleteSongFromPlaylist(playlist.getId(), song.getId());

        assertEquals(Collections.emptySet(), playlist.getSongs());

        verify(userRepository).findByEmail(anyString());
        verify(playlistRepository).findByIdAndSongId(playlist.getId(), song.getId());
    }

    @Test
    public void deleteSongFromPlaylist_shouldNotFoundPlaylist() {
        User user = new User();
        user.setId(1L);
        user.setEmail("test@test.com");

        Song song = new Song();

        song.setId(1L);
        song.setViews(0);
        song.setCreatedAt(LocalDateTime.now());
        song.setArtistName("test_artist");
        song.setAlbumName("test_album");
        song.setType(Type.SINGLE);

        Set<Song> songs = new HashSet<>();

        songs.add(song);

        Playlist playlist = new Playlist();

        playlist.setSongs(songs);
        playlist.setCreatedAt(LocalDateTime.now());
        playlist.setPublic(true);
        playlist.setUser(user);

        List<Playlist> playlists = new ArrayList<>();
        playlists.add(playlist);

        user.setPlaylists(playlists);

        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));
        when(auth.getName()).thenReturn(user.getEmail());
        when(securityContext.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(securityContext);

        when(playlistRepository.findByIdAndSongId(playlist.getId(), song.getId())).thenThrow(new PlaylistNotFoundException("Playlist not found with 1"));

        PlaylistNotFoundException exception = assertThrows(PlaylistNotFoundException.class,() -> userService.deleteSongFromPlaylist(playlist.getId(), song.getId()));

        assertEquals("Playlist not found with 1", exception.getMessage());
    }

    @Test
    public void deleteSongFromPlaylist_shouldNotFoundSong() {
        User user = new User();
        user.setId(1L);
        user.setEmail("test@test.com");

        Song song = new Song();

        song.setViews(0);
        song.setCreatedAt(LocalDateTime.now());
        song.setArtistName("test_artist");
        song.setAlbumName("test_album");
        song.setType(Type.SINGLE);

        Set<Song> songs = new HashSet<>();

        songs.add(song);

        Playlist playlist = new Playlist();

        playlist.setId(1L);
        playlist.setSongs(songs);
        playlist.setCreatedAt(LocalDateTime.now());
        playlist.setPublic(true);
        playlist.setUser(user);

        List<Playlist> playlists = new ArrayList<>();
        playlists.add(playlist);

        user.setPlaylists(playlists);

        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));
        when(auth.getName()).thenReturn(user.getEmail());
        when(securityContext.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(securityContext);

        when(playlistRepository.findByIdAndSongId(playlist.getId(), song.getId())).thenThrow(new SongNotFoundException("Song not found with 1"));

        SongNotFoundException exception = assertThrows(SongNotFoundException.class,() -> userService.deleteSongFromPlaylist(playlist.getId(), song.getId()));

        assertEquals("Song not found with 1", exception.getMessage());
    }
}