package zhedron.playlist.controllers;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import zhedron.playlist.config.filter.JwtFilter;
import zhedron.playlist.dto.PlaylistDTO;
import zhedron.playlist.dto.SongDTO;
import zhedron.playlist.dto.request.PlaylistRequest;
import zhedron.playlist.exceptions.PlaylistNotFoundException;
import zhedron.playlist.exceptions.SongNotFoundException;
import zhedron.playlist.exceptions.UserNotFoundException;
import zhedron.playlist.services.PlaylistService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PlaylistController.class)
@AutoConfigureMockMvc(addFilters = false)
class PlaylistControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private JwtFilter jwtFilter;

    @MockitoBean
    private PlaylistService playlistService;

    @Test
    void addSongInPlaylistShouldReturnSuccessMessage() throws Exception {
        mockMvc.perform(post("/playlist/add/1/10").param("public", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Playlist added"));

        verify(playlistService).addSong(1L, true, 10L);
    }

    @Test
    void addSongInPlaylistShouldReturnNotFoundWhenSongDoesNotExist() throws Exception {
        doThrow(new SongNotFoundException("Song not found with 1"))
                .when(playlistService).addSong(1L, false, 10L);

        mockMvc.perform(post("/playlist/add/1/10").param("public", "false"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Song not found with 1"));
    }

    @Test
    void findByArtistNameOrAlbumNameShouldReturnPlaylists() throws Exception {
        SongDTO song = new SongDTO(1L, "artist", "album", 10L, LocalDateTime.now(), null, null, 120, null, null, null, 7L);
        PlaylistDTO playlist = new PlaylistDTO(1L, Set.of(song), 10L, 120L, true, 1, LocalDateTime.now(), "cover.jpg", "image/jpeg", "Favorites");

        when(playlistService.getPlaylistsByArtistNameOrAlbumName("artist", "album", 7L))
                .thenReturn(List.of(playlist));

        mockMvc.perform(get("/playlist/search")
                        .param("artistName", "artist")
                        .param("albumName", "album")
                        .param("userId", "7"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].songs[0].artistName").value("artist"))
                .andExpect(jsonPath("$[0].songs[0].albumName").value("album"));
    }

    @Test
    void findByArtistNameOrAlbumNameShouldReturnUserNotFound() throws Exception {
        when(playlistService.getPlaylistsByArtistNameOrAlbumName("artist", "album", 7L))
                .thenThrow(new UserNotFoundException("User not found with 7"));

        mockMvc.perform(get("/playlist/search")
                        .param("artistName", "artist")
                        .param("albumName", "album")
                        .param("userId", "7"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("User not found with 7"));
    }

    @Test
    void findByArtistNameOrAlbumNameShouldReturnPlaylistNotFound() throws Exception {
        when(playlistService.getPlaylistsByArtistNameOrAlbumName("artist", "album", 7L))
                .thenThrow(new PlaylistNotFoundException("Playlist not found with artist album"));

        mockMvc.perform(get("/playlist/search")
                        .param("artistName", "artist")
                        .param("albumName", "album")
                        .param("userId", "7"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Playlist not found with artist album"));
    }

    @Test
    void changeAvailableShouldReturnSuccessMessage() throws Exception {
        mockMvc.perform(post("/playlist/available/10").param("public", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Changed available true"));

        verify(playlistService).changeAvailable(10L, true);
    }

    @Test
    void createPlaylistShouldReturnCreatedMessage() throws Exception {
        org.springframework.mock.web.MockMultipartFile requestPart = new org.springframework.mock.web.MockMultipartFile(
                "playlistRequest",
                "",
                "application/json",
                "{\"title\":\"Favorites\",\"public\":true}".getBytes()
        );
        org.springframework.mock.web.MockMultipartFile imagePart = new org.springframework.mock.web.MockMultipartFile(
                "file",
                "cover.jpg",
                "image/jpeg",
                "image".getBytes()
        );

        mockMvc.perform(multipart("/playlist/create")
                        .file(requestPart)
                        .file(imagePart))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("Playlist created"));

        verify(playlistService).savePlaylist(any(PlaylistRequest.class), any());
    }

    @Test
    void createPlaylistShouldRejectInvalidImageContentType() throws Exception {
        org.springframework.mock.web.MockMultipartFile requestPart = new org.springframework.mock.web.MockMultipartFile(
                "playlistRequest",
                "",
                "application/json",
                "{\"title\":\"Favorites\",\"public\":true}".getBytes()
        );
        org.springframework.mock.web.MockMultipartFile filePart = new org.springframework.mock.web.MockMultipartFile(
                "file",
                "track.mp3",
                "audio/mpeg",
                "audio".getBytes()
        );

        mockMvc.perform(multipart("/playlist/create")
                        .file(requestPart)
                        .file(filePart))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Upload image"));
    }

    @Test
    void deletePlaylistShouldReturnSuccessMessage() throws Exception {
        mockMvc.perform(delete("/playlist/delete/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Playlist deleted successfully"));

        verify(playlistService).deletePlaylist(10L);
    }

    @Test
    void deletePlaylistShouldReturnNotFoundWhenPlaylistMissing() throws Exception {
        doThrow(new PlaylistNotFoundException("Playlist not found with 10"))
                .when(playlistService).deletePlaylist(10L);

        mockMvc.perform(delete("/playlist/delete/10"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Playlist not found with 10"));
    }
}
