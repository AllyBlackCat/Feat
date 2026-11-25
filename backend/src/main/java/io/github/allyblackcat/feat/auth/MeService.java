package io.github.allyblackcat.feat.auth;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.github.allyblackcat.feat.user.User;
import io.github.allyblackcat.feat.user.UserRepository;

@Service
public class MeService {

    private final SessionValidationService sessionValidationService;
    private final UserRepository userRepository;

    public MeService(
            SessionValidationService sessionValidationService,
            UserRepository userRepository) {

        this.sessionValidationService = sessionValidationService;
        this.userRepository = userRepository;
    }

    @Transactional
    public MeResponse getMe(String token) {

        Session session =
            sessionValidationService.validate(token);

        User user = userRepository
            .findById(session.userId())
            .orElseThrow(InvalidSessionException::new);

        if (!user.available()) {
            throw new InvalidSessionException();
        }

        return MeResponse.from(user);
    }
}
