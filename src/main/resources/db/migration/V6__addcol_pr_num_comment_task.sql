ALTER TABLE pull_request ADD COLUMN comment_count INTEGER NOT NULL DEFAULT 0;
ALTER TABLE pull_request ADD COLUMN task_count INTEGER NOT NULL DEFAULT 0;