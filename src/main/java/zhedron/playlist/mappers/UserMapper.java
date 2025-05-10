package zhedron.playlist.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import zhedron.playlist.dto.PlaylistDTO;
import zhedron.playlist.dto.UserDTO;
import zhedron.playlist.entity.Playlist;
import zhedron.playlist.entity.User;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface UserMapper {


    @Mapping(source = "playlists", target = "playlistsDTO")
    UserDTO userToUserDTO(User user);


    @Mapping(source = "playlists", target = "playlistsDTO")
    List<PlaylistDTO> playlistsToPlaylistDTOs(List<Playlist> playlists);
}
