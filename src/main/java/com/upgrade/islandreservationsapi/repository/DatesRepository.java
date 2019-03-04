package com.upgrade.islandreservationsapi.repository;

import com.upgrade.islandreservationsapi.model.Dates;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.repository.CrudRepository;

import javax.persistence.LockModeType;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface DatesRepository extends CrudRepository<Dates, LocalDate> {

    @Override
    List<Dates> findAll();

    @Override
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    List<Dates> findAllById(Iterable<LocalDate> iterable);

    @Override
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Dates> findById(LocalDate dates);
}
