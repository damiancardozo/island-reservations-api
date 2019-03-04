package com.upgrade.islandreservationsapi.repository;

import com.upgrade.islandreservationsapi.model.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReservationRepository extends JpaRepository<Reservation, Integer> {

}
