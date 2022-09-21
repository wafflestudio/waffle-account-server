create table `verification_code` (
    `id` bigint not null auto_increment,
    `code` bigint not null,
    `target` varchar(100) not null,
    `expire_at` datetime(6) not null,
    `method` varchar(10) not null,
    `user_id` bigint not null,
    primary key (`id`)
);

ALTER TABLE `account_user` ADD COLUMN `phone` varchar(30);
ALTER TABLE `account_user` ADD COLUMN `is_email_verified` boolean not null default false;
ALTER TABLE `account_user` ADD COLUMN `is_phone_verified` boolean not null default false;
