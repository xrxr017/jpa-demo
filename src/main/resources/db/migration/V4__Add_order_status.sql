alter table orders add column status varchar(20);

update orders set status='PENDING' where status is null;