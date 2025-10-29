package zhedron.playlist.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import zhedron.playlist.dto.responseDTO.Token;
import zhedron.playlist.entity.RefreshToken;
import zhedron.playlist.entity.User;
import zhedron.playlist.exceptions.RefreshTokenNotFoundException;
import zhedron.playlist.repository.RefreshTokenRepository;
import zhedron.playlist.service.JwtService;
import zhedron.playlist.service.RefreshTokenService;
import zhedron.playlist.service.UserService;

import java.util.UUID;

@RestController
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

    @PostMapping("/login")
    public ResponseEntity<?> login (@RequestBody User user) {
        Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                user.getEmail(), user.getPassword()
        ));

        if (authentication.isAuthenticated()) {
            User userFound = userService.findByEmail(user.getEmail());

            String accessToken = null;
            RefreshToken refreshToken = null;

            if (userFound != null) {
                accessToken = jwtService.generateToken(userFound.getEmail());

                refreshToken = refreshTokenService.generateRefreshToken(userFound.getEmail());
            }

            Token token = new Token(accessToken, refreshToken.getRefreshToken());

            return ResponseEntity.ok(token);
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid email or password");
        }
    }

    @GetMapping("/google")
    public ResponseEntity<Token> google() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();

        User userFound = userService.findByEmail((String) oAuth2User.getAttributes().get("email"));

        String accessToken = null;
        RefreshToken refreshToken = null;

        if (userFound != null) {
            accessToken = jwtService.generateToken(userFound.getEmail());

            refreshToken = refreshTokenService.generateRefreshToken(userFound.getEmail());
        }

        Token token = new Token(accessToken, refreshToken.getRefreshToken());

        return ResponseEntity.ok(token);
    }

    @PostMapping("/refreshtoken")
    public ResponseEntity<Token> refreshToken (@RequestBody RefreshToken refreshToken) {
        return refreshTokenService.findByRefreshToken(refreshToken.getRefreshToken())
                .map(refreshTokenService::verifyRefreshToken)
                .map(RefreshToken::getUser)
                .map(user -> {
                    String accessToken = jwtService.generateToken(user.getEmail());

                    Token token = new Token(accessToken, refreshToken.getRefreshToken());

                    return ResponseEntity.ok(token);
                })
                .orElseThrow(() -> new RefreshTokenNotFoundException("Refresh Token not found with " + refreshToken.getRefreshToken()));
    }
}
