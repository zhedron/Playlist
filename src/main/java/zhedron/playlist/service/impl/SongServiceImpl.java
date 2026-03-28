package zhedron.playlist.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.AudioHeader;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import zhedron.playlist.dto.SongDTO;
import zhedron.playlist.dto.request.SongRequest;
import zhedron.playlist.dto.response.PaginatedResponse;
import zhedron.playlist.entity.Playlist;
import zhedron.playlist.entity.Song;
import zhedron.playlist.entity.User;
import zhedron.playlist.enums.Role;
import zhedron.playlist.enums.Type;
import zhedron.playlist.exception.SongNotFoundException;
import zhedron.playlist.exception.UserNotEnoughPermissionsException;
import zhedron.playlist.mapper.SongMapper;
import zhedron.playlist.repository.PlaylistRepository;
import zhedron.playlist.repository.SongRepository;
import zhedron.playlist.service.SongService;
import zhedron.playlist.service.UserService;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

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
    public List<SongDTO> save(SongRequest requestSong, List<MultipartFile> files) throws IOException {
        Path path = Paths.get(FILEPATH);

        List<Song> songList = new ArrayList<>();

        if (Files.notExists(path)) {
            Files.createDirectories(path);
        }

        for (MultipartFile multipartFile : files) {
            User currentUser = userService.getCurrentUser();

            Type type = files.size() > 1 ? Type.ALBUM : Type.SINGLE;

            String songName = currentUser.getId() + "_" + requestSong.getArtistName() + "_" + requestSong.getAlbumName() + "_" + multipartFile.getOriginalFilename();

            Path createFile = Paths.get(FILEPATH).resolve(songName).normalize();

            Files.copy(multipartFile.getInputStream(), createFile, StandardCopyOption.REPLACE_EXISTING);

            Song song = new Song();

            song.setCreator(currentUser);
            song.setContentType(multipartFile.getContentType());
            song.setFileName(songName);
            song.setCreatedAt(LocalDateTime.now());
            song.setAlbumName(requestSong.getAlbumName());
            song.setArtistName(requestSong.getArtistName());
            song.setType(type);

            songList.add(song);

            log.info("Saved song: {}", song);

            try {
                AudioFile audioFile = AudioFileIO.read(createFile.toFile());
                AudioHeader audioHeader = audioFile.getAudioHeader();

                song.setDuration(audioHeader.getTrackLength());
            } catch (Exception e) {
                log.error("Audio file error {}", e.getMessage());
            }
        }

        List<Song> savedSong = songRepository.saveAll(songList);

        return songMapper.songToSongDTOList(savedSong);
    }

    @Override
    public Song getSongById(long id) {
        return songRepository.findById(id).orElseThrow(() -> new SongNotFoundException("Song not found with " + id));
    }

    @Override
    public void deleteSongById(long id) {
        Song song = getSongById(id);

        User currentUser = userService.getCurrentUser();

        if (song.getCreator().getId() != currentUser.getId() && !currentUser.getRole().equals(Role.ADMIN)) {
            throw new UserNotEnoughPermissionsException("You do not have enough permissions to delete this song");
        }

        List<Playlist> playlists = playlistRepository.findAllBySongsId(song.getId());

        playlistRepository.deleteAll(playlists);

        songRepository.deleteById(id);
    }

    @Override
    public List<SongDTO> getTopSongs() {
        List<Song> songs = songRepository.findAll();

        List<Song> topSongs = songs.stream().sorted(Comparator.comparingLong(Song::getViews).reversed()).limit(10).collect(Collectors.toList());

        return songMapper.songToSongDTOList(topSongs);
    }

    @Override
    public PaginatedResponse findAllPerWeek(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("views").descending());

        Page<Song> songPage = songRepository.findAll(pageable);

        List<Song> findAllPerWeek = songPage.getContent().stream().filter(s -> s.getCreatedAt().isAfter(LocalDateTime.now().minusWeeks(1))).collect(Collectors.toList());

        List<SongDTO> songResponses = findAllPerWeek.stream().map(songMapper::songToSongDTO).collect(Collectors.toList());

        return new PaginatedResponse(
                songResponses,
                songPage.getNumber(),
                songPage.getSize(),
                songPage.getTotalElements(),
                songPage.getTotalPages(),
                songPage.isLast(),
                songPage.isFirst(),
                songPage.hasNext(),
                songPage.hasPrevious()
        );
    }
}
