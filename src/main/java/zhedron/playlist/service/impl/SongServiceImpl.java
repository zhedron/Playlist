package zhedron.playlist.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import zhedron.playlist.entity.Song;
import zhedron.playlist.entity.User;
import zhedron.playlist.exceptions.SongNotFoundException;
import zhedron.playlist.repository.SongRepository;
import zhedron.playlist.service.SongService;
import zhedron.playlist.service.UserService;

@Service
@Slf4j
public class SongServiceImpl implements SongService {
    private final SongRepository songRepository;
    private final UserService userService;

    public SongServiceImpl(SongRepository songRepository, UserService userService) {
        this.songRepository = songRepository;
        this.userService = userService;
    }

    @Override
    public Song save(Song song) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        String email = authentication.getName();

        User foundUser = userService.findByEmail(email);

        song.setCreator(foundUser);

        log.info("Saved song: {}", song);

        return songRepository.save(song);
    }

    @Override
    public Song getSongById(long id) {
        return songRepository.findById(id).orElseThrow(() -> new SongNotFoundException("Song not found with " + id));
    }
}
