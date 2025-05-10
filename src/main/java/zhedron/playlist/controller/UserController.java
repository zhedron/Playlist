package zhedron.playlist.controller;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import zhedron.playlist.dto.PlaylistDTO;
import zhedron.playlist.dto.UserDTO;
import zhedron.playlist.entity.User;
import zhedron.playlist.mappers.UserMapper;
import zhedron.playlist.service.UserService;

import java.util.List;

@RestController
@RequestMapping("/user")
public class UserController {
    private final UserService userService;
    private final UserMapper userMapper;

    @Autowired
    public UserController(UserService userService, UserMapper userMapper) {
        this.userService = userService;
        this.userMapper = userMapper;
    }

    @PostMapping("/registration")
    public ResponseEntity<UserDTO> save(@RequestBody User user) {
        User userSaved = userService.save(user);

        UserDTO userDTO = userMapper.userToUserDTO(userSaved);

        return ResponseEntity.ok(userDTO);
    }

    @GetMapping("{id}")
    public UserDTO getUser(@PathVariable long id) {
        return userService.getById(id);
    }

    @GetMapping("/playlists/{userId}")
    public List<PlaylistDTO> getPlaylists(@PathVariable long userId) {
        return userService.getPlaylists(userId);
    }

    @GetMapping("/playlists")
    public List<PlaylistDTO> getPlaylistsByArtistNameOrAlbumName(@RequestParam(required = false) String artistName, @RequestParam(required = false) String albumName) {
        return userService.getPlaylistsByArtistNameOrAlbumName(artistName, albumName);
    }
}
