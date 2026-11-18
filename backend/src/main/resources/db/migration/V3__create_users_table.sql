CREATE TABLE users (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    mail_address VARCHAR(254) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    screen_name VARCHAR(100) NOT NULL,
    department VARCHAR(100),
    administrator BOOLEAN NOT NULL DEFAULT FALSE,
    is_available BOOLEAN NOT NULL DEFAULT TRUE,
    password_hash VARCHAR(255) NOT NULL,
    must_change_password BOOLEAN NOT NULL,
    failed_login_count INTEGER NOT NULL DEFAULT 0,
    last_failed_login_at TIMESTAMPTZ,
    locked_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT chk_users_failed_login_count
        CHECK (failed_login_count >= 0)
);