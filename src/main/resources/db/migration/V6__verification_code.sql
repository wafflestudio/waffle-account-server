create table `verification_code` (
    `id` bigint not null auto_increment,
    `code` varchar(10) not null,
    `target` varchar(100) not null,
    `sent_at` datetime(6) not null,
    `verified_at` datetime(6),
    `expire_at` datetime(6) not null,
    `method` varchar(10) not null,
    `user_id` bigint not null,
    `is_valid` boolean not null,
    primary key (`id`),
    key `ix_code` (`code`),
    key `ix_target` (`target`, `is_valid`)
);

ALTER TABLE `account_user` ADD COLUMN `phone` varchar(30);
ALTER TABLE `account_user` ADD COLUMN `verified_email` varchar(100);
ALTER TABLE `account_user` ADD COLUMN `verified_snu_email` varchar(100);
