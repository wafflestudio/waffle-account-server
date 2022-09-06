create table `sms_code` (
    `id` bigint not null auto_increment,
    `code` bigint not null,
    `phone_number` varchar(30) not null,
    `expire_at` datetime(6) not null,
    primary key (`id`)
);
