package io.github.allyblackcat.feat.auth;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.github.allyblackcat.feat.user.User;

public record MeResponse(
    Long id,

    @JsonProperty("mail_address")
    String mailAddress,

    String name,

    @JsonProperty("screen_name")
    String screenName,

    boolean administrator,

    @JsonProperty("must_change_password")
    boolean mustChangePassword
) {

    public static MeResponse from(User user) {

        return new MeResponse(
            user.id(),
            user.mailAddress(),
            user.name(),
            user.screenName(),
            user.administrator(),
            user.mustChangePassword()
        );
    }
}
