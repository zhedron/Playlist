package zhedron.playlist.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import zhedron.playlist.dto.request.LoginRequest;
import zhedron.playlist.dto.response.MessageResponse;
import zhedron.playlist.dto.response.TokenResponse;
import zhedron.playlist.entity.RefreshToken;
import zhedron.playlist.entity.User;
import zhedron.playlist.exception.RefreshTokenNotFoundException;
import zhedron.playlist.exception.UserNotFoundException;
import zhedron.playlist.service.JwtService;
import zhedron.playlist.service.RefreshTokenService;
import zhedron.playlist.service.UserService;

import java.util.HashMap;
import java.util.Map;


@RestController()
@Tag(name = "Authorization API", description = "Authorization with login, password and get JWT token")
public class AuthController {
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final UserService userService;
    private final RefreshTokenService refreshTokenService;

    public AuthController(JwtService jwtService, AuthenticationManager authenticationManager, UserService userService, RefreshTokenService refreshTokenService) {
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
        this.userService = userService;
        this.refreshTokenService = refreshTokenService;
    }

    @PostMapping(value = "/login", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Login", description = "Login with email, password and get JWT token for authorization in others resources")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully logged ang got JWT token",
            content = @Content(mediaType = "application/json", schema = @Schema(type = "object", example = """
                    {
                      "accessToken": "{accessToken}",
                      "refreshToken": "refreshToken"
                    }"""))),
            @ApiResponse(responseCode = "401", description = "Invalid login",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(type = "object"), examples = {
                    @ExampleObject(
                            name = "Error email or password",
                            value = "{\"message\": \"Invalid email or password\"}",
                            summary = "Email does not exist or wrong password"
                    ),
                    @ExampleObject(
                            name = "Account is blocked",
                            value = "{\"message\": \"Your account is locked\"}",
                            summary = "The blocked user cannot log in"
                    )
            }))
    })
    public ResponseEntity<?> login (@Valid @RequestBody LoginRequest loginRequest, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            Map<String, String> errors = new HashMap<>();
            for (FieldError error : bindingResult.getFieldErrors()) {
                errors.put("error", error.getDefaultMessage());
            }
            return ResponseEntity.badRequest().body(errors);
        }

        String accessToken = null;
        RefreshToken refreshToken = null;

        try {
            Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                    loginRequest.getEmail(), loginRequest.getPassword()
            ));

            if (authentication.isAuthenticated()) {
                User userFound = userService.findByEmail(loginRequest.getEmail());

                if (userFound != null) {
                    accessToken = jwtService.generateToken(userFound);

                    refreshToken = refreshTokenService.generateRefreshToken(userFound.getEmail());
                }
            }
        } catch (BadCredentialsException | UserNotFoundException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new MessageResponse("Invalid email or password"));
        } catch (LockedException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new MessageResponse("Your account is locked"));
        }

        TokenResponse tokenResponse = new TokenResponse(accessToken, refreshToken.getRefreshToken());

        return ResponseEntity.ok(tokenResponse);
    }

    @GetMapping("/google")
    public ResponseEntity<TokenResponse> google() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();

        User userFound = userService.findByEmail((String) oAuth2User.getAttributes().get("email"));

        String accessToken = null;
        RefreshToken refreshToken = null;

        if (userFound != null) {
            accessToken = jwtService.generateToken(userFound);

            refreshToken = refreshTokenService.generateRefreshToken(userFound.getEmail());
        }

        TokenResponse tokenResponse = new TokenResponse(accessToken, refreshToken.getRefreshToken());

        return ResponseEntity.ok(tokenResponse);
    }

    @PostMapping("/refreshtoken")
    @Operation(summary = "refresh token for access token", description = "Extend access token the expiration date")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully extend access token the expiration date",
            content = @Content(schema = @Schema(implementation = TokenResponse.class))),
            @ApiResponse(responseCode = "404", description = "Not found a refresh token",
            content = @Content(schema = @Schema(type = "object", example = "{\"message\": \"Refresh Token not found with {refreshToken}\"}")))
    })
    public ResponseEntity<TokenResponse> refreshToken (@RequestBody RefreshToken refreshToken) {
        return refreshTokenService.findByRefreshToken(refreshToken.getRefreshToken())
                .map(refreshTokenService::verifyRefreshToken)
                .map(RefreshToken::getUser)
                .map(user -> {
                    String accessToken = jwtService.generateToken(user);

                    TokenResponse tokenResponse = new TokenResponse(accessToken, refreshToken.getRefreshToken());

                    return ResponseEntity.ok(tokenResponse);
                })
                .orElseThrow(() -> new RefreshTokenNotFoundException("Refresh Token not found with " + refreshToken.getRefreshToken()));
    }
}
