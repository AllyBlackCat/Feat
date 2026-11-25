package io.github.allyblackcat.feat.auth;

public class InvalidSessionException extends RuntimeException {

    public InvalidSessionException() {
        super("Invalid or expired session");
    }
}