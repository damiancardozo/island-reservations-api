insert into Reservation(ReservationID, FirstName, LastName, Email, Start, End, NumberOfPersons, Status, Version)
values (1, 'John', 'Oliver', 'johno@gmail.com', DATEADD('DAY', 1, CURRENT_DATE), DATEADD('DAY', 3, CURRENT_DATE), 10, 'ACTIVE', 1);

insert into DayAvailability(Date, Availability, MaxAvailability)
values (DATEADD('DAY', 1, CURRENT_DATE), 90, 100);
insert into DayAvailability(Date, Availability, MaxAvailability)
values (DATEADD('DAY', 2, CURRENT_DATE), 90, 100);

insert into DayAvailability(Date, Availability, MaxAvailability)
values (DATEADD('DAY', 6, CURRENT_DATE), 90, 100);
insert into DayAvailability(Date, Availability, MaxAvailability)
values (DATEADD('DAY', 7, CURRENT_DATE), 100, 100);
insert into DayAvailability(Date, Availability, MaxAvailability)
values (DATEADD('DAY', 8, CURRENT_DATE), 100, 100);

insert into Dates values(CURRENT_DATE);
insert into Dates values(DATEADD('DAY', 1, CURRENT_DATE));
insert into Dates values(DATEADD('DAY', 2, CURRENT_DATE));
insert into Dates values(DATEADD('DAY', 3, CURRENT_DATE));
insert into Dates values(DATEADD('DAY', 4, CURRENT_DATE));
insert into Dates values(DATEADD('DAY', 5, CURRENT_DATE));
insert into Dates values(DATEADD('DAY', 6, CURRENT_DATE));
insert into Dates values(DATEADD('DAY', 7, CURRENT_DATE));
insert into Dates values(DATEADD('DAY', 8, CURRENT_DATE));
insert into Dates values(DATEADD('DAY', 9, CURRENT_DATE));
insert into Dates values(DATEADD('DAY', 10, CURRENT_DATE));
insert into Dates values(DATEADD('DAY', 11, CURRENT_DATE));
insert into Dates values(DATEADD('DAY', 12, CURRENT_DATE));
insert into Dates values(DATEADD('DAY', 13, CURRENT_DATE));
insert into Dates values(DATEADD('DAY', 14, CURRENT_DATE));
insert into Dates values(DATEADD('DAY', 15, CURRENT_DATE));

