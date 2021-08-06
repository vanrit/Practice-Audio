ALTER TABLE users
    ADD INDEX idx_login (login);

ALTER TABLE users
    ADD INDEX idx_last_first (last_name, first_name);

SELECT index_name, non_unique, column_name
FROM information_schema.STATISTICS tics
WHERE table_schema = 'audio'
  AND table_name = 'users'
ORDER BY 1, 3;

ALTER TABLE records
    ADD INDEX idx_record_name (record_name);

SELECT index_name, non_unique, column_name
FROM information_schema.STATISTICS
WHERE table_schema = 'audio'
  AND table_name = 'records'
ORDER BY 1, 3;


