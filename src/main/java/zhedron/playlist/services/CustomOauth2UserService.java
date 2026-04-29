package zhedron.playlist.services;

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

    private final UserRepository userRepository;

    private final ImageContentTypeFetcherService imageContentTypeFetcherService;
    @Autowired
    public CustomOauth2UserService(UserRepository userRepository, ImageContentTypeFetcherService imageContentTypeFetcherService) {
        this.userRepository = userRepository;
        this.imageContentTypeFetcherService = imageContentTypeFetcherService;
    }

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) {
        OAuth2User oAuth2User = super.loadUser(userRequest);

        String token = userRequest.getAccessToken().getTokenValue();

        Map<String, Object> attributes = new HashMap<>();

        if (token != null) {
            String email = (String) oAuth2User.getAttributes().get("email");

            attributes.put("email", email);
            attributes.put("name", oAuth2User.getAttributes().get("name"));
            attributes.put("picture", oAuth2User.getAttributes().get("picture"));

            Optional<User> userFound = userRepository.findByEmail(email);

            if (userFound.isEmpty()) {
                User user = new User();

                String picture = (String) oAuth2User.getAttributes().get("picture");

                String contentType = imageContentTypeFetcherService.getImageContentType(picture);

                user.setName((String) attributes.get("name"));
                user.setEmail(email);
                user.setBlocked(false);
                user.setCreatedAt(LocalDateTime.now());
                user.setProvider(Provider.GOOGLE);
                user.setRole(Role.USER);
                user.setProfilePicture(picture.replace("=s96", "=s360"));
                user.setContentType(contentType);

                userRepository.save(user);
            }
        }

        return new DefaultOAuth2User(oAuth2User.getAuthorities(), attributes, "email");
    }
}
