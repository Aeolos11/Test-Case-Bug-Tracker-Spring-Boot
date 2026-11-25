CREATE TABLE projects (
                          id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                          name VARCHAR(255) NOT NULL,
                          description TEXT,
                          owner_id UUID NOT NULL,

                          CONSTRAINT fk_projects_owner
                              FOREIGN KEY (owner_id)
                                  REFERENCES users(id)
);

CREATE TABLE project_members (
                                 project_id UUID NOT NULL,
                                 user_id UUID NOT NULL,

                                 PRIMARY KEY (project_id, user_id),

                                 CONSTRAINT fk_members_project
                                     FOREIGN KEY (project_id)
                                         REFERENCES projects(id)
                                         ON DELETE CASCADE,

                                 CONSTRAINT fk_members_user
                                     FOREIGN KEY (user_id)
                                         REFERENCES users(id)
                                         ON DELETE CASCADE
);

CREATE INDEX idx_projects_owner
    ON projects(owner_id);

CREATE INDEX idx_project_members_user
    ON project_members(user_id);