package zhedron.playlist.dto;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import zhedron.playlist.enums.Type;

import java.time.LocalDateTime;

@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, property = "@class")
public record SongDTO(long id, String artistName, String albumName,
                      long listeners, LocalDateTime createdAt, String contentType,
                      String fileName, int duration, Type type,
                      String imagePath, String contentTypeImage, long creatorId) {
}
