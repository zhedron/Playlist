package zhedron.playlist.controller;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import zhedron.playlist.dto.SongDTO;
import zhedron.playlist.entity.Song;
import zhedron.playlist.mappers.SongMapper;
import zhedron.playlist.repository.SongRepository;
import zhedron.playlist.service.SongService;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/song")
public class SongController {
    private final SongService songService;
    private final SongMapper songMapper;
    private final SongRepository songRepository;

    private final String PATH = "song/";

    @Autowired
    public SongController(SongService songService, SongMapper songMapper, SongRepository songRepository) {
        this.songService = songService;
        this.songMapper = songMapper;
        this.songRepository = songRepository;
    }

    @PostMapping("/create")
    public ResponseEntity<?> createSong(@Valid @RequestPart Song song, @RequestPart MultipartFile multipartFile, BindingResult result) throws IOException {
        if (result.hasErrors()) {
            Map<String, String> errors = new HashMap<>();

            for (FieldError error : result.getFieldErrors()) {
                errors.put(error.getField(), error.getDefaultMessage());
            }
            return ResponseEntity.badRequest().body(errors);
        }

        System.out.println(multipartFile.getContentType());
        if (!multipartFile.getContentType().equals("audio/mp4") || !multipartFile.getContentType().equals("audio/mp3") || multipartFile.isEmpty()) {
            return ResponseEntity.badRequest().body("Upload audio file.");
        }

        return ResponseEntity.status(HttpStatus.OK).body(songService.save(song, multipartFile));
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<String> deleteSong(@PathVariable long id) {
        songService.deleteSongById(id);

        return ResponseEntity.ok("Song " + id + " deleted");
    }

    @GetMapping("/top")
    public List<SongDTO> topSongs() {
        return songService.getTopSongs();
    }

    @GetMapping("/{id}")
    public SongDTO findSongById(@PathVariable long id) {
        Song song = songService.getSongById(id);

        song.setViews(song.getViews() + 1);

        songRepository.save(song);

        return songMapper.songToSongDTO(song);
    }

    @GetMapping("/file/{id}")
    public ResponseEntity<?> getSongById (@PathVariable long id) {
        Song song = songService.getSongById(id);

        try {
            File path = new File(PATH + song.getFileName());

            byte[] file = Files.readAllBytes(path.toPath());

            return ResponseEntity.ok().contentType(MediaType.parseMediaType(song.getContentType())).body(file);
        } catch (IOException e) {
            System.out.println(e.getMessage());
            return ResponseEntity.badRequest().body("Cannot read file.");
        }
    }
}
