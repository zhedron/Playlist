package zhedron.playlist.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import zhedron.playlist.dto.PlaylistDTO;
import zhedron.playlist.entity.Playlist;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, uses = SongMapper.class)
public interface PlaylistMapper {
    List<PlaylistDTO> toPlaylistDTO(List<Playlist> playlists);
}
