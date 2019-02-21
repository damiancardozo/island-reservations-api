package com.upgrade.islandreservationsapi.service;

import com.upgrade.islandreservationsapi.model.Configuration;
import com.upgrade.islandreservationsapi.repository.ConfigurationRepository;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Optional;

public class ConfigurationServiceImpl implements ConfigurationService {

    @Autowired
    private ConfigurationRepository repository;

    public static final int DEFAULT_MAX_AHEAD_DAYS = 30;
    public static final int DEFAULT_MIN_AHEAD_DAYS = 1;
    public static final int DEFAULT_MAX_DURATION = 3;
    public static final int DEFAULT_MAX_AVAILABILITY = 3;

    @Override
    public int getMaxAvailability() {
        Optional<Integer> value = getIntegerConfigurationByName(Configuration.CONFIGURATION_NAMES.MAX_AVAILABILITY);
        return value.orElse(DEFAULT_MAX_AVAILABILITY);
    }

    @Override
    public int getMaxReservation() {
        Optional<Integer> value = getIntegerConfigurationByName(Configuration.CONFIGURATION_NAMES.MAX_RESERVATION);
        return value.orElse(DEFAULT_MAX_DURATION);
    }

    public int getMinAheadDays() {
        Optional<Integer> value = getIntegerConfigurationByName(Configuration.CONFIGURATION_NAMES.MIN_AHEAD);
        return value.orElse(DEFAULT_MIN_AHEAD_DAYS);
    }

    public int getMaxAheadDays() {
        Optional<Integer> value = getIntegerConfigurationByName(Configuration.CONFIGURATION_NAMES.MAX_AHEAD);
        return value.orElse(DEFAULT_MAX_AHEAD_DAYS);
    }

    private Optional<Integer> getIntegerConfigurationByName(Configuration.CONFIGURATION_NAMES name) {
        Optional<Configuration> maxValueOpt = repository.findById(name.toString());
        return maxValueOpt.map(c -> Integer.valueOf(c.getValue()));
    }
}
