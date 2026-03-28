package zhedron.playlist.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import org.springframework.web.multipart.MultipartFile;
import zhedron.playlist.config.SecurityConfig;
import zhedron.playlist.dto.SongDTO;
import zhedron.playlist.dto.request.SongRequest;
import zhedron.playlist.dto.response.PaginatedResponse;
import zhedron.playlist.enums.Type;
import zhedron.playlist.exception.SongNotFoundException;
import zhedron.playlist.exception.UserNotEnoughPermissionsException;
import zhedron.playlist.mapper.SongMapper;
import zhedron.playlist.repository.SongRepository;
import zhedron.playlist.repository.UserRepository;
import zhedron.playlist.service.*;
import zhedron.playlist.entity.Song;
import zhedron.playlist.service.impl.UserDetailsImpl;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SongController.class)
@Import(SecurityConfig.class)
@AutoConfigureMockMvc
class SongControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SongService songService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private SongMapper songMapper;

    @MockitoBean
    private SongRepository songRepository;

    @MockitoBean
    private UserDetailsImpl userDetailsImpl;

    @MockitoBean
    private CustomOauth2UserService customOauth2UserService;

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private PasswordEncoder passwordEncoder;

    @MockitoBean
    private AuthenticationManager authenticationManager;

    @MockitoBean
    private RefreshTokenService refreshTokenService;

    @MockitoBean
    private UserService userService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void getSongById_shouldReturn200() throws Exception {
        Song song = new Song();
        song.setId(1L);

        when(songService.getSongById(1L))
                .thenReturn(song);

        mockMvc.perform(get("/song/1"))
                .andExpect(status().isOk());

        verify(songService).getSongById(1L);
    }

    @Test
    public void getSongById_shouldReturnNotFound() throws Exception {
        when(songService.getSongById(1L)).thenThrow(new SongNotFoundException("Song not found with 1"));

        mockMvc.perform(get("/song/1"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Song not found with 1"));

        verify(songService).getSongById(1L);
    }

    @Test
    @WithMockUser(username = "test@test.com", password = "test", authorities = "USER")
    public void createSong_shouldReturnCreatedSong() throws Exception {
        SongRequest songRequest = new SongRequest();
        songRequest.setArtistName("test_artist");
        songRequest.setAlbumName("test_album");

        Path path = Paths.get("song/All The Things She Said.mp3");

        byte[] bytes = Files.readAllBytes(path);

        Resource resource = new UrlResource(path.toUri());

        MockMultipartFile multipartFile = new MockMultipartFile("files", resource.getFilename(), "audio/mpeg", bytes);

        List<MultipartFile> files = new ArrayList<>();
        files.add(multipartFile);

        String json = objectMapper.writeValueAsString(songRequest);

        MockMultipartFile multipartJson = new MockMultipartFile("requestSong", null, MediaType.APPLICATION_JSON_VALUE, json.getBytes());

        Song song = new Song();
        song.setId(1L);
        song.setType(Type.SINGLE);
        song.setArtistName(songRequest.getArtistName());
        song.setAlbumName(songRequest.getAlbumName());
        song.setCreatedAt(LocalDateTime.now());
        song.setFileName(multipartFile.getOriginalFilename());
        song.setContentType(multipartFile.getContentType());

        SongDTO songDTO = new SongDTO(song.getId(), song.getArtistName(), song.getAlbumName(), song.getViews(), song.getCreatedAt(), song.getContentType(), song.getFileName(), song.getDuration(), song.getType());

        List<SongDTO> songsDTO = new ArrayList<>();
        songsDTO.add(songDTO);

        when(songService.save(songRequest, files)).thenReturn(songsDTO);

        mockMvc.perform(multipart("/song/create")
                .file(multipartFile)
                .file(multipartJson)
                .contentType(MediaType.MULTIPART_FORM_DATA_VALUE))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].artistName").value("test_artist"))
                .andExpect(jsonPath("$[0].albumName").value("test_album"));

        verify(songService).save(songRequest, files);
    }

    @Test
    public void createSong_shouldReturnBadRequest() throws Exception {
        SongRequest songRequest = new SongRequest();
        songRequest.setArtistName("test_artist");
        songRequest.setAlbumName("test_album");

        Path path = Paths.get("song/All The Things She Said.mp3");

        byte[] bytes = Files.readAllBytes(path);

        Resource resource = new UrlResource(path.toUri());

        MockMultipartFile multipartFile = new MockMultipartFile("files", resource.getFilename(), MediaType.IMAGE_JPEG_VALUE, bytes);

        String json = objectMapper.writeValueAsString(songRequest);

        MockMultipartFile multipartJson = new MockMultipartFile("requestSong", null, MediaType.APPLICATION_JSON_VALUE, json.getBytes());

        Song song = new Song();
        song.setId(1L);
        song.setType(Type.SINGLE);
        song.setArtistName(songRequest.getArtistName());
        song.setAlbumName(songRequest.getAlbumName());
        song.setCreatedAt(LocalDateTime.now());
        song.setFileName(multipartFile.getOriginalFilename());
        song.setContentType(multipartFile.getContentType());

        mockMvc.perform(multipart("/song/create")
                .file(multipartFile)
                .file(multipartJson)
                .contentType(MediaType.MULTIPART_FORM_DATA_VALUE))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Upload audio file."));
    }

    @Test
    @WithMockUser(username = "test@test.com", password = "test", authorities = "USER")
    public void deleteSong_shouldReturnDeletedSong() throws Exception {
        Song song = new Song();
        song.setId(1);
        doNothing().when(songService).deleteSongById(song.getId());

        mockMvc.perform(delete("/song/delete/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Song 1 deleted"));

        verify(songService).deleteSongById(song.getId());
    }
    @Test
    @WithMockUser(username = "test@test.com", password = "test", authorities = "USER")
    public void deleteSong_shouldReturnNotFoundSong() throws Exception {
        doThrow(new SongNotFoundException("Song not found with 1")).when(songService).deleteSongById(anyLong());

        mockMvc.perform(delete("/song/delete/1"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Song not found with 1"));

        verify(songService).deleteSongById(anyLong());
    }

    @Test
    @WithMockUser(username = "test@test.com", password = "test", authorities = "USER")
    public void deleteSong_shouldReturnNotPermissionsUser()  throws Exception {
        Song song = new Song();
        song.setId(1);
        doThrow(new UserNotEnoughPermissionsException("You do not have enough permissions to delete this song")).when(songService).deleteSongById(anyLong());

        mockMvc.perform(delete("/song/delete/1"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("You do not have enough permissions to delete this song"));

        verify(songService).deleteSongById(song.getId());
    }

    @Test
    public void topSongs_shouldReturnTopSongs() throws Exception {
        Song song = new Song();
        song.setId(1);
        song.setViews(5);

        Song song2 = new Song();
        song2.setId(2);
        song2.setViews(4);

        Song song3 = new Song();
        song3.setId(3);
        song3.setViews(7);

        Song song4 = new Song();
        song4.setId(4);
        song4.setViews(1);

        SongDTO songDTO = new SongDTO(song.getId(), song.getArtistName(), song.getAlbumName(), song.getViews(), song.getCreatedAt(), song.getContentType(), song.getFileName(), song.getDuration(), song.getType());
        SongDTO songDTO2 = new SongDTO(song2.getId(), song2.getArtistName(), song2.getAlbumName(), song2.getViews(), song2.getCreatedAt(), song2.getContentType(), song2.getFileName(), song2.getDuration(), song2.getType());
        SongDTO songDTO3 = new SongDTO(song3.getId(), song3.getArtistName(), song3.getAlbumName(), song3.getViews(), song3.getCreatedAt(), song3.getContentType(), song3.getFileName(), song3.getDuration(), song3.getType());
        SongDTO songDTO4 = new SongDTO(song4.getId(), song4.getArtistName(), song4.getAlbumName(), song4.getViews(), song4.getCreatedAt(), song4.getContentType(), song4.getFileName(), song4.getDuration(), song4.getType());

        List<SongDTO> songsDTO = new ArrayList<>();
        songsDTO.add(songDTO);
        songsDTO.add(songDTO2);
        songsDTO.add(songDTO3);
        songsDTO.add(songDTO4);

        when(songService.getTopSongs()).thenReturn(songsDTO);

        mockMvc.perform(get("/song/top")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(songService).getTopSongs();
    }
    @Test
    public void findAllPerWeek_shouldReturnSongs() throws Exception {
        Song song = new Song();
        song.setId(1);
        song.setCreatedAt(LocalDateTime.now().minusWeeks(1));

        Song song2 = new Song();
        song2.setId(2);
        song2.setCreatedAt(LocalDateTime.now().minusDays(3));

        Song song3 = new Song();
        song3.setId(3);
        song3.setCreatedAt(LocalDateTime.now().minusDays(4));

        List<Song> songs = new ArrayList<>();
        songs.add(song);
        songs.add(song2);
        songs.add(song3);

        Page<Song> songPage = new PageImpl<>(songs);

        SongDTO songDTO = new SongDTO(song.getId(), song.getArtistName(), song.getAlbumName(), song.getViews(), song.getCreatedAt(), song.getContentType(), song.getFileName(), song.getDuration(), song.getType());
        SongDTO songDTO2 = new SongDTO(song2.getId(), song2.getArtistName(), song2.getAlbumName(), song2.getViews(), song2.getCreatedAt(), song2.getContentType(), song2.getFileName(), song2.getDuration(), song2.getType());
        SongDTO songDTO3 = new SongDTO(song3.getId(), song3.getArtistName(), song3.getAlbumName(), song3.getViews(), song3.getCreatedAt(), song3.getContentType(), song3.getFileName(), song3.getDuration(), song3.getType());

        List<SongDTO> songsDTO = new ArrayList<>();
        songsDTO.add(songDTO);
        songsDTO.add(songDTO2);
        songsDTO.add(songDTO3);

        PaginatedResponse response = new PaginatedResponse(
                songsDTO,
                songPage.getNumber(),
                songPage.getSize(),
                songPage.getTotalElements(),
                songPage.getTotalPages(),
                songPage.isLast(),
                songPage.isFirst(),
                songPage.hasNext(),
                songPage.hasPrevious()
        );

        when(songService.findAllPerWeek(0, 10)).thenReturn(response);

        mockMvc.perform(get("/song/perweek")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        assertNotNull(response);
        assertEquals(response.songs().size(), songs.size());

        verify(songService).findAllPerWeek(0, 10);
    }
}