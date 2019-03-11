package com.upgrade.islandreservationsapi.service;

import com.upgrade.islandreservationsapi.dto.ApiFieldError;
import com.upgrade.islandreservationsapi.exception.*;
import com.upgrade.islandreservationsapi.model.Reservation;
import com.upgrade.islandreservationsapi.repository.ReservationRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
public class ReservationServiceImpl implements ReservationService {

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private ConfigurationService configurationService;

    @Autowired
    private DayAvailabilityService availabilityService;

    private final Logger logger = LogManager.getLogger(ReservationServiceImpl.class);

    @Override
    public Reservation getReservation(Integer id) throws ReservationNotFoundException {
        logger.info("Reading reservation with id {}", id);
        Optional<Reservation> reservationOpt = reservationRepository.findById(id);
        reservationOpt.ifPresentOrElse(r -> logger.info("Reservation found. Returning.."),
                () -> logger.info("Reservation not found."));
        return reservationOpt.orElseThrow(ReservationNotFoundException::new);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Reservation createReservation(Reservation reservation) throws NoAvailabilityForDateException {
        logger.debug("createReservation(): updating avalability...");
        availabilityService.updateDayAvailability(reservation);
        logger.info("Creating reservation {}", reservation.toString());
        return reservationRepository.save(reservation);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Reservation updateReservation(Reservation reservation)
            throws NoAvailabilityForDateException, ReservationNotFoundException,
                InvalidReservationException {
        logger.info("Updating reservation with id {}", reservation.getId());
        final Optional<Reservation> oldReservationOpt = reservationRepository.findAndLockById(reservation.getId());
        oldReservationOpt.ifPresentOrElse(r -> logger.info("Found existing reservation with id {}", r.getId()),
                () -> logger.info("Reservation not found. Can't update."));
        Reservation oldReservation = oldReservationOpt
                .orElseThrow(ReservationNotFoundException::new);
        // validation for update
        validateReservationUpdate(reservation, oldReservation);

        final boolean datesChanged = !oldReservation.getStart().equals(reservation.getStart())
                || !oldReservation.getEnd().equals(reservation.getEnd());
        final boolean numberChanged = !oldReservation.getNumberOfPersons().equals(reservation.getNumberOfPersons());

        oldReservation.setFistName(reservation.getFistName());
        oldReservation.setLastName(reservation.getLastName());
        oldReservation.setEmail(reservation.getEmail());

        if(!datesChanged && !numberChanged) {
            logger.info("udpateReservation(): Dates, number of persons and status did not change. " +
                    "No need to update availability.");
            return reservationRepository.save(oldReservation);
        }

        if(!datesChanged) {
            logger.info("udpateReservation(): Dates did not change. Updating availability for dates.");
            int difference = oldReservation.getNumberOfPersons() - reservation.getNumberOfPersons();
            availabilityService.addAvailability(oldReservation.getStart(), oldReservation.getEnd(), difference);
        } else {
            logger.info("udpateReservation(): Updating availability for old and new dates");
            // increase availability for old reservation dates (like if reservation was cancelled) by the old number of persons
            availabilityService.addAvailability(oldReservation.getStart(), oldReservation.getEnd(), oldReservation.getNumberOfPersons());
            // decrease availability for new reservation dates by the new number of persons
            availabilityService.updateDayAvailability(reservation);
        }
        oldReservation.setStart(reservation.getStart());
        oldReservation.setEnd(reservation.getEnd());
        oldReservation.setNumberOfPersons(reservation.getNumberOfPersons());
        logger.debug("udpateReservation(): saving reservation...");
        return reservationRepository.save(oldReservation);
    }

    private void validateReservationUpdate(Reservation reservation, Reservation existingReservation)
            throws InvalidReservationException {
        if(existingReservation.getStatus() == Reservation.Status.CANCELLED) {
            throw new ReservationCancelledException();
        }
        if(existingReservation.getStatus() != reservation.getStatus()) {
            String message = "A cancelled reservation can't be reactivated. Please create a new one.";
            if(reservation.getStatus() == Reservation.Status.CANCELLED) {
                message = "Reservation can't be cancelled using this method. Please use DELETE method.";
            }
            throw new StatusChangeNotAllowedException(message);
        }
        if(LocalDate.now().isAfter(existingReservation.getEnd())) {
            // updating a reservation that has already ended.
            throw new ReservationEndedException();
        }

        final boolean started = !existingReservation.getStart().isAfter(LocalDate.now());

        // if reservation already started, don't allow to update start date or number of persons
        if(started) {
            List<ApiFieldError> errors = new ArrayList<>();
            if(!existingReservation.getNumberOfPersons().equals(reservation.getNumberOfPersons())) {
                errors.add(new ApiFieldError("numberOfPersons", "numberOfPersons can't be updated for a reservation that already started"));
            }
            if(!existingReservation.getStart().equals(reservation.getStart())) {
                errors.add(new ApiFieldError("start", "start date can't be updated for a reservation that already started"));
            }
            if(!errors.isEmpty()) {
                throw new InvalidReservationException("Validation failed", errors);
            }
        } else {
            int minAheadDays = configurationService.getMinAheadDays();
            // reservation did not start, need to enforce validation for start date
            if(ChronoUnit.DAYS.between(LocalDate.now(), reservation.getStart()) < minAheadDays) {
                throw new InvalidReservationException("Validation failed",
                        List.of(new ApiFieldError("start",
                                String.format("start date must be at least %d day(s) in the future.", minAheadDays))));
            }
        }
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Reservation cancelReservation(Integer id)
            throws ReservationNotFoundException, ReservationAlreadyCancelledException {
        logger.info("Cancelling reservation id {}", id);
        final Optional<Reservation> reservationOpt = reservationRepository.findById(id);
        reservationOpt.ifPresentOrElse(r -> logger.info("Reservation id {} found", r.getId()),
                () -> logger.info("Reservation not found. Can't cancel."));
        final Reservation reservation = reservationOpt.orElseThrow(ReservationNotFoundException::new);
        if(reservation.getStatus() == Reservation.Status.CANCELLED) {
            logger.info("Reservation id {} is already cancelled.", id);
            throw new ReservationAlreadyCancelledException(id);
        }
        reservation.setStatus(Reservation.Status.CANCELLED);

        // update availability
        logger.info("Updating availability after cancellation");
        availabilityService.addAvailability(reservation.getStart(), reservation.getEnd(), reservation.getNumberOfPersons());

        return reservationRepository.save(reservation);
    }

}
