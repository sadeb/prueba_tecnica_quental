--liquibase formatted sql

--changeset rickmorty:005-users-role
-- Role of the account (ADR-012): USER for self-registered accounts, ADMIN only for the system
-- administrator provisioned from .env at startup. Existing rows keep the USER default.
alter table users add column role varchar(16) not null default 'USER';
