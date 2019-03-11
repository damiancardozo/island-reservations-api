package com.upgrade.islandreservationsapi.service;

import com.upgrade.islandreservationsapi.model.Configuration;
import com.upgrade.islandreservationsapi.repository.ConfigurationRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

@Service
public class ConfigurationServiceImpl implements ConfigurationService {

    @Autowired
    private ConfigurationRepository repository;

    private final Logger logger = LogManager.getLogger(ConfigurationServiceImpl.class);

    private static final int DEFAULT_MAX_AHEAD_DAYS = 30;
    private static final int DEFAULT_MIN_AHEAD_DAYS = 1;
    private static final int DEFAULT_MAX_DURATION = 3;
    private static final int DEFAULT_MAX_AVAILABILITY = 100;
    private static final int DEFAULT_MAX_DATE_RANGE = 90;
    private static final int DEFAULT_DEFAULT_DATE_RANGE = 30;
    private static final String MISSING_RECORD_LOG_TEMPLATE = "Value for {} not found in database. Inserting row with default value {}";

    @Override
    public int getMaxAvailability() {
        return getOrSaveIntegerConfiguration(Configuration.CONFIGURATION_NAMES.MAX_AVAILABILITY, DEFAULT_MAX_AVAILABILITY, false);
    }

    @Override
    public int getMaxReservation() {
        return getOrSaveIntegerConfiguration(Configuration.CONFIGURATION_NAMES.MAX_RESERVATION, DEFAULT_MAX_DURATION, true);
    }

    @Override
    public int getMinAheadDays() {
        return getOrSaveIntegerConfiguration(Configuration.CONFIGURATION_NAMES.MIN_AHEAD, DEFAULT_MIN_AHEAD_DAYS, true);
    }

    @Override
    public int getMaxAheadDays() {
        return getOrSaveIntegerConfiguration(Configuration.CONFIGURATION_NAMES.MAX_AHEAD, DEFAULT_MAX_AHEAD_DAYS, true);
    }

    @Override
    public int getMaxDateRange() {
        return getOrSaveIntegerConfiguration(Configuration.CONFIGURATION_NAMES.MAX_DATE_RANGE, DEFAULT_MAX_DATE_RANGE, true);
    }

    @Override
    public int getDefaultDateRange() {
        return getOrSaveIntegerConfiguration(Configuration.CONFIGURATION_NAMES.DEFAULT_DATE_RANGE, DEFAULT_DEFAULT_DATE_RANGE, true);
    }

    private int getOrSaveIntegerConfiguration(Configuration.CONFIGURATION_NAMES name, Integer defaultValue, boolean updateDays) {
        final Optional<Integer> valueOpt = getIntegerConfigurationByName(name);
        if(valueOpt.isEmpty()) {
            logger.info(MISSING_RECORD_LOG_TEMPLATE,
                    name.toString(), defaultValue);
            Configuration conf = new Configuration(name.toString(),
                    Integer.toString(defaultValue));
            repository.save(conf);
        }
        int value = valueOpt.orElse(defaultValue);
        if(updateDays && value % 30 == 0) {
            // if max date range is multiple of 30, then treat it as a month. so update it based on this month's number of days
            value = (int) ChronoUnit.DAYS.between(LocalDate.now(), LocalDate.now().plusMonths(value / 30));
            logger.debug("updated value of property {} to {} based on the number of days in the month(s).", name.toString(), value);
        }
        return value;
    }

    private Optional<Integer> getIntegerConfigurationByName(Configuration.CONFIGURATION_NAMES name) {
        final Optional<Configuration> maxValueOpt = repository.findById(name.toString());
        return maxValueOpt.map(c -> Integer.valueOf(c.getValue()));
    }
}
