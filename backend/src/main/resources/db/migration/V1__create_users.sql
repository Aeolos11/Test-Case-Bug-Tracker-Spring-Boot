CREATE TABLE users (
                       id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                       name VARCHAR(255) NOT NULL,
                       email VARCHAR(255) NOT NULL UNIQUE,
                       password_hash VARCHAR(255) NOT NULL,
                       role VARCHAR(20) NOT NULL DEFAULT 'tester',

                       CONSTRAINT chk_users_role
                           CHECK (role IN ('admin', 'tester', 'developer'))
);