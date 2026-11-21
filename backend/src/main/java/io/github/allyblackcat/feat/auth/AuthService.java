package io.github.allyblackcat.feat.auth;

import java.util.Optional;

import io.github.allyblackcat.feat.user.UserRepository;
import io.github.allyblackcat.feat.user.User;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service 
public class AuthService {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(
            UserRepository userRepository, 
            PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public Optional<User> findUser(String mailAddress) {
        return userRepository.findByMailAddress(mailAddress);
    }

    public User signin(String mailAddress, String password) {
        Optional<User> user = userRepository.findByMailAddress(mailAddress);

        // 該当ユーザーが存在しない場合、認証エラー発生
        if (user.isEmpty()) {
            throw new AuthenticationFailedException();
        }

        // 存在するユーザー
        User foundUser = user.orElseThrow();

        // 利用状態が「利用可能」でない場合、認証エラー発生
        if (!foundUser.available()) {
            throw new AuthenticationFailedException();
        }

        // アカウントのロック状態である場合、認証エラー発生
        if (foundUser.lockedAt() != null) {
            throw new AuthenticationFailedException();
        }

        // パスワードハッシュ値が一致しない場合、認証エラー発生
        if (!passwordEncoder.matches(password, foundUser.passwordHash())) {
            throw new AuthenticationFailedException();
        }

        return foundUser;
    }
}