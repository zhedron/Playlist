package zhedron.playlist.dto.response;

import zhedron.playlist.dto.SongDTO;

import java.util.List;

public record PaginatedResponse(List<SongDTO> songs, int page, int size,
                                long totalElements, int totalPages, boolean last,
                                boolean first, boolean hasNext, boolean hasPrevious) {
}
