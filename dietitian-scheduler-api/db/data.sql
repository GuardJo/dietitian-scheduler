insert into account(username, name, password)
values ('tester', '테스터', '{noop}password1!');

insert into shift_type(label, start_time, end_time, color)
values ('A', '05:30:00', '15:00:00', '#2563EB');
insert into shift_type(label, start_time, end_time, color)
values ('B', '10:00:00', '19:30:00', '#F59E0B');
insert into shift_type(label, start_time, end_time, color)
values ('C', '08:30:00', '18:00:00', '#EF4444');
insert into shift_type(label, start_time, end_time, color)
values ('ALL', '05:30:00', '19:30:00', '#10B981');

insert into schedule(account_id, work_date, shift_type_id)
values (1, '2026-09-04', 1);
insert into schedule(account_id, work_date, shift_type_id)
values (1, '2026-09-05', 3);
insert into schedule(account_id, work_date, shift_type_id)
values (1, '2026-09-06', 2);
insert into schedule(account_id, work_date, shift_type_id)
values (1, '2026-09-10', 4);
insert into schedule(account_id, work_date, shift_type_id)
values (1, '2026-09-11', 1);