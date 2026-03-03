package zhedron.playlist.service;

import org.springframework.web.multipart.MultipartFile;
import zhedron.playlist.dto.SongDTO;
import zhedron.playlist.dto.request.SongRequest;
import zhedron.playlist.dto.response.PaginatedResponse;
import zhedron.playlist.entity.Song;

import java.io.IOException;
import java.util.List;

public interface SongService {
    List<SongDTO> save(SongRequest requestSong, List<MultipartFile> files) throws IOException;

    Song getSongById(long id);

    void deleteSongById(long id);

    List<SongDTO> getTopSongs();

    PaginatedResponse findAllPerWeek(int page, int size);
}
