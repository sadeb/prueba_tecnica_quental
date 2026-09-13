-- Surrogate id + unique (user, character) instead of a composite PK: same guarantee, simpler JPA mapping.
create table user_favorites (
    id           bigserial primary key,
    user_id      bigint not null,
    character_id bigint not null,
    created_at   timestamp with time zone not null,
    constraint uq_user_favorites unique (user_id, character_id),
    constraint fk_user_favorites_user foreign key (user_id) references users (id) on delete cascade,
    constraint fk_user_favorites_character foreign key (character_id) references characters (id) on delete cascade
);

create index ix_user_favorites_user on user_favorites (user_id);
