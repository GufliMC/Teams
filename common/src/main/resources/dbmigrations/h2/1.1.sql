-- apply alter tables
alter table clans add column member_limit integer default 10 not null;
