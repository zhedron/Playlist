package zhedron.playlist.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class UserUpdateRequest {
    private String name;

    private String about;

    @Email(message = "Write your email")
    private String email;

    private String password;

    @Pattern(regexp = "^\\+\\d{12}$", message = "Write your phone number")
    private String phone;

    private Boolean isHiddenPhone;
}
