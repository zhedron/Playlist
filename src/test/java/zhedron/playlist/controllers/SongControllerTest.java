package zhedron.playlist.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import zhedron.playlist.config.filter.JwtFilter;
import zhedron.playlist.dto.SongDTO;
import zhedron.playlist.dto.request.SongRequest;
import zhedron.playlist.dto.response.PaginatedResponse;
import zhedron.playlist.entity.Song;
import zhedron.playlist.entity.User;
import zhedron.playlist.enums.Type;
import zhedron.playlist.exceptions.SongNotFoundException;
import zhedron.playlist.exceptions.UserNotEnoughPermissionsException;
import zhedron.playlist.mapper.SongMapper;
import zhedron.playlist.repository.SongRepository;
import zhedron.playlist.services.SongService;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SongController.class)
@AutoConfigureMockMvc(addFilters = false)
class SongControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private JwtFilter jwtFilter;

    @MockitoBean
    private SongService songService;

    @MockitoBean
    private SongMapper songMapper;

    @MockitoBean
    private SongRepository songRepository;

    @Test
    void getSongByIdShouldReturnSongDto() throws Exception {
        User creator = new User();
        creator.setId(42L);

        Song song = new Song();
        song.setId(1L);
        song.setViews(2L);
        song.setCreator(creator);

        SongDTO songDTO = new SongDTO(1L, "artist", "album", 3L, LocalDateTime.now(), "audio/mpeg", "track.mp3", 120, Type.SINGLE, null, null, 42L);

        when(songService.getSongById(1L)).thenReturn(song);
        when(songMapper.songToSongDTO(song)).thenReturn(songDTO);

        mockMvc.perform(get("/song/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.artistName").value("artist"));

        verify(songRepository).save(song);
    }

    @Test
    void getSongByIdShouldReturnNotFound() throws Exception {
        when(songService.getSongById(1L)).thenThrow(new SongNotFoundException("Song not found with 1"));

        mockMvc.perform(get("/song/1"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Song not found with 1"));
    }

    @Test
    void createSongShouldReturnCreatedSongs() throws Exception {
        SongRequest songRequest = new SongRequest();
        songRequest.setArtistName("artist");
        songRequest.setAlbumName("album");

        MockMultipartFile requestPart = new MockMultipartFile("requestSong", "", MediaType.APPLICATION_JSON_VALUE, objectMapper.writeValueAsBytes(songRequest));
        MockMultipartFile audioPart = new MockMultipartFile("files", "track.mp3", "audio/mpeg", "audio".getBytes());
        MockMultipartFile imagePart = new MockMultipartFile("image", "cover.jpg", MediaType.IMAGE_JPEG_VALUE, "image".getBytes());

        SongDTO response = new SongDTO(1L, "artist", "album", 0L, LocalDateTime.now(), "audio/mpeg", "track.mp3", 120, Type.SINGLE, "cover.jpg", "image/jpeg", 7L);

        when(songService.save(any(SongRequest.class), anyList(), any()))
                .thenReturn(List.of(response));

        mockMvc.perform(multipart("/song/create")
                        .file(audioPart)
                        .file(requestPart)
                        .file(imagePart))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].artistName").value("artist"))
                .andExpect(jsonPath("$[0].albumName").value("album"));
    }

    @Test
    void createSongShouldRejectInvalidAudioContentType() throws Exception {
        SongRequest songRequest = new SongRequest();
        songRequest.setArtistName("artist");
        songRequest.setAlbumName("album");

        MockMultipartFile requestPart = new MockMultipartFile("requestSong", "", MediaType.APPLICATION_JSON_VALUE, objectMapper.writeValueAsBytes(songRequest));
        MockMultipartFile audioPart = new MockMultipartFile("files", "track.jpg", MediaType.IMAGE_JPEG_VALUE, "wrong".getBytes());
        MockMultipartFile imagePart = new MockMultipartFile("image", "cover.jpg", MediaType.IMAGE_JPEG_VALUE, "image".getBytes());

        mockMvc.perform(multipart("/song/create")
                        .file(audioPart)
                        .file(requestPart)
                        .file(imagePart))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Upload audio file."));
    }

    @Test
    void deleteSongShouldReturnSuccessMessage() throws Exception {
        mockMvc.perform(delete("/song/delete/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Song 1 deleted"));

        verify(songService).deleteSongById(1L);
    }

    @Test
    void deleteSongShouldReturnPermissionsError() throws Exception {
        doThrow(new UserNotEnoughPermissionsException("You do not have enough permissions to delete this song"))
                .when(songService).deleteSongById(1L);

        mockMvc.perform(delete("/song/delete/1"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("You do not have enough permissions to delete this song"));
    }

    @Test
    void topSongsShouldReturnResponseFromService() throws Exception {
        when(songService.getTopSongs()).thenReturn(List.of(
                new SongDTO(3L, "artist", "album", 99L, LocalDateTime.now(), null, null, 0, Type.SINGLE, null, null, 1L)
        ));

        mockMvc.perform(get("/song/top"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(3))
                .andExpect(jsonPath("$[0].views").value(99));
    }

    @Test
    void findAllPerWeekShouldReturnPaginatedResponse() throws Exception {
        PaginatedResponse response = new PaginatedResponse(
                List.of(new SongDTO(1L, "artist", "album", 10L, LocalDateTime.now(), null, null, 180, Type.SINGLE, null, null, 1L)),
                0,
                10,
                1L,
                1,
                true,
                true,
                false,
                false
        );

        when(songService.findAllPerWeek(0, 10)).thenReturn(response);

        mockMvc.perform(get("/song/perweek"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.songs[0].artistName").value("artist"))
                .andExpect(jsonPath("$.totalElements").value(1));
    }
}
