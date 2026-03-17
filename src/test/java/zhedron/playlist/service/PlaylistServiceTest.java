package zhedron.playlist.service;

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
import zhedron.playlist.exception.PlaylistNotFoundException;
import zhedron.playlist.exception.SongNotFoundException;
import zhedron.playlist.exception.UserNotFoundException;
import zhedron.playlist.mapper.PlaylistMapper;
import zhedron.playlist.repository.PlaylistRepository;
import zhedron.playlist.repository.UserRepository;
import zhedron.playlist.service.impl.PlaylistServiceImpl;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
public class PlaylistServiceTest {
    @InjectMocks
    private PlaylistServiceImpl playlistServiceImpl;

    @Mock
    private PlaylistRepository playlistRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserService userService;

    @Mock
    private SongService songService;

    @Mock
    private PlaylistMapper playlistMapper;

    @Test
    public void addSong_shouldReturnAddedSong() {
        User user = new User();
        user.setId(1L);
        user.setEmail("test@test.com");

        when(userService.getCurrentUser()).thenReturn(user);

        Song song = new Song();
        song.setId(1L);
        song.setDuration(15);

        Playlist playlist = new Playlist();
        playlist.setId(1L);
        playlist.setUser(user);
        playlist.getSongs().add(song);
        playlist.setPublic(true);
        playlist.setDuration(playlist.getDuration() + song.getDuration());

        List<Playlist> playlistList = new ArrayList<>();
        playlistList.add(playlist);

        user.setPlaylists(playlistList);

        when(songService.getSongById(anyLong())).thenReturn(song);
        when(playlistRepository.findByUser(any(User.class))).thenReturn(playlist);
        when(playlistRepository.save(any(Playlist.class))).thenReturn(playlist);
        when(userRepository.save(any(User.class))).thenReturn(user);

        playlistServiceImpl.addSong(song.getId(), playlist.isPublic());

        verify(playlistRepository).save(any(Playlist.class));
        verify(userRepository).save(any(User.class));
    }

    @Test
    public void addSong_shouldReturnNotFoundSong() {
        when(songService.getSongById(anyLong())).thenThrow(new SongNotFoundException("Song not found with 1"));

        SongNotFoundException exception = assertThrows(SongNotFoundException.class, () -> playlistServiceImpl.addSong(1L, false));

        assertEquals("Song not found with 1", exception.getMessage());
    }

    @Test
    public void getPlaylistsByArtistNameOrAlbumName_shouldReturnFound() {
        User user = new User();
        user.setId(1L);

        Song song = new Song();
        song.setId(1L);
        song.setArtistName("test_artist");
        song.setAlbumName("test_album");
        song.setCreator(user);

        Playlist playlist = new Playlist();
        playlist.setId(1L);
        playlist.setUser(user);
        playlist.getSongs().add(song);
        playlist.setPublic(true);

        List<Playlist> playlists = new ArrayList<>();
        playlists.add(playlist);

        user.setPlaylists(playlists);

        SongDTO songDTO = new SongDTO(song.getId(), song.getArtistName(), song.getAlbumName(), 0, null, null, null, 0, null);

        Set<SongDTO> songs = new HashSet<>();
        songs.add(songDTO);

        PlaylistDTO playlistDTO = new PlaylistDTO(playlist.getId(), songs, 0, 0, playlist.isPublic(), 0, null);

        List<PlaylistDTO> playlistsDTO = new ArrayList<>();
        playlistsDTO.add(playlistDTO);

        when(userRepository.existsById(anyLong())).thenReturn(true);
        when(playlistRepository.existsPlaylistByArtistNameOrAlbumNameAndUserId(song.getArtistName(), song.getAlbumName(), user.getId())).thenReturn(true);
        when(playlistRepository.findByArtistNameOrAlbumNameAndUserId(song.getArtistName(), song.getAlbumName(), user.getId())).thenReturn(playlists);
        when(playlistMapper.toPlaylistDTO(anyList())).thenReturn(playlistsDTO);

        List<PlaylistDTO> result = playlistServiceImpl.getPlaylistsByArtistNameOrAlbumName(song.getArtistName(), song.getAlbumName(), user.getId());

        assertNotNull(result);
        assertEquals(playlistsDTO.size(), result.size());

        verify(userRepository).existsById(anyLong());
        verify(playlistRepository).existsPlaylistByArtistNameOrAlbumNameAndUserId(song.getArtistName(), song.getAlbumName(), user.getId());
        verify(playlistRepository).findByArtistNameOrAlbumNameAndUserId(song.getArtistName(), song.getAlbumName(), user.getId());
        verify(playlistMapper).toPlaylistDTO(anyList());
    }

    @Test
    public void getPlaylistsByArtistNameOrAlbumName_shouldReturnNotFoundUser() {
        when(userRepository.existsById(anyLong())).thenThrow(new UserNotFoundException("User not found with 1"));

        UserNotFoundException exception = assertThrows(UserNotFoundException.class, () -> playlistServiceImpl.getPlaylistsByArtistNameOrAlbumName(null, null, 1L));

        assertEquals("User not found with 1", exception.getMessage());
    }

    @Test
    public void getPlaylistsByArtistNameOrAlbumName_shouldReturnNotFoundArtistAndAlbum() {
        User user = new User();
        user.setId(1L);

        when(userRepository.existsById(anyLong())).thenReturn(true);
        when(playlistRepository.existsPlaylistByArtistNameOrAlbumNameAndUserId(null, null, user.getId())).thenThrow(new PlaylistNotFoundException("Playlist not found with test_artist test_album"));

        PlaylistNotFoundException exception = assertThrows(PlaylistNotFoundException.class, () -> playlistServiceImpl.getPlaylistsByArtistNameOrAlbumName(null, null, user.getId()));

        assertEquals("Playlist not found with test_artist test_album", exception.getMessage());
    }
}
