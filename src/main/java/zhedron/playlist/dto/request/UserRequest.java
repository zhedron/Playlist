package zhedron.playlist.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class UserRequest {
    @NotBlank(message = "Name must not be empty")
    @NotNull(message = "Name must not be null")
    @Schema(name = "name", example = "John", requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;

    @Schema(name = "about", example = "I'm artist and born in USA")
    private String about;

    @Email(message = "Write your email")
    @NotNull(message = "Email must not be null")
    @NotBlank(message = "Email must not be empty")
    @Schema(name = "email", example = "John1995@gmail.com", requiredMode = Schema.RequiredMode.REQUIRED)
    private String email;

    @NotNull(message = "Password must not be null")
    @Schema(name = "password", example = "123456", requiredMode = Schema.RequiredMode.REQUIRED)
    private String password;

    @Pattern(regexp = "^\\+\\d{12}$", message = "Write your phone number")
    @Schema(name = "phone", example = "+394111215988", requiredMode = Schema.RequiredMode.REQUIRED)
    private String phone;
}
