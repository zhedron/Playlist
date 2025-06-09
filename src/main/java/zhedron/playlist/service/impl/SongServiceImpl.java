package zhedron.playlist.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.AudioHeader;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import zhedron.playlist.dto.SongDTO;
import zhedron.playlist.entity.Playlist;
import zhedron.playlist.entity.Song;
import zhedron.playlist.entity.User;
import zhedron.playlist.enums.Role;
import zhedron.playlist.exceptions.SongNotFoundException;
import zhedron.playlist.exceptions.UserNotEnoughPermissionsException;
import zhedron.playlist.mappers.SongMapper;
import zhedron.playlist.repository.PlaylistRepository;
import zhedron.playlist.repository.SongRepository;
import zhedron.playlist.service.SongService;
import zhedron.playlist.service.UserService;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

@Service
@Slf4j
public class SongServiceImpl implements SongService {
    private final SongRepository songRepository;
    private final UserService userService;
    private final PlaylistRepository playlistRepository;
    private final SongMapper songMapper;

    private final String FILEPATH = "song/";

    public SongServiceImpl(SongRepository songRepository, UserService userService, PlaylistRepository playlistRepository, SongMapper songMapper) {
        this.songRepository = songRepository;
        this.userService = userService;
        this.playlistRepository = playlistRepository;
        this.songMapper = songMapper;
    }

    @Override
    public SongDTO save(Song song, MultipartFile multipartFile) throws IOException {
        File file = new File(FILEPATH);

        if (!file.exists()) {
            file.mkdir();
        }

        String songName = multipartFile.getOriginalFilename();

        String path = FILEPATH + songName;

        File createFile = new File(path);

        User currentUser = userService.getCurrentUser();

        multipartFile.transferTo(createFile.toPath());

        song.setCreator(currentUser);
        song.setContentType(multipartFile.getContentType());
        song.setFileName(songName);
        song.setCreatedAt(LocalDateTime.now());

        try {
            AudioFile audioFile = AudioFileIO.read(createFile);
            AudioHeader audioHeader = audioFile.getAudioHeader();

            song.setDuration(audioHeader.getTrackLength());
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

        log.info("Saved song: {}", song);

        Song songSaved = songRepository.save(song);

        return songMapper.songToSongDTO(songSaved);
    }

    @Override
    public Song getSongById(long id) {
        return songRepository.findById(id).orElseThrow(() -> new SongNotFoundException("Song not found with " + id));
    }

    @Override
    public void deleteSongById(long id) {
        Song song = getSongById(id);

        User currentUser = userService.getCurrentUser();

        if (song.getCreator().getId() != currentUser.getId() || !currentUser.getRole().equals(Role.ADMIN)) {
            throw new UserNotEnoughPermissionsException("You do not have enough permissions to delete this song");
        }

        List<Playlist> playlists = playlistRepository.findAllBySongsId(song.getId());

        playlistRepository.deleteAll(playlists);

        songRepository.deleteById(id);
    }

    @Override
    public List<SongDTO> getTopSongs() {
        List<Song> songs = songRepository.findAll();

        List<Song> topSongs = songs.stream().sorted(Comparator.comparingDouble(Song::getViews).reversed()).limit(10).toList();

        return songMapper.songToSongDTOList(topSongs);
    }
}
