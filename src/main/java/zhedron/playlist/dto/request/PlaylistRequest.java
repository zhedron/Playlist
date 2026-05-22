package zhedron.playlist.dto.request;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class PlaylistRequest {
    private String title;

    private boolean isPublic;
}
