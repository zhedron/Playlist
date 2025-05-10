package zhedron.playlist.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import zhedron.playlist.dto.SongDTO;
import zhedron.playlist.entity.Song;
import zhedron.playlist.mappers.SongMapper;
import zhedron.playlist.service.SongService;

@RestController
@RequestMapping("/song")
public class SongController {
    private final SongService songService;
    private final SongMapper songMapper;

    @Autowired
    public SongController(SongService songService, SongMapper songMapper) {
        this.songService = songService;
        this.songMapper = songMapper;
    }

    @PostMapping("/create")
    public ResponseEntity<SongDTO> createSong(@RequestBody Song song) {
        Song createdSong = songService.save(song);

        SongDTO songDTO = songMapper.songToSongDTO(createdSong);

        return ResponseEntity.ok(songDTO);
    }
}
