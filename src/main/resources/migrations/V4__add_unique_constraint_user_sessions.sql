-- Deduplicate possible rows before adding unique constraint
WITH duplicates AS (
    SELECT id
    FROM (
        SELECT id,
               ROW_NUMBER() OVER (PARTITION BY chat_id, date ORDER BY id) AS rn
        FROM user_sessions
    ) t
    WHERE t.rn > 1
)
DELETE FROM user_sessions us
USING duplicates d
WHERE us.id = d.id;

-- Create a unique index (works for ON CONFLICT (chat_id, date))
CREATE UNIQUE INDEX IF NOT EXISTS user_sessions_chat_date_unique_idx
    ON user_sessions (chat_id, date);

-- Helpful index for lookups by chat_id
CREATE INDEX IF NOT EXISTS user_sessions_chat_id_idx ON user_sessions (chat_id);


