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
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import zhedron.playlist.dto.PlaylistDTO;
import zhedron.playlist.dto.request.PlaylistRequest;
import zhedron.playlist.dto.response.MessageResponse;
import zhedron.playlist.services.PlaylistService;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/playlist")
@Tag(name = "Playlist API", description = "This API contains add song in playlist user")
public class PlaylistController {
    private final PlaylistService playlistService;

    public PlaylistController(PlaylistService playlistService) {
        this.playlistService = playlistService;
    }

    @PostMapping("/add/{songId}/{playlistId}")
    @SecurityRequirement(name = "Playlist")
    @Operation(summary = "Add song", description = "Add song in playlist user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully added song in playlist user",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(type = "object", example = "{\"message\": \"Playlist added\"}"))),
            @ApiResponse(responseCode = "404", description = "Not found a song",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(type = "object", example = "{\"message\": \"Song not found with {id}\"}"))),
            @ApiResponse(responseCode = "403", description = "Can't song to playlist",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(type = "object", example = "{\"message\": \"You're can't add this song to this playlist\"}")))
    })
    public ResponseEntity<MessageResponse> addPlayList(@PathVariable long songId, @RequestParam(name = "public") boolean isPublic, @PathVariable long playlistId) {
        playlistService.addSong(songId, isPublic, playlistId);

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
    @SecurityRequirement(name = "Playlist")
    @Operation(summary = "Change to public or private playlist")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully changed available",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(type = "object", example = "{\"message\": \"Changed available {isPublic}\"}"))),
            @ApiResponse(responseCode = "404", description = "Not found playlist",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(type = "object", example = "{\"message\": \"Playlist not found with {playlistId}\"}"))),
            @ApiResponse(responseCode = "403", description = "Forbidden",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(type = "object", example = "{\"message\": \"You're can't change this playlist\"}")))
    })
    public ResponseEntity<MessageResponse> changeAvailable(@PathVariable long playlistId, @RequestParam("public") boolean isPublic) {
        playlistService.changeAvailable(playlistId, isPublic);

        return ResponseEntity.ok(new MessageResponse("Changed available " + isPublic));
    }

    @PostMapping(value = "/create")
    @SecurityRequirement(name = "Playlist")
    @Operation(summary = "User create playlist")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Playlist created successfully",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(type = "object", example = "{\"message\": \"Playlist created\"}"))),
            @ApiResponse(responseCode = "400", description = "Invalid file type",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(type = "object", example = "{\"message\": \"Upload image\"}")))
    })
    public ResponseEntity<MessageResponse> createPlaylist(@RequestPart PlaylistRequest playlistRequest, @RequestPart MultipartFile file) throws IOException {
        if (!file.isEmpty() && !(file.getContentType().equals("image/jpeg") || file.getContentType().equals("image/png"))) {
            return ResponseEntity.badRequest().body(new MessageResponse("Upload image"));
        }

        playlistService.savePlaylist(playlistRequest, file);

        return ResponseEntity.status(HttpStatus.CREATED).body(new MessageResponse("Playlist created"));
    }

    @DeleteMapping("/delete/{id}")
    @SecurityRequirement(name = "Playlist")
    @Operation(summary = "Delete playlist")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully deleted playlist",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(type = "object", example = "{\"message\": \"Playlist deleted successfully\"}"))),
            @ApiResponse(responseCode = "404", description = "Not found playlist",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(type = "object", example = "{\"message\": \"Playlist not found with {id}\"}"))),
            @ApiResponse(responseCode = "403", description = "You can't delete playlist",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(type = "object", example = "{\"message\": \"You can't delete this playlist\"}")))
    })
    public ResponseEntity<MessageResponse> deletePlaylist(@PathVariable long id) {
        playlistService.deletePlaylist(id);

        return ResponseEntity.ok(new MessageResponse("Playlist deleted successfully"));
    }
}
