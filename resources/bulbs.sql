-- :name create-bulb-table
-- :command :execute
-- :result :raw
-- :doc Create bulbs table
create table bulb(
  id varchar(40) primary key,
  description varchar(128)
);

-- :name create-bulb-state-table
-- :command :execute
-- :result :raw
-- :doc Create bulb-state-table
create table bulb_state(
  bulb_id varchar(40) primary key,
  ip varchar(40),
  power boolean not null default false,
  online boolean not null default false,
  mode varchar(20),
  rgb varchar(12),
  ww_percent integer,
  last_updated integer,
  foreign key(bulb_id) references bulb(id)
);

-- :name all-bulbs :? :*
select b.id,
       b.description,
       bs.ip,
       bs.power,
       bs.mode,
       bs.rgb,
       bs.ww_percent,
       bs.last_updated
from bulb b
  left join bulb_state bs on b.id = bs.bulb_id
where bs.online;

-- :name get-bulb :? :1
select b.id,
       b.description,
       bs.ip,
       bs.power,
       bs.mode,
       bs.rgb,
       bs.ww_percent,
       bs.last_updated
from bulb b
  left join bulb_state bs on b.id = bs.bulb_id
where bs.online and b.id = :id;

-- :name drop-bulb-state-table
-- :command :execute
-- :result :raw
-- :doc Drop bulb-state-table
drop table if exists bulb_state;

-- :name drop-bulb-table
-- :command :execute
-- :result :raw
-- :doc Drop bulb-table
drop table if exists bulb;


