package zhedron.playlist.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import zhedron.playlist.dto.PlaylistDTO;
import zhedron.playlist.dto.UserDTO;
import zhedron.playlist.entity.Playlist;
import zhedron.playlist.entity.User;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface UserMapper {
    UserDTO userToUserDTO(User user);
    
    List<PlaylistDTO> playlistsToPlaylistDTOs(List<Playlist> playlists);
}
