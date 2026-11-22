package io.github.allyblackcat.feat.auth;

import java.time.OffsetDateTime;

public record Session(
    Long id,
    Long userId,
    String tokenHash,
    OffsetDateTime createdAt,
    OffsetDateTime lastActivityAt,
    OffsetDateTime expiresAt,
    OffsetDateTime revokedAt
) {
}
