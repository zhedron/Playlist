package zhedron.playlist.dto;

import java.time.LocalDateTime;
import java.util.List;

public record PlaylistDTO(long id, List<SongDTO> songs, long views,
                          long duration, boolean isPublic, int counter,
                          LocalDateTime createdAt) {
}
