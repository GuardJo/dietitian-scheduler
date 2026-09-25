--- 사용자
create sequence user_id_seq start with 1 increment by 1;
comment on sequence user_id_seq is 'USER 식별키 시퀀스';

create table account
(
    id         bigint primary key default nextval('user_id_seq'),
    username   varchar(50)  not null unique,
    name       varchar(10)  not null,
    password   varchar(255) not null,
    created_at timestamp          default now(),
    updated_at timestamp          default now()
);

comment on table account is '사용자 원장 테이블';
comment on column account.id is '사용자 식별키';
comment on column account.username is '사용자 아이디';
comment on column account.name is '사용자 성명';
comment on column account.password is '사용자 비밀번호';
comment on column account.created_at is '생성일시';
comment on column account.updated_at is '수정일시';

--- 근무 타입
create sequence shift_type_seq start with 1 increment by 1;
comment on sequence shift_type_seq is '근무 타입 식별키 시퀀스';

create table shift_type (
                            id bigint primary key default nextval('shift_type_seq'),
                            label varchar(20) not null,
                            start_time time(0) not null,
                            end_time time(0) not null,
                            color varchar(7) not null not null check ( color ~ '^#[0-9A-F]{6}$' ),
    created_at timestamp not null default now(),
    updated_at timestamp not null default now()
);
comment on table shift_type is '근무 타입 테이블';
comment on column shift_type.id is '근무 타입 식별키';
comment on column shift_type.label is '근무 타입 라벨명';
comment on column shift_type.start_time is '근무 시작 시각';
comment on column shift_type.end_time is '근무 종료 시각';
comment on column shift_type.color is '16진수 색상값';
comment on column shift_type.created_at is '생성일시';
comment on column shift_type.updated_at is '수정일시';

--- 근무 스케줄
create sequence schedule_seq start with 1 increment by 1;
comment on sequence schedule_seq is '근무 스케줄 식별키 시퀀스';

create table schedule(
                         id bigint primary key default nextval('schedule_seq'),
                         account_id bigint not null references account(id) on delete cascade,
                         work_date date not null,
                         shift_type_id bigint not null references shift_type(id) on delete cascade,
                         memo varchar(255),
                         created_at timestamp not null default now(),
                         updated_at timestamp not null default now(),
                         unique (account_id, work_date)
);

create index schedule_work_date_idx on schedule(work_date);
create index schedule_shift_type_id_idx on schedule(shift_type_id);
create index schedule_account_id_idx on schedule(account_id);

comment on table schedule is '근무 스케줄 테이블';
comment on column schedule.id is '근무 스케줄 식별키';
comment on column schedule.account_id is '사용자 식별키';
comment on column schedule.work_date is '근무일자';
comment on column schedule.shift_type_id is '근무 타입 식별키';
comment on column schedule.memo is '비고';
comment on column schedule.created_at is '생성일시';
comment on column schedule.updated_at is '수정일시';