alter table users add column nickname VARCHAR(20);
alter table users add constraint uk_user_nickname unique (nickname);