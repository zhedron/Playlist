package zhedron.playlist.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import zhedron.playlist.service.PlaylistService;

@RestController
@RequestMapping("/playlist")
public class PlaylistController {
    private final PlaylistService playlistService;

    public PlaylistController(PlaylistService playlistService) {
        this.playlistService = playlistService;
    }

    @PostMapping("/add/{id}")
    public ResponseEntity<String> addPlayList(@PathVariable long id) {
        playlistService.addSong(id);

        return ResponseEntity.ok("Playlist added");
    }
}
