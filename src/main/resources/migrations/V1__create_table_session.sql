CREATE TABLE IF NOT EXISTS user_sessions (
    id SERIAL PRIMARY KEY,
    chat_id BIGINT NOT NULL,
    date DATE NOT NULL,
    work_cycles INT NOT NULL,
    total_minutes INT NOT NULL
);

CREATE UNIQUE INDEX IF NOT EXISTS user_sessions_chat_id_date_idx ON user_sessions (chat_id, date);