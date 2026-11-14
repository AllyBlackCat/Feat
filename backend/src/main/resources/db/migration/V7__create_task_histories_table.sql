CREATE TABLE task_histories (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    task_id BIGINT NOT NULL,
    actor_id BIGINT NOT NULL,
    action VARCHAR(50) NOT NULL,
    field_name VARCHAR(50),
    old_value TEXT,
    new_value TEXT,
    reason TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_task_histories_task
        FOREIGN KEY (task_id) REFERENCES tasks (id),

    CONSTRAINT fk_task_histories_actor
        FOREIGN KEY (actor_id) REFERENCES users (id)
);
