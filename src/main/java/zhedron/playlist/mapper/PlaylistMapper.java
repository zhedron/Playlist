package zhedron.playlist.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import zhedron.playlist.dto.PlaylistDTO;
import zhedron.playlist.dto.SongDTO;
import zhedron.playlist.entity.Playlist;
import zhedron.playlist.entity.Song;

import java.util.List;
import java.util.Set;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface PlaylistMapper {
    List<PlaylistDTO> toPlaylistDTO(List<Playlist> playlists);

    Set<SongDTO> toSongDTO(Set<Song> songs);
}
