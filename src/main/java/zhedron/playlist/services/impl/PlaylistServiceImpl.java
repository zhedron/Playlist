package zhedron.playlist.services.impl;

import org.springframework.stereotype.Service;
import zhedron.playlist.dto.PlaylistDTO;
import zhedron.playlist.entity.Playlist;
import zhedron.playlist.entity.Song;
import zhedron.playlist.entity.User;
import zhedron.playlist.exceptions.AccessDeniedException;
import zhedron.playlist.exceptions.PlaylistNotFoundException;
import zhedron.playlist.exceptions.UserNotFoundException;
import zhedron.playlist.mapper.PlaylistMapper;
import zhedron.playlist.repository.PlaylistRepository;
import zhedron.playlist.repository.UserRepository;
import zhedron.playlist.services.PlaylistService;
import zhedron.playlist.services.SongService;
import zhedron.playlist.services.UserService;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class PlaylistServiceImpl implements PlaylistService {
    private final PlaylistRepository playlistRepository;
    private final UserRepository userRepository;
    private final SongService songService;
    private final UserService userService;
    private final PlaylistMapper playlistMapper;

    public PlaylistServiceImpl(PlaylistRepository playlistRepository, UserRepository userRepository, SongService songService, UserService userService, PlaylistMapper playlistMapper) {
        this.playlistRepository = playlistRepository;
        this.userRepository = userRepository;
        this.songService = songService;
        this.userService = userService;
        this.playlistMapper = playlistMapper;
    }

    @Override
    public void addSong(long idSong, boolean isPublic) {
        Song song = songService.getSongById(idSong);

        User user = userService.getCurrentUser();

        Playlist playlist = playlistRepository.findByUser(user);

        if (playlist == null) {
            playlist = new Playlist();

            playlist.getSongs().add(song);
            playlist.setUser(user);
            playlist.setDuration(song.getDuration());
            playlist.setPublic(isPublic);
            playlist.setCreatedAt(LocalDateTime.now());
        } else {
            playlist.getSongs().add(song);
            playlist.setUser(user);
            playlist.setDuration(playlist.getDuration() + song.getDuration());
            playlist.setCreatedAt(LocalDateTime.now());
        }

        playlist.setCounter(playlist.getSongs().size());

        playlistRepository.save(playlist);

        user.getPlaylists().add(playlist);

        userRepository.save(user);
    }

    @Override
    public List<PlaylistDTO> getPlaylistsByArtistNameOrAlbumName(String artistName, String albumName, long userId) {
        if (!userRepository.existsById(userId)) {
            throw new UserNotFoundException("User not found with " + userId);
        }

        if (!playlistRepository.existsPlaylistByArtistNameOrAlbumNameAndUserId(artistName, albumName, userId)) {
            throw new PlaylistNotFoundException("Playlist not found with " + artistName + " " + albumName);
        }

        List<Playlist> playlists = playlistRepository.findByArtistNameOrAlbumNameAndUserId(artistName, albumName, userId);

        return playlistMapper.toPlaylistDTO(playlists);

    }

    @Override
    public void changeAvailable(long playlistId, boolean isPublic) {
        Playlist playlist = playlistRepository.findById(playlistId).orElseThrow(() -> new PlaylistNotFoundException("Playlist not found with " + playlistId));

        User currentUser = userService.getCurrentUser();

        if (!playlist.getUser().equals(currentUser)) {
            throw new AccessDeniedException("You are not allowed to change this playlist");
        } else if (playlist.isPublic() == isPublic) {
            return;
        }

        playlist.setPublic(isPublic);

        playlistRepository.save(playlist);
    }
}
