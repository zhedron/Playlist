package zhedron.playlist.dto.request;

import jakarta.validation.constraints.Email;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class LoginRequest {

    @Email(message = "Write your email")
    private String email;
    private String password;
}
