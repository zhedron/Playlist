package zhedron.playlist.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import zhedron.playlist.dto.SongDTO;
import zhedron.playlist.entity.Song;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface SongMapper {
    SongDTO songToSongDTO(Song song);

    List<SongDTO> songToSongDTOList(List<Song> songs);
}
