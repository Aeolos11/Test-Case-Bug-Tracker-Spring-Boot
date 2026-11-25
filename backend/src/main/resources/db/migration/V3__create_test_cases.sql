CREATE TABLE test_cases (
 id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
 title VARCHAR(255) NOT NULL, description TEXT,
 status VARCHAR(20) NOT NULL CHECK (status IN ('pending','passed','failed','blocked')),
 priority VARCHAR(20) NOT NULL CHECK (priority IN ('low','medium','high','critical')),
 expected_result TEXT NOT NULL,
 project_id UUID NOT NULL REFERENCES projects(id) ON DELETE CASCADE,
 created_by UUID NOT NULL REFERENCES users(id),
 created_at TIMESTAMPTZ NOT NULL, updated_at TIMESTAMPTZ NOT NULL
);
CREATE INDEX idx_test_cases_project ON test_cases(project_id);
CREATE TABLE test_steps (
 test_case_id UUID NOT NULL REFERENCES test_cases(id) ON DELETE CASCADE,
 position INTEGER NOT NULL, step_num INTEGER NOT NULL CHECK (step_num > 0),
 action TEXT NOT NULL, expected_result TEXT NOT NULL,
 PRIMARY KEY(test_case_id, position)
);
CREATE TABLE test_case_events (
 event_id UUID PRIMARY KEY, test_case_id UUID NOT NULL,
 old_status VARCHAR(20) NOT NULL, new_status VARCHAR(20) NOT NULL,
 actor_id UUID NOT NULL, occurred_at TIMESTAMPTZ NOT NULL
);
