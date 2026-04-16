drop table if exists "events" cascade;

drop table if exists "notifications" cascade;

drop table if exists "account" cascade;

drop table if exists "posts" cascade;

drop table if exists "comment" cascade;

drop table if exists "reactions" cascade;

create sequence event_id_seq;

create sequence account_id_seq;

create sequence post_id_seq;

create sequence comment_id_seq;

create sequence event_id_seq;

create sequence account_id_seq;

create sequence post_id_seq;

create sequence comment_id_seq;

create table if not exists events
(
	id bigint not null
		primary key,
	type varchar(100) not null,
	payload jsonb,
	status varchar(20) default 'waiting'::character varying not null
		constraint chk_event_status
			check ((status)::text = ANY (ARRAY[('waiting'::character varying)::text, ('pending'::character varying)::text, ('running'::character varying)::text, ('succeeded'::character varying)::text, ('failed'::character varying)::text, ('dead'::character varying)::text])),
	retry_count integer default 0 not null,
	error_msg text,
	created_at timestamp with time zone default CURRENT_TIMESTAMP,
	next_run_at timestamp with time zone default CURRENT_TIMESTAMP,
	updated_at timestamp with time zone default CURRENT_TIMESTAMP,
	current_step varchar(50),
	version integer default 0 not null
);

comment on column events.id is '事件的唯一标识符。';

comment on column events.type is '事件的类型，用于提供给 worker 分配任务。';

comment on column events.payload is '事件所需的参数。';

comment on column events.status is '事件的状态：waiting(等待), running(处理中), succeeded(成功), failed(失败), dead(死亡).';

comment on column events.retry_count is '事件重试次数。';

comment on column events.error_msg is '事件产生错误时的错误消息。';

comment on column events.created_at is '事件创建时的时间戳。';

comment on column events.next_run_at is '事件下一次尝试的时间戳。';

comment on column events.updated_at is '事件更新的时间戳。';

comment on column events.current_step is '当前所在的步骤。';

alter sequence event_id_seq owned by events.id;

create index if not exists idx_event_waiting
	on events (next_run_at)
	where ((status)::text = 'waiting'::text);

create table if not exists notifications
(
	id bigint not null
		constraint pk_notifications
			primary key,
	recipient_account_id bigint not null,
	actor_account_id bigint not null,
	type varchar(50) not null,
	target_type varchar(20) not null,
	target_id bigint not null,
	content text not null,
	is_read boolean default false not null,
	read_at timestamp with time zone,
	created_at timestamp with time zone default CURRENT_TIMESTAMP not null
);

create index if not exists idx_notifications_recipient_created
	on notifications (recipient_account_id, created_at desc);

create index if not exists idx_notifications_recipient_unread
	on notifications (recipient_account_id, is_read);

create table if not exists accounts
(
	id bigint not null
		constraint pk_account
			primary key,
	username varchar(50) not null
		constraint uq_account_username
			unique,
	nickname varchar(50),
	password_hash varchar(255) not null,
	must_change_password boolean default false not null，
	email varchar(255)
		constraint uq_account_email
			unique,
	phone varchar(20)
		constraint uq_account_phone
			unique,
	profile jsonb,
	status varchar(20) default 'normal'::character varying not null
		constraint chk_account_status
			check ((status)::text = ANY (ARRAY[('normal'::character varying)::text, ('suspended'::character varying)::text, ('archived'::character varying)::text])),
	role varchar(20) default 'user'::character varying not null
		constraint chk_account_role
			check ((role)::text = ANY (ARRAY[('user'::character varying)::text, ('admin'::character varying)::text, ('owner'::character varying)::text])),
	created_at timestamp with time zone default CURRENT_TIMESTAMP not null,
	updated_at timestamp with time zone default CURRENT_TIMESTAMP not null,
	last_login_at timestamp with time zone,
	last_login_ip inet
);

comment on column accounts.id is '账户的唯一标识符';

comment on column accounts.username is '账户的唯一用户名';

comment on column accounts.password_hash is '加密后的密码';

comment on column accounts.email is '账户关联的电子邮箱地址';

comment on column accounts.phone is '账户关联的手机号码';

comment on column accounts.profile is '账户的个人信息';

comment on column accounts.status is '账户的状态';

comment on column accounts.role is '账户所在的用户组';

comment on column accounts.created_at is '账户创建的时间戳';

comment on column accounts.updated_at is '账户上次更新的时间';

comment on column accounts.last_login_at is '账户上次的登录时间';

comment on column accounts.last_login_ip is '账户上次的登录IP地址';

create unique index if not exists idx_only_one_owner
  on accounts (role)
  where ((role)::text = 'owner'::text);

create table if not exists posts
(
	id bigserial
		constraint pk_post
			primary key,
	author_id bigint not null,
	title varchar(50),
	content text not null,
	status varchar(20) default 'normal'::character varying not null
		constraint chk_post_status
			check ((status)::text = ANY (ARRAY[('normal'::character varying)::text, ('archived'::character varying)::text])),
	created_at timestamp with time zone default CURRENT_TIMESTAMP not null,
	updated_at timestamp with time zone
);

create index if not exists idx_post_account
	on posts (author_id);

create index if not exists idx_post_list
	on posts (status asc, created_at desc);

create table if not exists comments
(
	id bigint not null
		constraint pk_comment
			primary key,
	target_type varchar(20) not null
		constraint chk_comment_target_type
			check ((target_type)::text = ANY ((ARRAY['post'::character varying, 'comment'::character varying, 'activity'::character varying])::text[])),
	target_id bigint not null,
	account_id bigint not null,
	parent_id bigint,
	content text not null,
	status varchar(20) default 'normal'::character varying not null
		constraint chk_comment_status
			check ((status)::text = ANY (ARRAY[('normal'::character varying)::text, ('archived'::character varying)::text])),
	created_at timestamp with time zone default CURRENT_TIMESTAMP not null,
	updated_at timestamp with time zone
);

create index if not exists idx_comment_target
	on comments (target_type, target_id);

create index if not exists idx_comment_parent
	on comments (parent_id);

create table if not exists reactions
(
	id bigint not null
		constraint pk_reactions
			primary key,
	account_id bigint not null,
	target_type varchar not null
		constraint chk_target_type
			check ((target_type)::text = ANY ((ARRAY['post'::character varying, 'comment'::character varying, 'activity'::character varying])::text[])),
	target_id bigint not null,
	reaction_type varchar not null
		constraint chk_reaction_type
			check ((reaction_type)::text = ANY ((ARRAY['like'::character varying, 'love'::character varying, 'laugh'::character varying, 'sad'::character varying])::text[])),
	created_at timestamp with time zone default now() not null,
	updated_at timestamp with time zone,
	constraint uq_account_target
		unique (account_id, target_type, target_id)
);

create index if not exists idx_reactions_target
	on reactions (target_type, target_id);

/* Insert default owner account. Default password is "password123456". */
/* MUST change password after first login. */
insert into accounts(id, username, nickname, must_change_password, password_hash, status, role, created_at, updated_at)
values (1,
        'owner',
        'Owner',
        true,
        '{bcrypt}$2a$10$okgQzA7rsCF1veD1B11/A.OkqJKMDfVjNKDuZRYZtiSx1bJSVsL9O',
        'normal',
        'owner',
        current_timestamp,
        current_timestamp) on conflict (username) do nothing;
