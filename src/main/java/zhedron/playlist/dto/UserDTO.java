package zhedron.playlist.dto;

import zhedron.playlist.enums.Provider;
import zhedron.playlist.enums.Role;

import java.time.LocalDateTime;
import java.util.List;


public record UserDTO(long id, String email, LocalDateTime createdAt, Role role, List<PlaylistDTO> playlists, boolean blocked, Provider provider, String name, String about) {
}
