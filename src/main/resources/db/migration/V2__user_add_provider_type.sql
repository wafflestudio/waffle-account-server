ALTER TABLE account_user ADD COLUMN provider varchar(20) NOT NULL;
ALTER TABLE account_user ADD INDEX ix_provider (provider);
