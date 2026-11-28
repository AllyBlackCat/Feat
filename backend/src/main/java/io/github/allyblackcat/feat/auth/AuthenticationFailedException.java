
package io.github.allyblackcat.feat.auth;

public class AuthenticationFailedException extends RuntimeException {

    public AuthenticationFailedException() {
        super("認証に失敗しました。");
    }
}
