CREATE TABLE tasks (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    project_id BIGINT NOT NULL,
    public_id UUID NOT NULL UNIQUE DEFAULT gen_random_uuid(),
    title VARCHAR(200) NOT NULL,
    description VARCHAR(5000),
    assignee_member_id BIGINT,
    reviewer_member_id BIGINT,
    status VARCHAR(20) NOT NULL DEFAULT 'not_started',
    priority VARCHAR(10) NOT NULL,
    planned_start_date DATE NOT NULL,
    due_date DATE NOT NULL,
    created_by BIGINT NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT chk_tasks_status
        CHECK (status IN (
            'not_started', 'in_progress', 'review_requested',
            'in_review', 'returned', 'completed'
        )),

    CONSTRAINT chk_tasks_priority
        CHECK (priority IN ('low', 'medium', 'high', 'urgent')),

    CONSTRAINT chk_tasks_dates
        CHECK (due_date >= planned_start_date),

    CONSTRAINT chk_tasks_distinct_members
        CHECK (
            assignee_member_id IS NULL
            OR reviewer_member_id IS NULL
            OR assignee_member_id <> reviewer_member_id
        ),

    CONSTRAINT fk_tasks_project
        FOREIGN KEY (project_id) REFERENCES projects (id),

    CONSTRAINT fk_tasks_creator
        FOREIGN KEY (created_by) REFERENCES users (id),

    CONSTRAINT fk_tasks_assignee
        FOREIGN KEY (assignee_member_id, project_id)
        REFERENCES members (id, project_id),

    CONSTRAINT fk_tasks_reviewer
        FOREIGN KEY (reviewer_member_id, project_id)
        REFERENCES members (id, project_id)
);
