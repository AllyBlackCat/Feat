CREATE TABLE comments (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    task_id BIGINT NOT NULL,
    author_id BIGINT NOT NULL,
    content VARCHAR(5000) NOT NULL,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    edited_at TIMESTAMPTZ,
    deleted_at TIMESTAMPTZ,

    CONSTRAINT fk_comments_task
        FOREIGN KEY (task_id) REFERENCES tasks (id),

    CONSTRAINT fk_comments_author
        FOREIGN KEY (author_id) REFERENCES users (id),

    CONSTRAINT chk_comments_deletion
        CHECK (
            (is_deleted = FALSE AND deleted_at IS NULL)
            OR (is_deleted = TRUE AND deleted_at IS NOT NULL)
        )
);
