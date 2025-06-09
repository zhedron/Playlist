package zhedron.playlist.dto;

import java.util.List;

public record PlaylistDTO(long id, List<SongDTO> songs, long views) {
}
