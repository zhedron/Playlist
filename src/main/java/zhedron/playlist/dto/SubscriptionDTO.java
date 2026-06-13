package zhedron.playlist.dto;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, property = "@class")
public record SubscriptionDTO(long id, long userId, String name,
                              String profilePicture, String contentType) {
}
