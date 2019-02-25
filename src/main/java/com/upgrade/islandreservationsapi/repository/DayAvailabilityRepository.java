package com.upgrade.islandreservationsapi.repository;

import com.upgrade.islandreservationsapi.model.DayAvailability;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;

public interface DayAvailabilityRepository extends JpaRepository<DayAvailability, LocalDate> {

}
