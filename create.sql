create database audio;
use audio;

create table users
(
    user_id  int not null auto_increment,
    login    varchar(40) not null ,
    password varchar(40) not null ,
    primary key (user_id)
);

create table records
(
    record_id int not null auto_increment,
    user_id int not null ,
    path varchar(255),
    primary key (record_id),
    foreign key (user_id) references users(user_id)
);