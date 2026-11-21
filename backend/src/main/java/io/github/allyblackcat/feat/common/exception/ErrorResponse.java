package io.github.allyblackcat.feat.common.exception;

public record ErrorResponse(
    String code,
    String message
) {
}