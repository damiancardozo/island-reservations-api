# island-reservations-api
REST API to handle reservations for a campsite in an island in the Pacific Ocean

To see API documentation, start app and browse http://localhost:8080/swagger-ui.html



SQL to create Database with inserts for Configuration

CREATE SCHEMA `Island`;

CREATE TABLE `Island`.`Reservation` (
  `ReservationID` INT NOT NULL AUTO_INCREMENT,
  `FirstName` VARCHAR(60) NOT NULL,
  `LastName` VARCHAR(60) NOT NULL,
  `Email` VARCHAR(60) NOT NULL,
  `Start` DATE NOT NULL,
  `End` DATE NOT NULL,
  `NumberOfPersons` INT NOT NULL,
  `Status` VARCHAR(20) NOT NULL,
  PRIMARY KEY (`ReservationID`));

CREATE TABLE `Island`.`DayAvailability` (
  `Date` DATE NOT NULL,
  `Availability` INT NOT NULL,
  `MaxAvailability` INT NULL,
  PRIMARY KEY (`Date`));

CREATE TABLE `Island`.`Configuration` (
  `Name` VARCHAR(45) NOT NULL,
  `Description` VARCHAR(200) NULL,
  `Value` VARCHAR(45) NOT NULL,
  PRIMARY KEY (`Name`));

-- configuration records

INSERT INTO `Island`.`Configuration` (`Name`, `Description`, `Value`) VALUES ('MAX_AVAILABILITY', 'Max number of people that can visit the island one day', '100');

INSERT INTO `Island`.`Configuration` (`Name`, `Description`, `Value`) VALUES ('MAX_RESERVATION', 'Max number of days a reservation can be made for', '3');

INSERT INTO `Island`.`Configuration` (`Name`, `Description`, `Value`) VALUES ('MIN_AHEAD', 'Minimum number of days a reservation must be made ahead', '1');

INSERT INTO `Island`.`Configuration` (`Name`, `Description`, `Value`) VALUES ('MAX_AHEAD', 'Max number of days a reservation can be made ahead', '30');

INSERT INTO `Island`.`Configuration` (`Name`, `Description`, `Value`) VALUES ('MAX_DATE_RANGE', 'Max number of days to query for availability', '90');

INSERT INTO `Island`.`Configuration` (`Name`, `Description`, `Value`) VALUES ('DEFAULT_DATE_RANGE', 'Default number of days to query for availability', '30');


-- view with dates used to lock before creating/updating DayAvailability records

CREATE VIEW `Dates` AS
SELECT a.Date
from (
	select (CURDATE() - INTERVAL 30 DAY) + INTERVAL (a.a + (10 * b.a) + (100 * c.a)) DAY AS Date
	FROM
    (SELECT 0 AS a UNION ALL SELECT 1 UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4 UNION ALL SELECT 5 UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL SELECT 9) AS a
        CROSS JOIN (SELECT 0 AS a UNION ALL SELECT 1 UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4 UNION ALL SELECT 5 UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL SELECT 9) AS b
        CROSS JOIN (SELECT 0 AS a UNION ALL SELECT 1 UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4 UNION ALL SELECT 5 UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL SELECT 9) AS c
	) a
    WHERE
        a.Date <= (CURDATE() + INTERVAL (SELECT
                CAST(configuration.Value AS UNSIGNED)
            FROM
                configuration
            WHERE
                configuration.Name = 'MAX_DATE_RANGE') DAY)
			AND a.Date >= (CURDATE() - INTERVAL (SELECT
                CAST(configuration.Value AS UNSIGNED)
            FROM
                configuration
            WHERE
                configuration.Name = 'MAX_RESERVATION') DAY);