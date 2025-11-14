package com.team.updevic001.configuration.config.oauth2;

import com.team.updevic001.dao.entities.auth.User;
import com.team.updevic001.dao.entities.auth.UserProfile;
import com.team.updevic001.dao.entities.auth.UserRole;
import com.team.updevic001.dao.repositories.UserProfileRepository;
import com.team.updevic001.dao.repositories.UserRepository;
import com.team.updevic001.model.enums.Role;
import com.team.updevic001.services.interfaces.AuthService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

import static com.team.updevic001.model.enums.Status.ACTIVE;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthService authService;
    private final DefaultOAuth2UserService delegate = new DefaultOAuth2UserService();

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest req) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = delegate.loadUser(req);

        String email = oAuth2User.getAttribute("email");
        String firstName;
        String lastName;

        if ("github".equals(req.getClientRegistration().getRegistrationId())) {
            String fullName = oAuth2User.getAttribute("name");
            String[] parts = fullName != null ? fullName.split(" ", 2) : new String[0];
            firstName = parts.length > 0 ? parts[0] : "GitHubUser";
            lastName = parts.length > 1 ? parts[1] : "";
        } else if ("google".equals(req.getClientRegistration().getRegistrationId())) {
            firstName = oAuth2User.getAttribute("given_name");
            lastName = oAuth2User.getAttribute("family_name");
        } else {
            firstName = "Unknown";
            lastName = "User";
        }

        String avatarUrl = oAuth2User.getAttribute("picture");

        UserRole role = authService.findOrCreateRole(Role.STUDENT);

        User user = userRepository.findByEmail(email).orElseGet(() -> {
            User newUser = User.builder()
                    .email(email)
                    .password(passwordEncoder.encode(UUID.randomUUID().toString()))
                    .firstName(firstName)
                    .lastName(lastName)
                    .status(ACTIVE)
                    .roles(List.of(role))
                    .build();

            User saved = userRepository.save(newUser);

            userProfileRepository.save(UserProfile.builder()
                    .user(saved)
                    .profilePhotoUrl(avatarUrl)
                    .profilePhotoKey("public/" + saved.getId() + "profilePhoto")
                    .build());

            return saved;
        });


        return new DefaultOAuth2User(
                user.getRoles().stream()
                        .map(r -> new SimpleGrantedAuthority(r.getName().name()))
                        .toList(),
                oAuth2User.getAttributes(), // Google/GitHub-dan gələn atributlar saxlanır
                "email" // əsas açar -> email
        );
    }

}
