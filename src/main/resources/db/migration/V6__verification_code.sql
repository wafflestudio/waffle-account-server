create table `verification_code` (
    `id` bigint not null auto_increment,
    `code` bigint not null,
    `target` varchar(100) not null,
    `expire_at` datetime(6) not null,
    `method` varchar(10) not null,
    primary key (`id`)
);
