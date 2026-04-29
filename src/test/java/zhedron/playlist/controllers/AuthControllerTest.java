package zhedron.playlist.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import zhedron.playlist.config.filter.JwtFilter;
import zhedron.playlist.dto.request.LoginRequest;
import zhedron.playlist.entity.RefreshToken;
import zhedron.playlist.entity.User;
import zhedron.playlist.services.JwtService;
import zhedron.playlist.services.RefreshTokenService;
import zhedron.playlist.services.UserService;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private JwtFilter jwtFilter;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private RefreshTokenService refreshTokenService;

    @MockitoBean
    private AuthenticationManager authenticationManager;

    @Test
    void loginShouldReturnTokensWhenCredentialsAreValid() throws Exception {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("test@gmail.com");
        loginRequest.setPassword("secret");

        User user = new User();
        user.setEmail(loginRequest.getEmail());

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setRefreshToken("refresh-token");

        Authentication authentication = new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword(), List.of());

        when(authenticationManager.authenticate(any()))
                .thenReturn(authentication);
        when(userService.findByEmail(loginRequest.getEmail())).thenReturn(user);
        when(jwtService.generateToken(user)).thenReturn("access-token");
        when(refreshTokenService.generateRefreshToken(loginRequest.getEmail())).thenReturn(refreshToken);

        mockMvc.perform(post("/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("access-token"))
                .andExpect(jsonPath("$.refreshToken").value("refresh-token"))
                .andExpect(header().exists(HttpHeaders.SET_COOKIE));
    }

    @Test
    void loginShouldReturnUnauthorizedWhenCredentialsAreInvalid() throws Exception {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("test@test.com");
        loginRequest.setPassword("wrong");

        when(authenticationManager.authenticate(any()))
                .thenThrow(new BadCredentialsException("Invalid email or password"));

        mockMvc.perform(post("/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(loginRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Invalid email or password"));
    }

    @Test
    void refreshTokenShouldReturnNewAccessToken() throws Exception {
        User user = new User();
        user.setEmail("test@test.com");

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setRefreshToken("refresh-token");
        refreshToken.setUser(user);

        RefreshToken newRefreshToken = new RefreshToken();
        newRefreshToken.setRefreshToken("rotated-refresh-token");

        when(refreshTokenService.findByRefreshToken("refresh-token")).thenReturn(Optional.of(refreshToken));
        when(refreshTokenService.verifyRefreshToken(refreshToken)).thenReturn(refreshToken);
        when(jwtService.generateToken(user)).thenReturn("new-access-token");
        when(userService.findByEmail(user.getEmail())).thenReturn(user);
        when(refreshTokenService.generateRefreshToken(user.getEmail())).thenReturn(newRefreshToken);

        mockMvc.perform(post("/refreshtoken")
                        .cookie(new Cookie("refreshToken", "refresh-token")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("new-access-token"))
                .andExpect(jsonPath("$.refreshToken").value("rotated-refresh-token"));
    }

    @Test
    void refreshTokenShouldReturnNotFoundWhenTokenDoesNotExist() throws Exception {
        when(refreshTokenService.findByRefreshToken(anyString())).thenReturn(Optional.empty());

        mockMvc.perform(post("/refreshtoken")
                        .cookie(new Cookie("refreshToken", "missing-token")))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Refresh Token not found"));
    }

}
