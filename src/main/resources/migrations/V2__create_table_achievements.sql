CREATE TABLE IF NOT EXISTS user_achievements (
    id SERIAL PRIMARY KEY,
    chat_id BIGINT NOT NULL,
    emoji TEXT NOT NULL,
    title TEXT NOT NULL,
    achieved_on DATE NOT NULL
);

CREATE INDEX IF NOT EXISTS user_achievements_chat_id_idx ON user_achievements (chat_id);
CREATE UNIQUE INDEX IF NOT EXISTS user_achievements_unique_idx ON user_achievements (chat_id, title, achieved_on);