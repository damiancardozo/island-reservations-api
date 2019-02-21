package com.upgrade.islandreservationsapi.service;

import com.upgrade.islandreservationsapi.exception.*;
import com.upgrade.islandreservationsapi.model.DayAvailability;
import com.upgrade.islandreservationsapi.model.Reservation;
import com.upgrade.islandreservationsapi.repository.DayAvailabilityRepository;
import com.upgrade.islandreservationsapi.repository.ReservationRepository;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class ReservationServiceImpl implements ReservationService {

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private ConfigurationService configurationService;

    @Autowired
    private DayAvailabilityRepository availabilityRepository;

    @Override
    public Reservation getReservation(Integer id) throws ReservationNotFoundException {
        Optional<Reservation> reservationOpt = reservationRepository.findById(id);
        return reservationOpt.orElseThrow(ReservationNotFoundException::new);
    }

    @Override
    public Reservation createReservation(Reservation reservation) throws NoAvailabilityForDateException, DayAvailabilityNotFoundException {

        List<LocalDate> dates = reservation.getStart().datesUntil(reservation.getEnd().plusDays(1)).collect(Collectors.toList());
        updateAvailability(dates, reservation, true);

        return reservationRepository.save(reservation);
    }

    @Override
    public Reservation updateReservation(Reservation reservation)
            throws NoAvailabilityForDateException, DayAvailabilityNotFoundException, ReservationNotFoundException {

        Reservation oldReservation = reservationRepository.findById(reservation.getId()).orElseThrow(ReservationNotFoundException::new);

        if(oldReservation.getStart().isAfter(reservation.getStart())) {
            LocalDate deleteFromDate;
            LocalDate addToDate;
            if(reservation.getEnd().isAfter(oldReservation.getStart())) {
                addToDate = oldReservation.getStart();
                deleteFromDate = reservation.getEnd().plusDays(1);
            } else {
                addToDate = reservation.getEnd();
                deleteFromDate = oldReservation.getStart(); // maybe have to add 1 if oldreservation.start = reservation.end they are the same
            }
            List<LocalDate> addDates = reservation.getStart().datesUntil(addToDate).collect(Collectors.toList());
            updateAvailability(addDates, reservation, true);

            List<LocalDate> removeDates = deleteFromDate.datesUntil(oldReservation.getEnd()).collect(Collectors.toList());
            updateAvailability(removeDates, oldReservation, false);
        } else if(reservation.getStart().isAfter(oldReservation.getStart())) {
            LocalDate toDate;
            if(oldReservation.getEnd().isAfter(reservation.getStart())) {
                toDate = reservation.getStart();
            } else {
                toDate = oldReservation.getEnd();
            }
            List<LocalDate> removeDates = oldReservation.getStart().datesUntil(toDate).collect(Collectors.toList());
            updateAvailability(removeDates, oldReservation, false);

            LocalDate fromDate;
            if(oldReservation.getEnd().isAfter(reservation.getStart())) {
                fromDate = oldReservation.getEnd().plusDays(1);
            } else {
                fromDate = reservation.getStart();
            }
            List<LocalDate> addDates = fromDate.datesUntil(reservation.getEnd().plusDays(1)).collect(Collectors.toList());
            updateAvailability(addDates, reservation, true);
        }

        return reservationRepository.save(reservation);
    }

    @Override
    public Reservation cancelReservation(Integer id)
            throws ReservationNotFoundException, ReservationAlreadyCancelledException, NoAvailabilityForDateException, DayAvailabilityNotFoundException {
        Optional<Reservation> reservationOpt = reservationRepository.findById(id);
        Reservation reservation = reservationOpt.orElseThrow(ReservationNotFoundException::new);
        if(reservation.getStatus() == Reservation.Status.CANCELLED) {
            throw new ReservationAlreadyCancelledException(id);
        }
        reservation.setStatus(Reservation.Status.CANCELLED);
        reservation = reservationRepository.save(reservation);

        // update availability
        List<LocalDate> dates = reservation.getStart().datesUntil(reservation.getEnd().plusDays(1)).collect(Collectors.toList());
        updateAvailability(dates, reservation, false);

        return reservation;
    }

    private void updateAvailability(List<LocalDate> dates, Reservation reservation, boolean add)
            throws NoAvailabilityForDateException, DayAvailabilityNotFoundException {
        for(LocalDate date: dates) {
            Optional<DayAvailability> availabilityOpt = availabilityRepository.findById(date);
            int maxOccupancy = configurationService.getMaxAvailability();
            DayAvailability dayAvailability = add ? availabilityOpt.orElse(new DayAvailability(date, maxOccupancy, maxOccupancy))
                    : availabilityOpt.orElseThrow(DayAvailabilityNotFoundException::new);
            if(add) {
                dayAvailability.setAvailability(dayAvailability.getAvailability() - reservation.getNumberOfPersons());
            } else {
                dayAvailability.setAvailability(dayAvailability.getAvailability() + reservation.getNumberOfPersons());
            }
            if(dayAvailability.getAvailability() < 0) {
                throw new NoAvailabilityForDateException();
            }
            availabilityRepository.save(dayAvailability);
        }
    }
}
