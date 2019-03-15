package com.upgrade.islandreservationsapi.repository;

import com.upgrade.islandreservationsapi.dto.Occupancy;
import com.upgrade.islandreservationsapi.model.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.LockModeType;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ReservationRepository extends JpaRepository<Reservation, Integer> {

    @Lock(LockModeType.PESSIMISTIC_READ)
    Optional<Reservation> findAndLockById(Integer id);

    @Transactional(readOnly = true)
    @Query("SELECT new com.upgrade.islandreservationsapi.dto.Occupancy(rd.id.date, sum(rd.numberOfPersons)) " +
            "FROM ReservationDay rd " +
            "WHERE rd.id.date >= ?1 AND rd.id.date < ?2 " +
            "GROUP BY rd.id.date")
    List<Occupancy> calculateOccupancyByRange(LocalDate start, LocalDate end);

    @Transactional(readOnly = true)
    @Query("SELECT new com.upgrade.islandreservationsapi.dto.Occupancy(rd.id.date, sum(rd.numberOfPersons)) " +
            "FROM ReservationDay rd INNER JOIN rd.id.reservation r " +
            "WHERE rd.id.date IN ?1 AND r.status = 'ACTIVE' AND r.id != ?2 " +
            "GROUP BY rd.id.date")
    List<Occupancy> calculateOccupancyExcludeReservation(List<LocalDate> dates, Integer reservationId);

}
