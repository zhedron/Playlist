package zhedron.playlist.services;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import zhedron.playlist.dto.PlaylistDTO;
import zhedron.playlist.dto.SongDTO;
import zhedron.playlist.entity.Playlist;
import zhedron.playlist.entity.Song;
import zhedron.playlist.entity.User;
import zhedron.playlist.exceptions.PlaylistNotFoundException;
import zhedron.playlist.exceptions.UserNotFoundException;
import zhedron.playlist.mapper.PlaylistMapper;
import zhedron.playlist.repository.PlaylistRepository;
import zhedron.playlist.repository.UserRepository;
import zhedron.playlist.services.impl.PlaylistServiceImpl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PlaylistServiceTest {

    @InjectMocks
    private PlaylistServiceImpl playlistService;

    @Mock
    private PlaylistRepository playlistRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private SongService songService;

    @Mock
    private UserService userService;

    @Mock
    private PlaylistMapper playlistMapper;

    @Test
    void addSongShouldCreatePlaylistWhenUserDoesNotHaveOne() {
        User user = new User();
        user.setId(1L);
        user.setEmail("test@test.com");
        user.setPlaylists(new java.util.ArrayList<>());

        Song song = new Song();
        song.setId(5L);
        song.setDuration(180);

        when(songService.getSongById(5L)).thenReturn(song);
        when(userService.getCurrentUser()).thenReturn(user);
        when(playlistRepository.findByUser(user)).thenReturn(null);

        playlistService.addSong(5L, true);

        assertEquals(1, user.getPlaylists().size());
        assertEquals(1, user.getPlaylists().get(0).getSongs().size());
        assertTrue(user.getPlaylists().get(0).isPublic());
        verify(playlistRepository).save(user.getPlaylists().get(0));
        verify(userRepository).save(user);
    }

    @Test
    void getPlaylistsByArtistNameOrAlbumNameShouldReturnMappedPlaylists() {
        SongDTO songDTO = new SongDTO(1L, "artist", "album", 10L, LocalDateTime.now(), null, null, 120, null, null, null, 1L);
        PlaylistDTO playlistDTO = new PlaylistDTO(2L, Set.of(songDTO), 10L, 120L, true, 1, LocalDateTime.now());
        Playlist playlist = new Playlist();

        when(userRepository.existsById(1L)).thenReturn(true);
        when(playlistRepository.existsPlaylistByArtistNameOrAlbumNameAndUserId("artist", "album", 1L)).thenReturn(true);
        when(playlistRepository.findByArtistNameOrAlbumNameAndUserId("artist", "album", 1L)).thenReturn(List.of(playlist));
        when(playlistMapper.toPlaylistDTO(List.of(playlist))).thenReturn(List.of(playlistDTO));

        List<PlaylistDTO> result = playlistService.getPlaylistsByArtistNameOrAlbumName("artist", "album", 1L);

        assertEquals(1, result.size());
        assertEquals("artist", result.get(0).songs().iterator().next().artistName());
    }

    @Test
    void getPlaylistsByArtistNameOrAlbumNameShouldThrowWhenUserMissing() {
        when(userRepository.existsById(1L)).thenReturn(false);

        UserNotFoundException exception = assertThrows(
                UserNotFoundException.class,
                () -> playlistService.getPlaylistsByArtistNameOrAlbumName("artist", "album", 1L)
        );

        assertEquals("User not found with 1", exception.getMessage());
    }

    @Test
    void getPlaylistsByArtistNameOrAlbumNameShouldThrowWhenPlaylistMissing() {
        when(userRepository.existsById(1L)).thenReturn(true);
        when(playlistRepository.existsPlaylistByArtistNameOrAlbumNameAndUserId("artist", "album", 1L)).thenReturn(false);

        PlaylistNotFoundException exception = assertThrows(
                PlaylistNotFoundException.class,
                () -> playlistService.getPlaylistsByArtistNameOrAlbumName("artist", "album", 1L)
        );

        assertEquals("Playlist not found with artist album", exception.getMessage());
    }
}
