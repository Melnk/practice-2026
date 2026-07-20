CREATE TABLE IF NOT EXISTS users (
    id UUID PRIMARY KEY,
    email VARCHAR(320) NOT NULL UNIQUE,
    password_hash TEXT NOT NULL,
    profile_description TEXT NOT NULL DEFAULT '',
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_users_profile_description
    ON users (profile_description);

CREATE TABLE IF NOT EXISTS service (
    id BIGSERIAL PRIMARY KEY,
    name TEXT NOT NULL,
    cost NUMERIC(10, 2) NOT NULL CHECK (cost >= 0)
);
