package zhedron.playlist.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import zhedron.playlist.config.SecurityConfig;
import zhedron.playlist.dto.PlaylistDTO;
import zhedron.playlist.dto.SongDTO;
import zhedron.playlist.entity.Playlist;
import zhedron.playlist.entity.Song;
import zhedron.playlist.exception.PlaylistNotFoundException;
import zhedron.playlist.exception.SongNotFoundException;
import zhedron.playlist.exception.UserNotFoundException;
import zhedron.playlist.repository.UserRepository;
import zhedron.playlist.service.*;
import zhedron.playlist.service.impl.UserDetailsImpl;

import java.util.*;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PlaylistController.class)
@Import(SecurityConfig.class)
@AutoConfigureMockMvc
public class PlaylistControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private AuthenticationManager authenticationManager;

    @MockitoBean
    private RefreshTokenService refreshTokenService;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private PlaylistService playlistService;

    @MockitoBean
    private UserDetailsImpl userDetailsImpl;

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private CustomOauth2UserService customOauth2UserService;

    @Test
    @WithMockUser(username = "test@test.com", password = "test", authorities = "USER")
    public void addSongInPlaylist_shouldReturnAddedSong() throws Exception {
        Song song = new Song();
        song.setId(1);


        Playlist playlist = new Playlist();
        playlist.setId(1);
        playlist.setSongs(Set.of(song));
        playlist.setPublic(true);

        doNothing().when(playlistService).addSong(anyLong(), anyBoolean());

        mockMvc.perform(post("/playlist/add/1")
                .param("public", String.valueOf(playlist.isPublic()))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Playlist added"));

        verify(playlistService).addSong(anyLong(), anyBoolean());
    }

    @Test
    @WithMockUser(username = "test@test.com", password = "test", authorities = "USER")
    public void addSongInPlaylist_shouldReturnNotFoundSong() throws Exception {
        doThrow(new SongNotFoundException("Song not found with 1")).when(playlistService).addSong(anyLong(), anyBoolean());

        mockMvc.perform(post("/playlist/add/1")
                .param("public", String.valueOf(false))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Song not found with 1"));

        verify(playlistService).addSong(anyLong(), anyBoolean());
    }

    @Test
    public void findByArtistNameOrAlbumName_shouldReturnSongsFromPlaylist() throws Exception {
        Song song = new Song();
        song.setId(1);
        song.setArtistName("test_artist");
        song.setAlbumName("test_album");

        Set<Song> songs = new HashSet<>();
        songs.add(song);

        Playlist playlist = new Playlist();
        playlist.setId(1);
        playlist.setPublic(true);
        playlist.setSongs(songs);

        SongDTO songDTO = new SongDTO(song.getId(), song.getArtistName(), song.getAlbumName(), song.getViews(), song.getCreatedAt(), null, null, song.getDuration(), song.getType());

        Set<SongDTO> songsDTO = new HashSet<>();
        songsDTO.add(songDTO);

        PlaylistDTO playlistDTO = new PlaylistDTO(playlist.getId(), songsDTO, 0, playlist.getDuration(), playlist.isPublic(), playlist.getCounter(), playlist.getCreatedAt());

        List<PlaylistDTO> playlistListDTO = new ArrayList<>();
        playlistListDTO.add(playlistDTO);

        when(playlistService.getPlaylistsByArtistNameOrAlbumName(song.getArtistName(), song.getAlbumName(), 1L)).thenReturn(playlistListDTO);

        mockMvc.perform(get("/playlist/search")
                .param("artistName", "test_artist")
                .param("albumName", "test_album")
                .param("userId", "1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].songs[0].artistName").value("test_artist"))
                .andExpect(jsonPath("$[0].songs[0].albumName").value("test_album"));

        verify(playlistService).getPlaylistsByArtistNameOrAlbumName(song.getArtistName(), song.getAlbumName(), 1L);
    }

    @Test
    public void findByArtistNameOrAlbumName_shouldReturnNotFoundUser() throws Exception {
        Song song = new Song();
        song.setId(1);
        song.setArtistName("test_artist");
        song.setAlbumName("test_album");

        Set<Song> songs = new HashSet<>();
        songs.add(song);

        Playlist playlist = new Playlist();
        playlist.setId(1);
        playlist.setPublic(true);
        playlist.setSongs(songs);

        when(playlistService.getPlaylistsByArtistNameOrAlbumName(song.getArtistName(), song.getAlbumName(), 1L)).thenThrow(new UserNotFoundException("User not found with 1"));

        mockMvc.perform(get("/playlist/search")
                        .param("artistName", "test_artist")
                        .param("albumName", "test_album")
                        .param("userId", "1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("User not found with 1"));

        verify(playlistService).getPlaylistsByArtistNameOrAlbumName(song.getArtistName(), song.getAlbumName(), 1L);
    }

    @Test
    public void findByArtistNameOrAlbumName_shouldReturnNotFoundSongInPlaylist() throws Exception {
        when(playlistService.getPlaylistsByArtistNameOrAlbumName("test_artist", "test_album", 1L)).thenThrow(new PlaylistNotFoundException("Playlist not found with artist_test album_test"));

        mockMvc.perform(get("/playlist/search")
                        .param("artistName", "test_artist")
                        .param("albumName", "test_album")
                        .param("userId", "1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Playlist not found with artist_test album_test"));

        verify(playlistService).getPlaylistsByArtistNameOrAlbumName("test_artist", "test_album", 1L);
    }
}
