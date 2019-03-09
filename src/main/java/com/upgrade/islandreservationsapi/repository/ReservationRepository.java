package com.upgrade.islandreservationsapi.repository;

import com.upgrade.islandreservationsapi.model.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

import javax.persistence.LockModeType;
import java.util.Optional;

public interface ReservationRepository extends JpaRepository<Reservation, Integer> {

    @Lock(LockModeType.PESSIMISTIC_READ)
    Optional<Reservation> findAndLockById(Integer id);

}
