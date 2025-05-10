package zhedron.playlist.dto;

import zhedron.playlist.enums.Role;

import java.util.List;


public record UserDTO(long id, String email, Role role, List<PlaylistDTO> playlistsDTO) {
}
