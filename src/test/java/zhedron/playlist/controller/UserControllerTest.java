package zhedron.playlist.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import zhedron.playlist.config.SecurityConfig;
import zhedron.playlist.dto.PlaylistDTO;
import zhedron.playlist.dto.SongDTO;
import zhedron.playlist.dto.UserDTO;
import zhedron.playlist.dto.request.UserRequest;
import zhedron.playlist.dto.request.UserUpdateRequest;
import zhedron.playlist.entity.Playlist;
import zhedron.playlist.entity.Song;
import zhedron.playlist.entity.User;
import zhedron.playlist.enums.Provider;
import zhedron.playlist.enums.Role;
import zhedron.playlist.enums.Type;
import zhedron.playlist.exception.PlaylistNotFoundException;
import zhedron.playlist.exception.SongNotFoundException;
import zhedron.playlist.exception.UserNotFoundException;
import zhedron.playlist.mapper.UserMapper;
import zhedron.playlist.repository.UserRepository;
import zhedron.playlist.service.*;
import zhedron.playlist.service.impl.UserDetailsImpl;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
@Import(SecurityConfig.class)
@AutoConfigureMockMvc
public class UserControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserDetailsImpl userDetailsImpl;

    @MockitoBean
    private UserMapper userMapper;

    @MockitoBean
    private AESEncryptionService aesEncryptionService;

    @MockitoBean
    private PasswordEncoder passwordEncoder;

    private User user;

    @MockitoBean
    private CustomOauth2UserService customOauth2UserService;

    private UserDTO userDTO;

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private AuthenticationManager authenticationManager;

    @MockitoBean
    private RefreshTokenService refreshTokenService;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    public void setUp() {
        user = new User();

        user.setId(1L);
        user.setName("test");
        user.setPassword(passwordEncoder.encode("test"));
        user.setAbout("about");
        user.setEmail("test@test.com");
        user.setPhone("+394111215988");
        user.setCreatedAt(LocalDateTime.now());
        user.setProvider(Provider.LOCAL);

        userDTO = new UserDTO(user.getId(), user.getEmail(), user.getCreatedAt(),
                user.getRole(), null, user.isBlocked(),
                user.getProvider(), user.getName(), user.getAbout(),
                user.getProfilePicture(), user.getContentType(), user.getPhone(),
                user.isHiddenPhone(), user.getUpdatedAt());
    }

    @Test
    public void createUser_shouldReturnCreatedUser() throws Exception {
        UserRequest userRequest = new UserRequest();
        userRequest.setName("test");
        userRequest.setEmail("test@test.com");
        userRequest.setPhone("+394111215988");
        userRequest.setAbout("about");
        userRequest.setPassword("test");

        when(userService.save(any(UserRequest.class))).thenReturn(user);

        when(userMapper.userToUserDTO(any(User.class))).thenReturn(userDTO);

        mockMvc.perform(post("/user/registration")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userRequest))
                        .characterEncoding("UTF-8"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("test"))
                .andExpect(jsonPath("$.email").value("test@test.com"))
                .andExpect(jsonPath("$.phone").value("+394111215988"))
                .andExpect(jsonPath("$.about").value("about"));

        verify(userService).save(any(UserRequest.class));
    }

    @Test
    public void getUserById_shouldReturnUser() throws Exception {
        when(userService.getById(anyLong())).thenReturn(user);
        when(userMapper.userToUserDTO(any(User.class))).thenReturn(userDTO);

        mockMvc.perform(get("/user/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    public void getUserById_shouldReturnNotFoundUser() throws Exception {
        when(userService.getById(2)).thenThrow(new UserNotFoundException("User not found with 2"));

        mockMvc.perform(get("/user/2"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("User not found with 2"));
    }

    @Test
    @WithMockUser(username = "test@test.com", password = "test", authorities = "ADMIN")
    public void blockUser_shouldReturnBlockedUserWithAdmin() throws Exception {
        mockMvc.perform(put("/user/block/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("User blocked successfully"));
    }

    @Test
    @WithMockUser(username = "test@test.com", password = "test", authorities = "USER")
    public void blockUser_shouldReturnBlockedUserWithUser() throws Exception {
        mockMvc.perform(put("/user/block/1"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "test@test.com", password = "test", authorities = "USER")
    public void updateUser_shouldReturnUpdatedUser() throws Exception {
        UserUpdateRequest userUpdateRequest = new UserUpdateRequest();
        userUpdateRequest.setEmail("test@update.com");
        userUpdateRequest.setPhone("+394111215989");

        user.setEmail(userUpdateRequest.getEmail());
        user.setPhone(userUpdateRequest.getPhone());

        doNothing().when(userService).updateUser(userUpdateRequest, user.getId());

        mockMvc.perform(patch("/user/update/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userUpdateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("User updated successfully"));

        verify(userService).updateUser(userUpdateRequest, user.getId());
    }

    @Test
    public void updateUser_shouldReturnUserNotFound() throws Exception {
        UserUpdateRequest userUpdateRequest = new UserUpdateRequest();
        userUpdateRequest.setEmail("test@update.com");
        userUpdateRequest.setPhone("+394111215989");

        when(userService.getById(3)).thenThrow(new UserNotFoundException("User not found with 3"));
        doThrow(new UserNotFoundException("User not found with 3")).when(userService).updateUser(userUpdateRequest, 3);


        mockMvc.perform(patch("/user/update/3")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userUpdateRequest)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("User not found with 3"));

        verify(userService).updateUser(userUpdateRequest, 3);
    }

    @Test
    @WithMockUser(username = "test@test.com", password = "test", authorities = "ADMIN")
    public void changeRoleUser_shouldReturnChangedRoleWithAdmin() throws Exception {

        Role role = Role.USER;

        doNothing().when(userService).changeRole(any(Role.class), anyLong());

        mockMvc.perform(put("/user/change_role/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(role)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Changed role to " + role + " successfully"));

        verify(userService).changeRole(any(Role.class), anyLong());
    }

    @Test
    @WithMockUser(username = "test@test.com", password = "test", authorities = "USER")
    public void changeRoleUser_shouldReturnForbiddenWithUser() throws Exception {
        Role role = Role.USER;

        mockMvc.perform(put("/user/change_role/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(role)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "test@test.com", password = "test", authorities = "ADMIN")
    public void changeRoleUser_shouldReturnUserNotFound() throws Exception {
        Role role = Role.USER;

        doThrow(new UserNotFoundException("User not found with 3")).when(userService).changeRole(any(Role.class), anyLong());


        mockMvc.perform(put("/user/change_role/3")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(role)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("User not found with 3"));

        verify(userService).changeRole(any(Role.class), anyLong());
    }

    @Test
    @WithMockUser(username = "test@test.com", password = "test", authorities = "USER")
    public void uploadAvatar_shouldReturnUploadedAvatar() throws Exception {
        Path path = Paths.get("profile_image/Без названия (6).jpg");

        Resource resource = new UrlResource(path.toUri());

        byte[] bytes = Files.readAllBytes(path);

        MockMultipartFile multipartFile = new MockMultipartFile("file", resource.getFilename(), MediaType.IMAGE_JPEG_VALUE, bytes);
        doNothing().when(userService).uploadAvatar(multipartFile);

        mockMvc.perform(multipart("/user/upload-avatar").file(multipartFile)
                .contentType(MediaType.MULTIPART_FORM_DATA_VALUE))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Avatar uploaded successfully"));

        verify(userService).uploadAvatar(multipartFile);
    }

    @Test
    @WithMockUser(username = "test@test.com", password = "test", authorities = "USER")
    public void uploadAvatar_shouldReturnContentType() throws Exception {
        Path path = Paths.get("song/All The Things She Said.mp3");

        byte[] bytes = Files.readAllBytes(path);

        Resource resource = new UrlResource(path.toUri());

        MockMultipartFile multipartFile = new MockMultipartFile("file", resource.getFilename(), "audio/mpeg", bytes);


        mockMvc.perform(multipart("/user/upload-avatar").file(multipartFile)
                .contentType(MediaType.MULTIPART_FORM_DATA_VALUE))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Invalid image jpg or png format"));
    }

    @Test
    @WithMockUser(username = "test@test.com", password = "test", authorities = "USER")
    public void uploadAvatar_shouldReturnBadRequest() throws Exception {
        MockMultipartFile multipartFile = new MockMultipartFile("file", "", MediaType.IMAGE_PNG_VALUE, new byte[0]);

        mockMvc.perform(multipart("/user/upload-avatar").file(multipartFile)
                .accept(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("File must not be null or empty"));
    }

    @Test
    @WithMockUser(username = "test@test.com", password = "test", authorities = "USER")
    public void deleteSongFromPlaylist_shouldReturnDeletedSong() throws Exception {
        Song song = new Song();
        song.setId(1);

        Set<Song> songs = new HashSet<>();
        songs.add(song);

        Playlist playlist = new Playlist();
        playlist.setId(1);
        playlist.setSongs(songs);
        doNothing().when(userService).deleteSongFromPlaylist(playlist.getId(), song.getId());

        mockMvc.perform(delete("/user/playlist/delete/1/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Song from playlist deleted successfully"));

        verify(userService).deleteSongFromPlaylist(playlist.getId(), song.getId());
    }

    @Test
    @WithMockUser(username = "test@test.com", password = "test", authorities = "USER")
    public void deleteSongFromPlaylist_shouldReturnNotFoundSong() throws Exception {
        Playlist playlist = new Playlist();
        playlist.setId(1);

        doThrow(new SongNotFoundException("Song not found with 1")).when(userService).deleteSongFromPlaylist(playlist.getId(), 1);

        mockMvc.perform(delete("/user/playlist/delete/1/1"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Song not found with 1"));

        verify(userService).deleteSongFromPlaylist(playlist.getId(), 1);
    }

    @Test
    @WithMockUser(username = "test@test.com", password = "test", authorities = "USER")
    public void deleteSongFromPlaylist_shouldReturnNotFoundPlaylist() throws Exception {
        Song song = new Song();
        song.setId(1L);

        doThrow(new PlaylistNotFoundException("Playlist not found with 1")).when(userService).deleteSongFromPlaylist(1, song.getId());

        mockMvc.perform(delete("/user/playlist/delete/1/1"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Playlist not found with 1"));

        verify(userService).deleteSongFromPlaylist(1L, song.getId());
    }

    @Test
    @WithMockUser(username = "test@test.com", password = "test", authorities = "USER")
    public void getPlaylists_shouldReturnListOfPlaylists() throws Exception {
        Song song = new Song();
        song.setId(1);
        song.setCreator(user);
        song.setCreatedAt(LocalDateTime.now());
        song.setType(Type.ALBUM);

        Song song2 = new Song();
        song2.setId(2);
        song2.setCreator(user);
        song2.setCreatedAt(LocalDateTime.now());
        song2.setType(Type.ALBUM);

        Song song3 = new Song();
        song3.setId(3);
        song3.setCreator(user);
        song3.setCreatedAt(LocalDateTime.now());
        song3.setType(Type.ALBUM);

        Set<Song> songs = new HashSet<>();
        songs.add(song);
        songs.add(song2);
        songs.add(song3);

        Playlist playlist = new Playlist();
        playlist.setId(1);
        playlist.setUser(user);
        playlist.setSongs(songs);
        playlist.setPublic(true);
        playlist.setCreatedAt(LocalDateTime.now());

        List<Playlist> playlists = new ArrayList<>();
        playlists.add(playlist);

        user.setPlaylists(playlists);

        SongDTO songDTO = new SongDTO(song.getId(), song.getArtistName(), song.getAlbumName(), song.getViews(), song.getCreatedAt(), null, null, 0, song.getType());
        SongDTO songDTO2 = new SongDTO(song2.getId(), song2.getArtistName(), song2.getAlbumName(), song2.getViews(), song2.getCreatedAt(), null, null, 0, song2.getType());
        SongDTO songDTO3 = new SongDTO(song3.getId(), song3.getArtistName(), song3.getAlbumName(), song3.getViews(), song3.getCreatedAt(), null, null, 0, song3.getType());

        Set<SongDTO> songDTOs = new HashSet<>();
        songDTOs.add(songDTO);
        songDTOs.add(songDTO2);
        songDTOs.add(songDTO3);

        PlaylistDTO playlistDTO = new PlaylistDTO(playlist.getId(), songDTOs, 0, playlist.getDuration(), playlist.isPublic(), playlist.getCounter(), playlist.getCreatedAt());

        List<PlaylistDTO> playlistsDTO = new ArrayList<>();
        playlistsDTO.add(playlistDTO);

        when(userService.getPlaylists(anyLong())).thenReturn(playlistsDTO);

        mockMvc.perform(get("/user/playlists/1"))
                .andExpect(status().isOk());

        assertEquals(playlistsDTO.get(0).songs().size(), songs.size());

        verify(userService).getPlaylists(anyLong());
    }

    @Test
    @WithMockUser(username = "test@test.com", password = "test", authorities = "USER")
    public void getPlaylists_shouldReturnNotFoundUser() throws Exception {
        Song song = new Song();
        song.setId(1);
        song.setCreator(user);
        song.setCreatedAt(LocalDateTime.now());
        song.setType(Type.ALBUM);

        Song song2 = new Song();
        song2.setId(2);
        song2.setCreator(user);
        song2.setCreatedAt(LocalDateTime.now());
        song2.setType(Type.ALBUM);

        Song song3 = new Song();
        song3.setId(3);
        song3.setCreator(user);
        song3.setCreatedAt(LocalDateTime.now());
        song3.setType(Type.ALBUM);

        Set<Song> songs = new HashSet<>();
        songs.add(song);
        songs.add(song2);
        songs.add(song3);

        Playlist playlist = new Playlist();
        playlist.setId(1);
        playlist.setUser(user);
        playlist.setSongs(songs);
        playlist.setPublic(true);
        playlist.setCreatedAt(LocalDateTime.now());

        List<Playlist> playlists = new ArrayList<>();
        playlists.add(playlist);

        user.setPlaylists(playlists);

        when(userService.getPlaylists(anyLong())).thenThrow(new UserNotFoundException("User not found with 1"));

        mockMvc.perform(get("/user/playlists/1"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("User not found with 1"));

        verify(userService).getPlaylists(anyLong());
    }
}
