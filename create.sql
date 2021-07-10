CREATE DATABASE audio;
USE audio;

CREATE TABLE users
(
    user_id  INT NOT NULL AUTO_INCREMENT,
    login    VARCHAR(40) NOT NULL ,
    password VARCHAR(40) NOT NULL ,
    PRIMARY KEY (user_id)
);

CREATE TABLE records
(
    record_id  INT NOT NULL AUTO_INCREMENT,
    user_id  INT NOT NULL ,
    path VARCHAR(255),
    PRIMARY KEY  (record_id),
    FOREIGN KEY  (user_id) REFERENCES users(user_id)
);