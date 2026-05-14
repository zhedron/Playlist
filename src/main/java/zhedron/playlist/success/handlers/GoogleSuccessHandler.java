package zhedron.playlist.success.handlers;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import zhedron.playlist.entity.RefreshToken;
import zhedron.playlist.entity.User;
import zhedron.playlist.repository.UserRepository;
import zhedron.playlist.services.JwtService;
import zhedron.playlist.services.RefreshTokenService;

import java.io.IOException;
import java.time.Duration;
import java.util.Optional;

@Component
public class GoogleSuccessHandler implements AuthenticationSuccessHandler {
    private final UserRepository userRepository;

    private final JwtService jwtService;

    private final RefreshTokenService refreshTokenService;

    public GoogleSuccessHandler(UserRepository userRepository, JwtService jwtService, RefreshTokenService refreshTokenService) {
        this.userRepository = userRepository;
        this.jwtService = jwtService;
        this.refreshTokenService = refreshTokenService;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();

        Optional<User> userFound = userRepository.findByEmail((String) oAuth2User.getAttributes().get("email"));

        if (userFound.isPresent()) {
            User user = userFound.get();

            String accessToken = jwtService.generateToken(user);

            RefreshToken refreshToken = refreshTokenService.generateRefreshToken(user.getEmail());

            ResponseCookie cookie_accessToken = ResponseCookie.from("accessToken", accessToken)
                    .httpOnly(true)
                    .path("/")
                    .maxAge(Duration.ofMinutes(15))
                    .build();

            ResponseCookie cookie_refreshToken = ResponseCookie.from("refreshToken", refreshToken.getRefreshToken())
                    .httpOnly(true)
                    .path("/refreshtoken")
                    .maxAge(Duration.ofDays(30))
                    .build();

            response.addHeader(HttpHeaders.SET_COOKIE, cookie_refreshToken.toString());
            response.addHeader(HttpHeaders.SET_COOKIE, cookie_accessToken.toString());

            response.sendRedirect("http://localhost:4200");
        }
    }
}
