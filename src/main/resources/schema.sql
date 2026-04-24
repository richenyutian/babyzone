create table if not exists settings (
    id integer primary key check (id = 1),
    baby_name text not null,
    baby_birthday text not null,
    admin_username text not null,
    admin_password_hash text not null,
    created_time text not null,
    update_time text not null
);

create table if not exists records (
    id integer primary key autoincrement,
    content text not null,
    date text not null,
    tags text,
    create_time text not null,
    update_time text not null
);

create table if not exists photos (
    id integer primary key autoincrement,
    record_id integer not null,
    file_path text not null,
    file_name text not null,
    foreign key (record_id) references records (id) on delete cascade
);

create index if not exists idx_records_date on records (date desc, create_time desc, id desc);
create index if not exists idx_photos_record_id on photos (record_id);
