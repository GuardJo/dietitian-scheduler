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