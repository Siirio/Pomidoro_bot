-- Добавление индекса для оптимизации запросов по дате в таблице user_sessions
CREATE INDEX IF NOT EXISTS user_sessions_date_idx ON user_sessions (date);

-- Добавление индекса для оптимизации запросов по achieved_on в таблице user_achievements
CREATE INDEX IF NOT EXISTS user_achievements_achieved_on_idx ON user_achievements (achieved_on);

-- Добавление составного индекса для оптимизации запросов по chat_id и achieved_on в таблице user_achievements
CREATE INDEX IF NOT EXISTS user_achievements_chat_id_achieved_on_idx ON user_achievements (chat_id, achieved_on);