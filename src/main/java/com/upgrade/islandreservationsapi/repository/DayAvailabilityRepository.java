package com.upgrade.islandreservationsapi.repository;

import com.upgrade.islandreservationsapi.model.DayAvailability;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

import javax.persistence.LockModeType;
import java.time.LocalDate;
import java.util.List;

public interface DayAvailabilityRepository extends JpaRepository<DayAvailability, LocalDate>, DayAvailabilityRepositoryCustom {

    List<DayAvailability> findByDateBetweenOrderByDateAsc(LocalDate from, LocalDate to);

    @Override
    @Lock(LockModeType.PESSIMISTIC_READ)
    List<DayAvailability> findAllById(Iterable<LocalDate> iterable);

    void refresh(DayAvailability da);

}
