package com.upgrade.islandreservationsapi.repository;

import com.upgrade.islandreservationsapi.model.DayAvailability;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

public class DayAvailabilityRepositoryCustomImpl implements DayAvailabilityRepositoryCustom {

    @PersistenceContext
    private EntityManager em;

    @Override
    public void refresh(DayAvailability da) {
        em.refresh(da);
    }
}
