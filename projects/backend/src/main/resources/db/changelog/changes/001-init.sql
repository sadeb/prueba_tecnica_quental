--liquibase formatted sql

--changeset rickmorty:001-init
-- Synchronised entities (ADR-001): internal id + unique external_id. Descriptive columns are nullable
-- because a row may be created as a placeholder from a forward reference (ADR-004).
-- DDL restricted to the subset accepted by PostgreSQL 10 and H2 2.x in MODE=PostgreSQL (tests).

create table locations (
    id           bigserial primary key,
    external_id  bigint not null,
    name         varchar(255),
    type         varchar(255),
    dimension    varchar(255),
    placeholder  boolean not null default false,
    created_at   timestamp with time zone not null,
    updated_at   timestamp with time zone not null,
    constraint uq_locations_external_id unique (external_id)
);

create table episodes (
    id           bigserial primary key,
    external_id  bigint not null,
    name         varchar(255),
    air_date     varchar(64),
    code         varchar(16),
    placeholder  boolean not null default false,
    created_at   timestamp with time zone not null,
    updated_at   timestamp with time zone not null,
    constraint uq_episodes_external_id unique (external_id)
);

create table characters (
    id           bigserial primary key,
    external_id  bigint not null,
    name         varchar(255),
    status       varchar(16) not null default 'UNKNOWN',
    species      varchar(255),
    type         varchar(255),
    gender       varchar(16) not null default 'UNKNOWN',
    image_url    varchar(512),
    origin_id    bigint,
    location_id  bigint,
    placeholder  boolean not null default false,
    created_at   timestamp with time zone not null,
    updated_at   timestamp with time zone not null,
    constraint uq_characters_external_id unique (external_id),
    constraint fk_characters_origin foreign key (origin_id) references locations (id),
    constraint fk_characters_location foreign key (location_id) references locations (id)
);

create table character_episodes (
    character_id bigint not null,
    episode_id   bigint not null,
    primary key (character_id, episode_id),
    constraint fk_character_episodes_character foreign key (character_id) references characters (id) on delete cascade,
    constraint fk_character_episodes_episode foreign key (episode_id) references episodes (id) on delete cascade
);

create index ix_characters_name on characters (name);
create index ix_characters_status on characters (status);
create index ix_characters_species on characters (species);
create index ix_characters_gender on characters (gender);
create index ix_characters_origin on characters (origin_id);
create index ix_characters_location on characters (location_id);
create index ix_character_episodes_episode on character_episodes (episode_id);
