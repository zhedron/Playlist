package zhedron.playlist.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import zhedron.playlist.config.filter.JwtFilter;
import zhedron.playlist.dto.PlaylistDTO;
import zhedron.playlist.dto.SongDTO;
import zhedron.playlist.dto.UserDTO;
import zhedron.playlist.dto.request.UserRequest;
import zhedron.playlist.dto.request.UserUpdateRequest;
import zhedron.playlist.entity.User;
import zhedron.playlist.enums.Provider;
import zhedron.playlist.enums.Role;
import zhedron.playlist.enums.Type;
import zhedron.playlist.exceptions.PlaylistNotFoundException;
import zhedron.playlist.exceptions.UserNotFoundException;
import zhedron.playlist.mapper.UserMapper;
import zhedron.playlist.services.AESEncryptionService;
import zhedron.playlist.services.UserService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
@AutoConfigureMockMvc(addFilters = false)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private JwtFilter jwtFilter;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private UserMapper userMapper;

    @MockitoBean
    private AESEncryptionService aesEncryptionService;

    private User user;
    private UserDTO userDTO;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setEmail("test@test.com");
        user.setName("test");
        user.setAbout("about");
        user.setProvider(Provider.LOCAL);
        user.setCreatedAt(LocalDateTime.now());

        userDTO = new UserDTO(
                1L,
                "test@test.com",
                user.getCreatedAt(),
                Role.USER,
                null,
                false,
                Provider.LOCAL,
                "test",
                "about",
                "avatar.jpg",
                "image/jpeg",
                "encrypted-phone",
                false,
                null
        );
    }

    @Test
    void createUserShouldReturnCreatedUser() throws Exception {
        UserRequest request = new UserRequest();
        request.setName("test");
        request.setEmail("test@test.com");
        request.setPassword("secret");
        request.setPhone("+394111215988");
        request.setAbout("about");

        when(userService.save(any(UserRequest.class))).thenReturn(user);
        when(userMapper.userToUserDTO(user)).thenReturn(userDTO);

        mockMvc.perform(post("/user/registration")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("test"))
                .andExpect(jsonPath("$.email").value("test@test.com"));
    }

    @Test
    void getUserByIdShouldReturnDecryptedPhoneWhenVisible() throws Exception {
        when(userService.getById(1L)).thenReturn(user);
        when(userMapper.userToUserDTO(user)).thenReturn(userDTO);
        when(aesEncryptionService.decrypt("encrypted-phone")).thenReturn("+394111215988");

        mockMvc.perform(get("/user/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.phone").value("+394111215988"));
    }

    @Test
    void getUserByIdShouldReturnNotFound() throws Exception {
        when(userService.getById(2L)).thenThrow(new UserNotFoundException("User not found with 2"));

        mockMvc.perform(get("/user/2"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("User not found with 2"));
    }

    @Test
    void blockUserShouldReturnSuccessMessage() throws Exception {
        mockMvc.perform(put("/user/block/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("User blocked successfully"));

        verify(userService).blockUser(1L);
    }

    @Test
    void updateUserShouldReturnSuccessMessage() throws Exception {
        UserUpdateRequest request = new UserUpdateRequest();
        request.setEmail("updated@test.com");
        request.setPhone("+394111215989");

        doNothing().when(userService).updateUser(any(UserUpdateRequest.class), any(Long.class));

        mockMvc.perform(patch("/user/update/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("User updated successfully"));
    }

    @Test
    void changeRoleShouldReturnSuccessMessage() throws Exception {
        mockMvc.perform(put("/user/change_role/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(Role.USER)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Changed role to USER successfully"));

        verify(userService).changeRole(Role.USER, 1L);
    }

    @Test
    void uploadAvatarShouldRejectInvalidContentType() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "audio.mp3", "audio/mpeg", "audio".getBytes());

        mockMvc.perform(multipart("/user/upload-avatar").file(file))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Invalid image jpg or png format"));
    }

    @Test
    void uploadAvatarShouldRejectEmptyFile() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "", MediaType.IMAGE_PNG_VALUE, new byte[0]);

        mockMvc.perform(multipart("/user/upload-avatar").file(file))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("File must not be null or empty"));
    }

    @Test
    void deleteSongFromPlaylistShouldReturnSuccessMessage() throws Exception {
        mockMvc.perform(delete("/user/playlist/delete/1/2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Song from playlist deleted successfully"));

        verify(userService).deleteSongFromPlaylist(1L, 2L);
    }

    @Test
    void deleteSongFromPlaylistShouldReturnPlaylistNotFound() throws Exception {
        doThrow(new PlaylistNotFoundException("Playlist not found with 1"))
                .when(userService).deleteSongFromPlaylist(1L, 2L);

        mockMvc.perform(delete("/user/playlist/delete/1/2"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Playlist not found with 1"));
    }

    @Test
    void getPlaylistsShouldReturnPlaylistList() throws Exception {
        SongDTO song = new SongDTO(3L, "artist", "album", 5L, LocalDateTime.now(), null, null, 180, Type.SINGLE, null, null, 1L);
        PlaylistDTO playlist = new PlaylistDTO(11L, Set.of(song), 5L, 180L, true, 1, LocalDateTime.now());

        when(userService.getPlaylists(1L)).thenReturn(List.of(playlist));

        mockMvc.perform(get("/user/playlists/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(11))
                .andExpect(jsonPath("$[0].songs[0].albumName").value("album"));
    }
}
