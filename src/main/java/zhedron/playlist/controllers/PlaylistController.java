package zhedron.playlist.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import zhedron.playlist.dto.PlaylistDTO;
import zhedron.playlist.dto.response.MessageResponse;
import zhedron.playlist.services.PlaylistService;

import java.util.List;

@RestController
@RequestMapping("/playlist")
@Tag(name = "Playlist API", description = "This API contains add song in playlist user")
public class PlaylistController {
    private final PlaylistService playlistService;

    public PlaylistController(PlaylistService playlistService) {
        this.playlistService = playlistService;
    }

    @PostMapping("/add/{id}")
    @SecurityRequirement(name = "Playlist")
    @Operation(summary = "Add song", description = "Add song in playlist user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully added song in playlist user",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(type = "object", example = "{\"message\": \"Playlist added\"}"))),
            @ApiResponse(responseCode = "404", description = "Not found a song",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(type = "object", example = "{\"error\": \"Song not found with {id}\"}")))
    })
    public ResponseEntity<MessageResponse> addPlayList(@PathVariable long id, @RequestParam(name = "public") boolean isPublic) {
        playlistService.addSong(id, isPublic);

        return ResponseEntity.ok(new MessageResponse("Playlist added"));
    }

    @GetMapping("/search")
    @Operation(summary = "Find artist or album songs", description = "If found artist or album, then get songs from artist or album in playlist user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully found songs artist or album from playlist user",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            array = @ArraySchema(schema = @Schema(implementation = PlaylistDTO.class))
                    )),
            @ApiResponse(responseCode = "404", description = "Not found artist or album",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(type = "object"), examples = {
                            @ExampleObject(
                                    name = "Not found a album or artist",
                                    value = "{\"message\": \"Artist name or album name not found\"}",
                                    summary = "Find a album or artist in playlist user"
                            ),
                            @ExampleObject(
                                    name = "Not found a user",
                                    value = "{\"message\": \"User not found with {id}\"}",
                                    summary = "return user if exists"
                            )
                    }))
    })
    public ResponseEntity<List<PlaylistDTO>> findByArtistNameOrAlbumName(@RequestParam(required = false) String artistName,
                                                                         @RequestParam(required = false) String albumName,
                                                                         @RequestParam long userId) {
        return ResponseEntity.ok(playlistService.getPlaylistsByArtistNameOrAlbumName(artistName, albumName, userId));
    }

    @PostMapping("/available/{playlistId}")
    public ResponseEntity<MessageResponse> changeAvailable(@PathVariable long playlistId, @RequestParam("public") boolean isPublic) {
        playlistService.changeAvailable(playlistId, isPublic);

        return ResponseEntity.ok(new MessageResponse("Changed available " + isPublic));
    }
}
