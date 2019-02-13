package com.upgrade.islandreservationsapi.repository;

import com.upgrade.islandreservationsapi.model.DayAvailability;
import org.springframework.data.repository.CrudRepository;

import java.time.LocalDate;

public interface DayAvailabilityRepository extends CrudRepository<DayAvailability, LocalDate> {
}
