package zhedron.playlist.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.springframework.data.domain.*;

import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;
import zhedron.playlist.dto.request.SongRequest;
import zhedron.playlist.entity.Playlist;
import zhedron.playlist.entity.Song;
import zhedron.playlist.dto.SongDTO;
import zhedron.playlist.dto.response.PaginatedResponse;
import zhedron.playlist.entity.User;
import zhedron.playlist.enums.Type;
import zhedron.playlist.exception.SongNotFoundException;
import zhedron.playlist.exception.UserNotEnoughPermissionsException;
import zhedron.playlist.mapper.SongMapper;
import zhedron.playlist.repository.PlaylistRepository;
import zhedron.playlist.repository.SongRepository;
import zhedron.playlist.service.impl.SongServiceImpl;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
class SongServiceTest {

    @Mock
    private SongRepository songRepository;

    @Mock
    private SongMapper songMapper;

    @InjectMocks
    private SongServiceImpl songService;

    @Mock
    private UserService userService;

    @Mock
    private PlaylistRepository playlistRepository;

    @Test
    void getSongById_shouldReturnSong() {

        Song song = new Song();
        song.setId(1L);

        when(songRepository.findById(1L)).thenReturn(Optional.of(song));

        Song result = songService.getSongById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());

        verify(songRepository).findById(1L);
    }

    @Test
    void getSongById_shouldThrowException() {

        when(songRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(SongNotFoundException.class,
                () -> songService.getSongById(1L));
    }

    @Test
    public void findAllPerWeek_shouldReturnPaginatedResponse() {

        Song song = new Song();
        song.setId(1L);
        song.setCreatedAt(LocalDateTime.now());

        Page<Song> page = new PageImpl<>(List.of(song));

        when(songRepository.findAll(any(Pageable.class)))
                .thenReturn(page);

        SongDTO dto = new SongDTO(1, "test_artist", "test_album", 0, LocalDateTime.now(), null, null, 0, Type.SINGLE);

        when(songMapper.songToSongDTO(song)).thenReturn(dto);

        PaginatedResponse response = songService.findAllPerWeek(0,10);

        assertEquals(1, response.songs().size());
    }

    @Test
    public void createSong_shouldCreatedSong() throws IOException {
        User user = new User();
        user.setId(1L);
        user.setEmail("test@test.com");

        when(userService.getCurrentUser()).thenReturn(user);

        File file = new File("song/All The Things She Said.mp3");

        byte[] bytes = Files.readAllBytes(file.toPath());

        MultipartFile multipartFile = new MockMultipartFile("file", file.getName(), "audio/mpeg", bytes);

        List<MultipartFile> multipartFiles = new ArrayList<>();
        multipartFiles.add(multipartFile);

        SongRequest songRequest = new SongRequest();
        songRequest.setArtistName("test_artist");
        songRequest.setAlbumName("test_album");

        Song song = new Song();

        song.setId(1L);
        song.setCreatedAt(LocalDateTime.now());
        song.setCreator(user);
        song.setArtistName(songRequest.getArtistName());
        song.setAlbumName(songRequest.getAlbumName());
        song.setContentType(multipartFile.getContentType());
        song.setFileName(multipartFile.getOriginalFilename());

        List<Song> songList = new ArrayList<>();
        songList.add(song);

        SongDTO songDTO = new SongDTO(song.getId(), song.getArtistName(), song.getAlbumName(), song.getViews(), song.getCreatedAt(), song.getContentType(), song.getFileName(), song.getDuration(), song.getType());

        List<SongDTO> songDTOList = new ArrayList<>();
        songDTOList.add(songDTO);

        when(songRepository.saveAll(any())).thenReturn(songList);
        when(songMapper.songToSongDTOList(any())).thenReturn(songDTOList);

        List<SongDTO> result = songService.save(songRequest, multipartFiles);

        assertNotNull(result);

        assertEquals(songDTOList.size(), result.size());

        verify(songRepository).saveAll(anyList());
        verify(songMapper).songToSongDTOList(any());
    }

    @Test
    public void deleteSong_shouldDeletedSong() {
        User user = new User();
        user.setId(1L);
        user.setEmail("test@test.com");

        when(userService.getCurrentUser()).thenReturn(user);

        Song song = new Song();
        song.setId(1L);
        song.setCreator(user);

        Set<Song> songSet = new HashSet<>();
        songSet.add(song);

        Playlist playlist = new Playlist();
        playlist.setId(1L);
        playlist.setSongs(songSet);

        List<Playlist> playlists = new ArrayList<>();
        playlists.add(playlist);

        when(songRepository.findById(anyLong())).thenReturn(Optional.of(song));
        when(playlistRepository.findAllBySongsId(anyLong())).thenReturn(playlists);
        doNothing().when(playlistRepository).deleteAll(playlists);
        doNothing().when(songRepository).deleteById(anyLong());

        songService.deleteSongById(song.getId());

        verify(playlistRepository).deleteAll(anyList());
        verify(songRepository).deleteById(anyLong());
    }

    @Test
    public void deleteSong_shouldNotFoundSong() {
        when(songRepository.findById(anyLong())).thenThrow(new SongNotFoundException("Song not found with 1"));

        SongNotFoundException exception = assertThrows(SongNotFoundException.class, () -> songService.deleteSongById(1L));

        assertEquals("Song not found with 1", exception.getMessage());
    }

    @Test
    public void deleteSong_shouldNotPermissionsToDeleteSong() {
        User user = new User();
        user.setId(100L);

        User currentUser = new User();
        currentUser.setId(1L);

        when(userService.getCurrentUser()).thenReturn(currentUser);

        Song song = new Song();
        song.setId(1L);
        song.setCreator(user);

        when(songRepository.findById(anyLong())).thenReturn(Optional.of(song));

        UserNotEnoughPermissionsException exception = assertThrows(UserNotEnoughPermissionsException.class, () -> songService.deleteSongById(1L));

        assertEquals("You do not have enough permissions to delete this song", exception.getMessage());
    }

    @Test
    public void getTopSongs_shouldReturnTopSongs() {
        Song song = new Song();
        song.setId(1L);
        song.setViews(5);

        Song song1 = new Song();
        song1.setId(2L);
        song1.setViews(7);

        Song song2 = new Song();
        song2.setId(3L);
        song2.setViews(2);

        List<Song> songList = List.of(song, song1, song2);

        when(songRepository.findAll()).thenReturn(songList);
        when(songMapper.songToSongDTOList(anyList())).thenAnswer(i -> {
            List<Song> songs = i.getArgument(0);

            return songs.stream().map(s -> new SongDTO(s.getId(), null, null, s.getViews(), null, null, null, 0, null)).collect(Collectors.toList());
        });

        List<SongDTO> result = songService.getTopSongs();

        assertEquals(7, result.get(0).views());
        assertEquals(5, result.get(1).views());
        assertEquals(2, result.get(2).views());

        verify(songMapper).songToSongDTOList(anyList());
    }
}