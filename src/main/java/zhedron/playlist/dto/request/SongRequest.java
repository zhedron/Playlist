package zhedron.playlist.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class SongRequest {

    @NotBlank(message = "Name must not be empty")
    @NotNull(message = "Name must not be null")
    private String artistName;

    @NotBlank(message = "Album must not be empty")
    private String albumName;
}
