package com.upgrade.islandreservationsapi.repository;

import com.upgrade.islandreservationsapi.model.Reservation;
import org.springframework.data.repository.CrudRepository;

public interface ReservationRepository extends CrudRepository<Reservation, Integer> {

}
