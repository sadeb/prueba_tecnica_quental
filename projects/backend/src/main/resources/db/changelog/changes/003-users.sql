--liquibase formatted sql

--changeset rickmorty:003-users
create table users (
    id            bigserial primary key,
    username      varchar(64) not null,
    password_hash varchar(72) not null,
    created_at    timestamp with time zone not null,
    constraint uq_users_username unique (username)
);
