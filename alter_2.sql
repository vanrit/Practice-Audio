ALTER TABLE users
    MODIFY COLUMN password VARCHAR(64) NOT NULL;

ALTER TABLE records
    ADD COLUMN scope VARCHAR(7) CHECK ( scope IN ('public', 'private') );