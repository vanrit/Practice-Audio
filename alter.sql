USE audio;

ALTER TABLE users
    ADD COLUMN first_name VARCHAR(30),
    ADD COLUMN last_name  VARCHAR(30);

ALTER TABLE records
    ADD COLUMN record_name   VARCHAR(30) NOT NULL,
    ADD COLUMN duration      TIME        NOT NULL,
    ADD COLUMN source        VARCHAR(8) CHECK ( source IN ('Whatsapp', 'Telegram', 'Local') ),
    ADD COLUMN source_id     int,
    ADD COLUMN source_author VARCHAR(30),
    ADD FOREIGN KEY (source_id) REFERENCES users (user_id);