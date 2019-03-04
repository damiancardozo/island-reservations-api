package com.upgrade.islandreservationsapi.service;

import com.upgrade.islandreservationsapi.exception.*;
import com.upgrade.islandreservationsapi.model.Reservation;
import com.upgrade.islandreservationsapi.repository.ReservationRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

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
        reservation.setStatus(Reservation.Status.ACTIVE);
        logger.debug("createReservation(): updating uvalability...");
        availabilityService.updateDayAvailability(reservation);
        logger.info("Creating reservation {}", reservation.toString());
        return reservationRepository.save(reservation);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Reservation updateReservation(Reservation reservation)
            throws NoAvailabilityForDateException, ReservationNotFoundException,
                ReservationCancelledException, StatusChangeNotAllowedException {
        logger.info("Updating reservation with id {}", reservation.getId());
        final Optional<Reservation> oldReservationOpt = reservationRepository.findById(reservation.getId());
        oldReservationOpt.ifPresentOrElse(r -> logger.info("Found existing reservation with id {}, version {}",
                r.getId(), r.getVersion()),
                () -> logger.info("Reservation not found. Can't update."));
        Reservation oldReservation = oldReservationOpt
                .orElseThrow(ReservationNotFoundException::new);
        if(oldReservation.getStatus() == Reservation.Status.CANCELLED) {
            throw new ReservationCancelledException();
        }
        if(oldReservation.getStatus() != reservation.getStatus()) {
            String message;
            if(reservation.getStatus() == Reservation.Status.CANCELLED) {
                message = "Reservation can't be cancelled using this method. Please use DELETE method.";
            } else {
                message = "A cancelled reservation can't be reactivated. Please create a new one.";
            }
            throw new StatusChangeNotAllowedException(message);
        }

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
            availabilityService.addAvailability(oldReservation.getStart(), oldReservation.getEnd(), oldReservation.getNumberOfPersons());
            availabilityService.updateDayAvailability(reservation);
        }
        logger.debug("udpateReservation(): saving reservation...");
        oldReservation.setStart(reservation.getStart());
        oldReservation.setEnd(reservation.getEnd());
        oldReservation.setNumberOfPersons(reservation.getNumberOfPersons());
        return reservationRepository.saveAndFlush(oldReservation);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Reservation cancelReservation(Integer id)
            throws ReservationNotFoundException, ReservationAlreadyCancelledException {
        logger.info("Cancelling reservation id {}", id);
        final Optional<Reservation> reservationOpt = reservationRepository.findById(id);
        reservationOpt.ifPresentOrElse(r -> logger.info("Reservation id {} found", r.getId()),
                () -> logger.info("Reservation not found. Can't cancel."));
        Reservation reservation = reservationOpt.orElseThrow(ReservationNotFoundException::new);
        if(reservation.getStatus() == Reservation.Status.CANCELLED) {
            logger.info("Reservation id {} is already cancelled.", id);
            throw new ReservationAlreadyCancelledException(id);
        }
        reservation.setStatus(Reservation.Status.CANCELLED);
        reservation = reservationRepository.save(reservation);

        // update availability
        logger.info("Updating availability after cancellation");
        availabilityService.addAvailability(reservation.getStart(), reservation.getEnd(), reservation.getNumberOfPersons());

        return reservation;
    }

}
