package zhedron.playlist.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Component;
import zhedron.playlist.entity.User;
import zhedron.playlist.enums.Provider;
import zhedron.playlist.enums.Role;
import zhedron.playlist.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Component
public class CustomOauth2UserService extends DefaultOAuth2UserService {
    @Autowired
    private UserRepository userRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) {
        OAuth2User oAuth2User = super.loadUser(userRequest);

        String token = userRequest.getAccessToken().getTokenValue();

        Map<String, Object> attributes = new HashMap<>();

        if (token != null) {
            String email = (String) oAuth2User.getAttributes().get("email");

            attributes.put("email", email);
            attributes.put("name", oAuth2User.getAttributes().get("name"));

            Optional<User> userFound = userRepository.findByEmail(email);

            if (userFound.isEmpty()) {
                User user = new User();

                if (user.getAbout() == null || user.getAbout().isEmpty()) {
                    user.setAbout("There is no description");
                }

                user.setName((String) attributes.get("name"));
                user.setEmail(email);
                user.setBlocked(false);
                user.setCreatedAt(LocalDateTime.now());
                user.setProvider(Provider.google);
                user.setRole(Role.USER);

                userRepository.save(user);
            }
        }

        return new DefaultOAuth2User(oAuth2User.getAuthorities(), attributes, "email");
    }
}
