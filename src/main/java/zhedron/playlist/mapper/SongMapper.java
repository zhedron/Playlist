package zhedron.playlist.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import zhedron.playlist.dto.SongDTO;
import zhedron.playlist.entity.Song;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface SongMapper {
    @Mapping(target = "creatorId", expression = "java(song.getCreator().getId())")
    SongDTO songToSongDTO(Song song);

    List<SongDTO> songToSongDTOList(List<Song> songs);
}
