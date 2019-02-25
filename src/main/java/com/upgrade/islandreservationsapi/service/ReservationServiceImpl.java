package com.upgrade.islandreservationsapi.service;

import com.upgrade.islandreservationsapi.exception.*;
import com.upgrade.islandreservationsapi.model.DayAvailability;
import com.upgrade.islandreservationsapi.model.Reservation;
import com.upgrade.islandreservationsapi.repository.DayAvailabilityRepository;
import com.upgrade.islandreservationsapi.repository.ReservationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
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
    public Reservation createReservation(Reservation reservation) throws NoAvailabilityForDateException {

        updateAvailability(reservation);
        return reservationRepository.save(reservation);
    }

    @Override
    public Reservation updateReservation(Reservation reservation)
            throws NoAvailabilityForDateException, ReservationNotFoundException {

        Reservation oldReservation = reservationRepository.findById(reservation.getId()).orElseThrow(ReservationNotFoundException::new);
        reservationRepository.save(reservation);

        boolean datesChanged = !oldReservation.getStart().equals(reservation.getStart())
                || !oldReservation.getEnd().equals(reservation.getEnd());
        boolean numberChanged = !oldReservation.getNumberOfPersons().equals(reservation.getNumberOfPersons());

        if(!datesChanged && !numberChanged) {
            return reservation;
        }
        List<LocalDate> oldDates = oldReservation.getStart().datesUntil(oldReservation.getEnd().plusDays(1)).collect(Collectors.toList());
        List<DayAvailability> availabilities = availabilityRepository.findAllById(oldDates);
        if(!datesChanged) {
            availabilities.forEach(a -> a.setAvailability(oldReservation.getNumberOfPersons() - reservation.getNumberOfPersons()));
            availabilityRepository.saveAll(availabilities);
        } else {
            availabilities.forEach(a -> a.setAvailability(a.getAvailability() + oldReservation.getNumberOfPersons()));
            availabilityRepository.saveAll(availabilities);
            updateAvailability(reservation);
        }
        return reservation;
    }

    @Override
    public Reservation cancelReservation(Integer id)
            throws ReservationNotFoundException, ReservationAlreadyCancelledException {
        Optional<Reservation> reservationOpt = reservationRepository.findById(id);
        Reservation reservation = reservationOpt.orElseThrow(ReservationNotFoundException::new);
        if(reservation.getStatus() == Reservation.Status.CANCELLED) {
            throw new ReservationAlreadyCancelledException(id);
        }
        reservation.setStatus(Reservation.Status.CANCELLED);
        reservation = reservationRepository.save(reservation);

        // update availability

        List<LocalDate> oldDates = reservation.getStart().datesUntil(reservation.getEnd().plusDays(1)).collect(Collectors.toList());
        List<DayAvailability> availabilities = availabilityRepository.findAllById(oldDates);
        for(DayAvailability availability: availabilities) {
            availability.setAvailability(availability.getAvailability() + reservation.getNumberOfPersons());
        }
        availabilityRepository.saveAll(availabilities);

        return reservation;
    }

    private void updateAvailability(Reservation reservation)
            throws NoAvailabilityForDateException {
        List<LocalDate> dates = reservation.getStart().datesUntil(reservation.getEnd().plusDays(1)).collect(Collectors.toList());
        List<DayAvailability> availabilities = availabilityRepository.findAllById(dates);
        Map<LocalDate, DayAvailability> availabilityMap = availabilities.stream()
                .collect(Collectors.toMap(DayAvailability::getDate, v -> v));
        for(LocalDate date: dates) {
            Optional<DayAvailability> availabilityOpt = Optional.ofNullable(availabilityMap.get(date));
            int maxOccupancy = configurationService.getMaxAvailability();
            DayAvailability dayAvailability = availabilityOpt.orElse(new DayAvailability(date, maxOccupancy, maxOccupancy));
            dayAvailability.setAvailability(dayAvailability.getAvailability() - reservation.getNumberOfPersons());
            if(dayAvailability.getAvailability() < 0) {
                throw new NoAvailabilityForDateException();
            }
            availabilities.add(dayAvailability);
        }
        availabilityRepository.saveAll(availabilities);
    }

}
