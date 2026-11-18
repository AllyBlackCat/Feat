CREATE TABLE members (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    project_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    is_manager BOOLEAN NOT NULL DEFAULT FALSE,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_members_project
        FOREIGN KEY (project_id) REFERENCES projects (id),

    CONSTRAINT fk_members_user
        FOREIGN KEY (user_id) REFERENCES users (id),

    CONSTRAINT uq_members_project_user
        UNIQUE (project_id, user_id),

    CONSTRAINT uq_members_id_project
        UNIQUE (id, project_id)
);