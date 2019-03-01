package com.upgrade.islandreservationsapi.repository;

import com.upgrade.islandreservationsapi.model.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

import javax.persistence.LockModeType;

public interface ReservationRepository extends JpaRepository<Reservation, Integer> {

    @Override
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    <S extends Reservation> S save(S s);
}
