-- IDX-001 プロジェクト別タスク一覧
CREATE INDEX idx_tasks_project_status
ON tasks (project_id, status)
WHERE is_active = TRUE;

-- IDX-002 担当タスク一覧
CREATE INDEX idx_tasks_assignee_status
ON tasks (assignee_member_id, status)
WHERE is_active = TRUE;

-- IDX-003 レビュー対象一覧
CREATE INDEX idx_tasks_reviewer_status
ON tasks (reviewer_member_id, status)
WHERE is_active = TRUE;

-- IDX-004 期限検索
CREATE INDEX idx_tasks_due_date
ON tasks (due_date)
WHERE is_active = TRUE;

-- IDX-005 タスク変更履歴
CREATE INDEX idx_task_histories_task_created
ON task_histories (task_id, created_at, id);

-- IDX-006 タスクコメント
CREATE INDEX idx_comments_task_created
ON comments (task_id, created_at, id);

-- IDX-007 ユーザー別プロジェクトメンバー
CREATE INDEX idx_members_user_project
ON members (user_id, project_id);

-- IDX-008 ユーザー別セッション
CREATE INDEX idx_sessions_user
ON sessions (user_id);
