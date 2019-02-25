package com.upgrade.islandreservationsapi.service;

import com.upgrade.islandreservationsapi.model.Configuration;
import com.upgrade.islandreservationsapi.repository.ConfigurationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class ConfigurationServiceImpl implements ConfigurationService {

    @Autowired
    private ConfigurationRepository repository;

    private static final int DEFAULT_MAX_AHEAD_DAYS = 30;
    private static final int DEFAULT_MIN_AHEAD_DAYS = 1;
    private static final int DEFAULT_MAX_DURATION = 3;
    private static final int DEFAULT_MAX_AVAILABILITY = 100;
    private static final int DEFAULT_MAX_DATE_RANGE = 30;

    @Override
    public int getMaxAvailability() {
        Optional<Integer> value = getIntegerConfigurationByName(Configuration.CONFIGURATION_NAMES.MAX_AVAILABILITY);
        if(value.isEmpty()) {
            Configuration conf = new Configuration(Configuration.CONFIGURATION_NAMES.MAX_AVAILABILITY.toString(),
                    Integer.toString(DEFAULT_MAX_AVAILABILITY));
            repository.save(conf);
            value = Optional.of(DEFAULT_MAX_AVAILABILITY);
        }
        return value.get();
    }

    @Override
    public int getMaxReservation() {
        Optional<Integer> value = getIntegerConfigurationByName(Configuration.CONFIGURATION_NAMES.MAX_RESERVATION);
        if(value.isEmpty()) {
            Configuration conf = new Configuration(Configuration.CONFIGURATION_NAMES.MAX_RESERVATION.toString(),
                    Integer.toString(DEFAULT_MAX_DURATION));
            repository.save(conf);
            value = Optional.of(DEFAULT_MAX_DURATION);
        }
        return value.get();
    }

    public int getMinAheadDays() {
        Optional<Integer> value = getIntegerConfigurationByName(Configuration.CONFIGURATION_NAMES.MIN_AHEAD);
        if(value.isEmpty()) {
            Configuration conf = new Configuration(Configuration.CONFIGURATION_NAMES.MIN_AHEAD.toString(),
                    Integer.toString(DEFAULT_MIN_AHEAD_DAYS));
            repository.save(conf);
            value = Optional.of(DEFAULT_MIN_AHEAD_DAYS);
        }
        return value.get();
    }

    public int getMaxAheadDays() {
        Optional<Integer> value = getIntegerConfigurationByName(Configuration.CONFIGURATION_NAMES.MAX_AHEAD);
        if(value.isEmpty()) {
            Configuration conf = new Configuration(Configuration.CONFIGURATION_NAMES.MAX_AHEAD.toString(),
                    Integer.toString(DEFAULT_MAX_AHEAD_DAYS));
            repository.save(conf);
            value = Optional.of(DEFAULT_MAX_AHEAD_DAYS);
        }
        return value.get();
    }

    public int getMaxDateRange() {
        Optional<Integer> value = getIntegerConfigurationByName(Configuration.CONFIGURATION_NAMES.MAX_DATE_RANGE);
        if(value.isEmpty()) {
            Configuration conf = new Configuration(Configuration.CONFIGURATION_NAMES.MAX_DATE_RANGE.toString(),
                    Integer.toString(DEFAULT_MAX_DATE_RANGE));
            repository.save(conf);
            value = Optional.of(DEFAULT_MAX_DATE_RANGE);
        }
        return value.get();
    }

    private Optional<Integer> getIntegerConfigurationByName(Configuration.CONFIGURATION_NAMES name) {
        Optional<Configuration> maxValueOpt = repository.findById(name.toString());
        return maxValueOpt.map(c -> Integer.valueOf(c.getValue()));
    }
}
