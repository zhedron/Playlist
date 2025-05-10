package zhedron.playlist.service.impl;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import zhedron.playlist.entity.Playlist;
import zhedron.playlist.entity.Song;
import zhedron.playlist.entity.User;
import zhedron.playlist.repository.PlaylistRepository;
import zhedron.playlist.repository.UserRepository;
import zhedron.playlist.service.PlaylistService;
import zhedron.playlist.service.SongService;
import zhedron.playlist.service.UserService;

@Service
public class PlaylistServiceImpl implements PlaylistService {
    private final PlaylistRepository playlistRepository;
    private final UserRepository userRepository;
    private final SongService songService;
    private final UserService userService;

    public PlaylistServiceImpl(PlaylistRepository playlistRepository, UserRepository userRepository, SongService songService, UserService userService) {
        this.playlistRepository = playlistRepository;
        this.userRepository = userRepository;
        this.songService = songService;
        this.userService = userService;
    }

    @Override
    public void addSong(long idSong) {
        Song song = songService.getSongById(idSong);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        User user = userService.findByEmail(authentication.getName());

        Playlist playlist = new Playlist();

        playlist.setArtistName(song.getArtistName());
        playlist.setAlbumName(song.getAlbumName());
        playlist.setUser(user);

        playlistRepository.save(playlist);

        user.getPlaylists().add(playlist);

        userRepository.save(user);
    }
}
