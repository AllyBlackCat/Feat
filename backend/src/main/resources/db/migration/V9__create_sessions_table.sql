CREATE TABLE sessions (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    user_id BIGINT NOT NULL,
    token_hash VARCHAR(64) NOT NULL UNIQUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_activity_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMPTZ NOT NULL,
    revoked_at TIMESTAMPTZ,

    CONSTRAINT fk_sessions_user
        FOREIGN KEY (user_id) REFERENCES users (id),

    CONSTRAINT chk_sessions_expires_at
        CHECK (expires_at > created_at),

    CONSTRAINT chk_sessions_last_activity
        CHECK (last_activity_at >= created_at)
);
