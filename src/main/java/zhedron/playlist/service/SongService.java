package zhedron.playlist.service;

import org.springframework.web.multipart.MultipartFile;
import zhedron.playlist.dto.SongDTO;
import zhedron.playlist.entity.Song;

import java.io.IOException;
import java.util.List;

public interface SongService {
    SongDTO save(Song song, MultipartFile multipartFile) throws IOException;

    Song getSongById(long id);

    void deleteSongById(long id);

    List<SongDTO> getTopSongs();
}
