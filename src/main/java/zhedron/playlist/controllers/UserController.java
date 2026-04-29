package zhedron.playlist.controllers;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.*;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import zhedron.playlist.dto.PlaylistDTO;
import zhedron.playlist.dto.UserDTO;
import zhedron.playlist.dto.request.UserRequest;
import zhedron.playlist.dto.request.UserUpdateRequest;
import zhedron.playlist.dto.response.MessageResponse;
import zhedron.playlist.entity.User;
import zhedron.playlist.enums.Provider;
import zhedron.playlist.enums.Role;
import zhedron.playlist.mapper.UserMapper;
import zhedron.playlist.services.AESEncryptionService;
import zhedron.playlist.services.UserService;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(value = "/user", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "User API",
        description = "This User API can create, update, delete, block user and find user or songs from user playlist")
public class UserController {
    private final UserService userService;
    private final UserMapper userMapper;
    private final AESEncryptionService aesEncryptionService;

    private final String PICTURE_PATH = "profile_image/";

    @Autowired
    public UserController(UserService userService, UserMapper userMapper, AESEncryptionService aesEncryptionService) {
        this.userService = userService;
        this.userMapper = userMapper;
        this.aesEncryptionService = aesEncryptionService;
    }

    @PostMapping(value = "/registration")
    @Operation(summary = "Create a new user", description = "Add a new user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
            description = "User created successfully",
            content = @Content(schema = @Schema(implementation = UserDTO.class))),
            @ApiResponse(responseCode = "400",
            description = "Invalid name, email, password",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(type = "object"), examples = {
                    @ExampleObject(
                            name = "Invalid name",
                            value = "{\"error\": \"Name must not be empty\"}",
                            summary = "Name"
                    ),
                    @ExampleObject(
                            name = "Invalid email",
                            value = "{\"error\": \"Write your email\"}",
                            summary = "Email"
                    ),
                    @ExampleObject(
                            name = "Invalid password",
                            value = "{\"error\": \"Password must not be null\"}",
                            summary = "Password"
                    )
            }))
    })
    public ResponseEntity<?> save(@Valid @RequestBody UserRequest requestUser, BindingResult result) {
        if (result.hasErrors()) {
            Map<String, String> errors = new HashMap<>();
            for (FieldError error : result.getFieldErrors()) {
                errors.put("error", error.getDefaultMessage());
            }
            return ResponseEntity.badRequest().body(errors);
        }

        User userSaved = userService.save(requestUser);

        UserDTO userDTO = userMapper.userToUserDTO(userSaved);

        return ResponseEntity.ok(userDTO);
    }

    @GetMapping("{id}")
    @Operation(summary = "Get a user by id", description = "If found user, then get a data user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully found user",
                    content = @Content(schema = @Schema(implementation = UserDTO.class))),
            @ApiResponse(responseCode = "404", description = "Not found a user",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(type = "object", example = "{\"message\": \"Not found a user {id}\"}")))
    })
    public UserDTO getUserById(@PathVariable long id) {
        User user = userService.getById(id);

        UserDTO userDTO = userMapper.userToUserDTO(user);

        return userDTO.phone() != null && !userDTO.isHiddenPhone() ? userDTO.getByPhone(aesEncryptionService.decrypt(userDTO.phone())) : userDTO;
    }

    @GetMapping("/playlists/{userId}")
    @Operation(summary = "Find user playlist", description = "If found user, then get a songs in user playlist")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully found user",
                    content = @Content(schema = @Schema(implementation = PlaylistDTO.class))),
            @ApiResponse(responseCode = "404", description = "Not found a user",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(type = "object", example = "{\"error\": \"Not found a user\"}")))
    })
    public List<PlaylistDTO> getPlaylists(@PathVariable long userId) {
        return userService.getPlaylists(userId);
    }

    @DeleteMapping(value = "/playlist/delete/{playlistId}/{songId}")
    @SecurityRequirement(name = "Playlist")
    @Operation(summary = "Find user playlist and song from user playlist", description = "If successfully found, then delete song from user playlist")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully deleted song from user playlist",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(type = "object", example = "{\"message\": \"Song from playlist deleted successfully\"}"))),
            @ApiResponse(responseCode = "404", description = "Not found playlist or song",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(type = "object"), examples = {
                    @ExampleObject(
                            name = "Not found playlist",
                            value = "{\"message\": \"Playlist not found with {id}\"}",
                            summary = "Playlist"
                    ),
                    @ExampleObject(
                            name = "Not found a user",
                            value = "{\"message\": \"User not found with {id}\"}",
                            summary = "User"
                    ),
                    @ExampleObject(
                            name = "Not found a song",
                            value = "{\"message\": \"Song not found with {id}\"}",
                            summary = "Song"
                    )
            }))
    })
    public ResponseEntity<MessageResponse> deleteSongFromPlaylist(@PathVariable long playlistId, @PathVariable long songId) {
        userService.deleteSongFromPlaylist(playlistId, songId);

        return ResponseEntity.ok(new MessageResponse("Song from playlist deleted successfully"));
    }

    @PutMapping("/block/{id}")
    @SecurityRequirement(name = "Playlist")
    @Operation(summary = "Block user", description = "If user blocked, then he's can't logging in application")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User blocked successfully",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(type = "object", example = "{\"message\": \"User blocked successfully\"}"))),
            @ApiResponse(responseCode = "404", description = "Not found a user",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(type = "object", example = "{\"message\": \"User not found with {id}\"}")))
    })
    public ResponseEntity<MessageResponse> block(@PathVariable long id) {
        userService.blockUser(id);

        return ResponseEntity.ok(new MessageResponse("User blocked successfully"));
    }

    @GetMapping(value = "/picture/{id}", produces = {MediaType.IMAGE_JPEG_VALUE, MediaType.IMAGE_PNG_VALUE})
    @Operation(summary = "Get a avatar user", description = "Display a avatar user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Got a avatar user",
            content = {
                    @Content(mediaType = MediaType.IMAGE_PNG_VALUE, schema = @Schema(type = "string", format = "binary")),
                    @Content(mediaType = MediaType.IMAGE_JPEG_VALUE, schema = @Schema(type = "string", format = "binary"))
            }),
            @ApiResponse(responseCode = "404", description = "Not found a user",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
            schema = @Schema(type = "object", example = "{\"error\": \"User not found with {id}\"}")))
    })
    public ResponseEntity<Resource> getUserPicture(@PathVariable long id) throws MalformedURLException {
        UserDTO userDTO = getUserById(id);

        if (userDTO.provider().equals(Provider.GOOGLE)) {
            Resource resource = userService.getProfilePicture(id);

            return ResponseEntity.ok().contentType(MediaType.parseMediaType(userDTO.contentType())).body(resource);
        } else {
            Path path = Paths.get(PICTURE_PATH).resolve(userDTO.profilePicture()).normalize();

            Resource resource = new UrlResource(path.toUri());

            return ResponseEntity.ok().contentType(MediaType.parseMediaType(userDTO.contentType())).body(resource);
        }
    }

    @PatchMapping("/update/{id}")
    @Operation(summary = "Change name, about, email, password, phone", description = "Change user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully changed",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(type = "object", example = "{\"message\": \"User updated successfully\"}"))),
            @ApiResponse(responseCode = "404", description = "Not found a user",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(type = "object", example = "{\"error\": \"Not found a user\"}"))),
            @ApiResponse(responseCode = "400", description = "Invalid email or pattern phone",
            content = @Content(schema = @Schema(type = "object"), examples = {
                    @ExampleObject(
                            name = "Email error",
                            value = "{\"error\": \"Write your email\"}",
                            summary = "Invalid error"
                    ),
                    @ExampleObject(
                            name = "Phone error",
                            value = "{\"error\": \"Write your phone number\"}",
                            summary = "Invalid phone"
                    )
            }))
    })
    @SecurityRequirement(name = "Playlist")
    public ResponseEntity<?> updateUser(@PathVariable long id, @RequestBody @Valid UserUpdateRequest userUpdate, BindingResult result) throws Exception {
        if (result.hasErrors()) {
            Map<String, String> errors = new HashMap<>();
            for (FieldError error : result.getFieldErrors()) {
                errors.put("error", error.getDefaultMessage());
            }
            return ResponseEntity.badRequest().body(errors);
        }

        userService.updateUser(userUpdate, id);

        return ResponseEntity.ok(new MessageResponse("User updated successfully"));
    }

    @PutMapping("/change_role/{userId}")
    @SecurityRequirement(name = "Playlist")
    @Operation(summary = "Change role for user", description = "If user found and admin, then this is user can change role")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully changed role for user",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(type = "object", example = "{\"message\": \"Changed role successfully.\"}"))),
            @ApiResponse(responseCode = "404", description = "Not found a user",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(type = "object", example = "{\"message\": \"User not found with {id}\"}")))
    })
    public ResponseEntity<MessageResponse> changeRoleUser(@RequestBody Role role, @PathVariable long userId) {
        userService.changeRole(role, userId);

        return ResponseEntity.ok(new MessageResponse("Changed role to " + role + " successfully"));
    }

    @PostMapping(value = "/upload-avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @SecurityRequirement(name = "Playlist")
    @Operation(summary = "Upload avatar", description = "If image successfully uploaded, display avatar for all users")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Avatar uploaded successfully",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(type = "object", example = "{\"message\": \"Avatar uploaded successfully\"}"))),
            @ApiResponse(responseCode = "400", description = "File does not contain image",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(type = "object"), examples = {
                    @ExampleObject(
                            name = "Content type file",
                            value = "{\"message\": \"Invalid image or png format\"}",
                            summary = "File can only contain png or jpg"
                    ),
                    @ExampleObject(
                            name = "File not uploaded",
                            value = "{\"message\": \"File must not be null or empty\"}",
                            summary = "The file must be uploaded"
                    )
            }))
    })
    public ResponseEntity<MessageResponse> uploadAvatar(@RequestPart MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            return ResponseEntity.badRequest().body(new MessageResponse("File must not be null or empty"));
        } else if (!file.getContentType().equals("image/jpeg") && !file.getContentType().equals("image/png")) {
            return ResponseEntity.badRequest().body(new MessageResponse("Invalid image jpg or png format"));
        }

        userService.uploadAvatar(file);

        return ResponseEntity.ok(new MessageResponse("Avatar uploaded successfully"));
    }

    @GetMapping("/me")
    public ResponseEntity<UserDTO> getCurrentUser() {
        User currentUser = userService.getCurrentUser();

        UserDTO userDTO = userMapper.userToUserDTO(currentUser);

        return ResponseEntity.ok(userDTO);
    }
}
