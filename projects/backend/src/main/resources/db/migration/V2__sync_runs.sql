-- One row per explicit synchronisation (ADR-002). Counters are the trace required by spec/03.
create table sync_runs (
    id                 bigserial primary key,
    started_at         timestamp with time zone not null,
    finished_at        timestamp with time zone,
    status             varchar(16) not null,
    published_messages bigint not null default 0,
    skipped_items      bigint not null default 0,
    failed_pages       bigint not null default 0,
    failed_messages    bigint not null default 0
);

create index ix_sync_runs_status on sync_runs (status);
