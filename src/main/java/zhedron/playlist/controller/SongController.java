package zhedron.playlist.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.*;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import zhedron.playlist.dto.SongDTO;
import zhedron.playlist.dto.request.SongRequest;
import zhedron.playlist.dto.response.MessageResponse;
import zhedron.playlist.dto.response.PaginatedResponse;
import zhedron.playlist.entity.Song;
import zhedron.playlist.mapper.SongMapper;
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
@Tag(name = "Song API", description = "This API contains create, update, delete, play song and top 10 songs.")
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

    @PostMapping(value = "/create", consumes =  MediaType.MULTIPART_FORM_DATA_VALUE)
    @SecurityRequirement(name = "Playlist")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(content = @Content(encoding = @Encoding(name = "requestSong", contentType = MediaType.APPLICATION_JSON_VALUE)))
    @Operation(summary = "Create song", description = "Create song and upload song")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully uploaded song and created",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = SongDTO.class))),
            @ApiResponse(responseCode = "400", description = "Not successfully create song or upload song",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(type = "object"), examples = {
                    @ExampleObject(
                            name = "Empty name",
                            value = "{\"error\": \"Name must not be empty\"}",
                            summary = "Name must not be empty"
                    ),
                    @ExampleObject(
                            name = "Null name",
                            value = "{\"error\": \"Name must not be null\"}",
                            summary = "Null name"
                    ),
                    @ExampleObject(
                            name = "Empty album",
                            value = "{\"error\": \"Album must not be empty\"}",
                            summary = "Album"
                    ),
                    @ExampleObject(
                            name = "Invalid content type",
                            value = "{\"message\": \"Upload audio file.\"}",
                            summary = "If file uploaded does not contain audio/mp4 or audio/mpeg"
                    )
            }))
    })
    public ResponseEntity<?> createSong(@Valid @RequestPart SongRequest requestSong, BindingResult result, @RequestPart List<MultipartFile> files) throws IOException {
        if (result.hasErrors()) {
            Map<String, String> errors = new HashMap<>();

            for (FieldError error : result.getFieldErrors()) {
                errors.put("error", error.getDefaultMessage());
            }
            return ResponseEntity.badRequest().body(errors);
        }
        for (MultipartFile file : files) {
            if (!(file.getContentType().equals("audio/mp4") || file.getContentType().equals("audio/mpeg")) || file.isEmpty()) {
                return ResponseEntity.badRequest().body(new MessageResponse("Upload audio file."));
            }
        }

        return ResponseEntity.ok(songService.save(requestSong, files));
    }

    @DeleteMapping("/delete/{id}")
    @SecurityRequirement(name = "Playlist")
    @Operation(summary = "Delete song", description = "Delete song from application")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully deleted song",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(type = "object", example = "{\"message\": \"Song {id} deleted\"}"))),
            @ApiResponse(responseCode = "404", description = "Not found a song or user",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(type = "object", example = "{\"message\": \"Song not found with {id}\"}"))),
            @ApiResponse(responseCode = "403", description = "User didn't log in")
    })
    public ResponseEntity<MessageResponse> deleteSong(@PathVariable long id) {
        songService.deleteSongById(id);

        return ResponseEntity.ok(new MessageResponse("Song " + id + " deleted"));
    }

    @GetMapping("/top")
    @Operation(summary = "Get 10 top songs", description = "Get 10 top songs by views")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Got top 10 songs",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, array = @ArraySchema(schema = @Schema(implementation = SongDTO.class)))),
    })
    public List<SongDTO> topSongs() {
        return songService.getTopSongs();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get song", description = "Get song by id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully found song",
            content = @Content(schema = @Schema(implementation = SongDTO.class))),
            @ApiResponse(responseCode = "404", description = "Not found a song",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(type = "object", example = "{\"message\": \"Song not found with {id}\"}")))
    })
    public SongDTO findSongById(@PathVariable long id) {
        Song song = songService.getSongById(id);

        song.setViews(song.getViews() + 1);

        songRepository.save(song);

        return songMapper.songToSongDTO(song);
    }

    @GetMapping("/file/{id}")
    @Operation(summary = "Display a song", description = "Display a song and play")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully found song and play",
            content = @Content(mediaType = "audio/mpeg", schema = @Schema(type = "string", format = "byte"))),
            @ApiResponse(responseCode = "404", description = "Not found a song",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(type = "object", example = "{\"message\": \"Song not found with {id}\"}"))),
            @ApiResponse(responseCode = "400", description = "A file not loaded",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(type = "object", example = "{\"message\": \"Cannot read file.\"}")))
    })
    public ResponseEntity<?> getSongById (@PathVariable long id) {
        Song song = songService.getSongById(id);

        try {
            File path = new File(PATH + song.getFileName());

            byte[] file = Files.readAllBytes(path.toPath());

            return ResponseEntity.ok().contentType(MediaType.parseMediaType(song.getContentType())).body(file);
        } catch (IOException e) {
            return ResponseEntity.badRequest().body(new MessageResponse("Cannot read file."));
        }
    }

    @GetMapping("/perweek")
    @Operation(summary = "Get songs per week", description = "Get songs per page and week")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully got all songs",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = PaginatedResponse.class)))
    })
    public ResponseEntity<PaginatedResponse> findAllPerWeek(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(songService.findAllPerWeek(page, size));
    }
}
