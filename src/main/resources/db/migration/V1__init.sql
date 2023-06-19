create table `account_user` (
    `id` bigint not null auto_increment,
    `username` varchar (100),
    `email` varchar(100),
    `password` varchar(128) not null,
    `is_active` boolean not null,
    `is_banned` boolean not null,
    `created_at` datetime(6) not null,
    `updated_at` datetime(6) not null,
    primary key (`id`),
    key `ix_username` (`username`),
    unique key `ux_email` (`email`)
);

create table `account_refresh_token` (
    `id` bigint not null auto_increment,
    `user_id` bigint not null,
    `token` varchar(500) not null,
    `token_hash` varchar(64) not null,
    `used_token_id` bigint,
    `expire_at` datetime(6) not null,
    `created_at` datetime(6) not null,
    `updated_at` datetime(6) not null,
    primary key (`id`),
    constraint `fk_user_id` foreign key (`user_id`) references account_user (`id`),
    constraint `fk_used_token_id` foreign key (`used_token_id`) references account_refresh_token (`id`)
);
