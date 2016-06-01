-- :name create-bulb-table
-- :command :execute
-- :result :raw
-- :doc Create bulbs table
create table bulb (
  id varchar(40) primary key,
  description varchar(128),
  model varchar(128) not null,
  ip varchar(40) not null,
  online boolean not null default true
);

-- :name insert-bulb :! :n
insert into bulb (id, model, ip)
  values (:id, :model, :ip);

-- :name update-bulb-ip :! :n
update bulb set ip = :ip where id = :id;

-- :name update-online :! :n
update bulb set online = :online
where id in (:v*:ids);

-- :name all-bulbs :? :*
select * from bulb;

-- :name get-bulb :? :1
select * from bulb where id = :id;

-- :name drop-bulb-table
-- :command :execute
-- :result :raw
-- :doc Drop bulb-table
drop table if exists bulb;
