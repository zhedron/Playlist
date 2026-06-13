package zhedron.playlist.dto;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.time.LocalDateTime;
import java.util.Set;

@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, property = "@class")
public record PlaylistDTO(long id, Set<SongDTO> songs, long views,
                          long duration, boolean isPublic, int counter,
                          LocalDateTime createdAt, String imageURL, String contentType,
                          String title) {
}
