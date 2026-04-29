package zhedron.playlist.config.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import zhedron.playlist.services.JwtService;
import zhedron.playlist.services.impl.UserDetailsImpl;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {
    private JwtService jwtService;
    private UserDetailsImpl userDetails;

    @Autowired
    public JwtFilter(JwtService jwtService, UserDetailsImpl userDetails) {
        this.jwtService = jwtService;
        this.userDetails = userDetails;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String token = null;

        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if (cookie.getName().equals("accessToken")) {
                    token = cookie.getValue();
                }
            }
        }

        if (token != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            String email = jwtService.extractEmail(token);

            UserDetails user = userDetails.loadUserByUsername(email);

            if (jwtService.validateToken(token, user)) {
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());

                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        }

        filterChain.doFilter(request, response);
    }
}
