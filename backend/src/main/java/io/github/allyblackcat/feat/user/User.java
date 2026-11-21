
package io.github.allyblackcat.feat.user;

import java.time.OffsetDateTime;

public record User(
    Long id,
    String mailAddress,
    String name,
    String screenName,
    String department,
    boolean administrator,
    boolean available,
    String passwordHash,
    boolean mustChangePassword,
    int failedLoginCount,
    OffsetDateTime lastFailedLoginAt,
    OffsetDateTime lockedAt,
    OffsetDateTime createdAt,
    OffsetDateTime updatedAt
) {
}
