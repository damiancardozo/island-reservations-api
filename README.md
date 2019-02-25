# island-reservations-api
REST API to handle reservations for a campsite in an island in the Pacific Ocean

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

INSERT INTO `Island`.`Configuration` (`Name`, `Description`, `Value`) VALUES ('MAX_AVAILABILITY', 'Max number of people that can visit the island one day', '100');
INSERT INTO `Island`.`Configuration` (`Name`, `Description`, `Value`) VALUES ('MAX_RESERVATION', 'Max number of days a reservation can be made for', '3');
INSERT INTO `Island`.`Configuration` (`Name`, `Description`, `Value`) VALUES ('MIN_AHEAD', 'Minimum number of days a reservation must be made ahead', '1');
INSERT INTO `Island`.`Configuration` (`Name`, `Description`, `Value`) VALUES ('MAX_AHEAD', 'Max number of days a reservation can be made ahead', '30');
INSERT INTO `Island`.`Configuration` (`Name`, `Description`, `Value`) VALUES ('MAX_DATE_RANGE', 'Max number of days to query for availability', '90');