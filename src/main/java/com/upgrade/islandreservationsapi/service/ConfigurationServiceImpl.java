package com.upgrade.islandreservationsapi.service;

import com.upgrade.islandreservationsapi.model.Configuration;
import com.upgrade.islandreservationsapi.repository.ConfigurationRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
    private static final int DEFAULT_MAX_DATE_RANGE = 30;
    private static final String MISSING_RECORD_LOG_TEMPLATE = "Value for {} not found in database. Inserting row with default value {}";

    @Override
    public int getMaxAvailability() {
        Optional<Integer> value = getIntegerConfigurationByName(Configuration.CONFIGURATION_NAMES.MAX_AVAILABILITY);
        if(value.isEmpty()) {
            logger.info(MISSING_RECORD_LOG_TEMPLATE,
                    Configuration.CONFIGURATION_NAMES.MAX_AVAILABILITY.toString(), DEFAULT_MAX_AVAILABILITY);
            Configuration conf = new Configuration(Configuration.CONFIGURATION_NAMES.MAX_AVAILABILITY.toString(),
                    Integer.toString(DEFAULT_MAX_AVAILABILITY));
            repository.save(conf);
        }
        return value.orElse(DEFAULT_MAX_AVAILABILITY);
    }

    @Override
    public int getMaxReservation() {
        Optional<Integer> value = getIntegerConfigurationByName(Configuration.CONFIGURATION_NAMES.MAX_RESERVATION);
        if(value.isEmpty()) {
            logger.info(MISSING_RECORD_LOG_TEMPLATE,
                    Configuration.CONFIGURATION_NAMES.MAX_RESERVATION.toString(), DEFAULT_MAX_DURATION);
            Configuration conf = new Configuration(Configuration.CONFIGURATION_NAMES.MAX_RESERVATION.toString(),
                    Integer.toString(DEFAULT_MAX_DURATION));
            repository.save(conf);
        }
        return value.orElse(DEFAULT_MAX_DURATION);
    }

    public int getMinAheadDays() {
        Optional<Integer> value = getIntegerConfigurationByName(Configuration.CONFIGURATION_NAMES.MIN_AHEAD);
        if(value.isEmpty()) {
            logger.info(MISSING_RECORD_LOG_TEMPLATE,
                    Configuration.CONFIGURATION_NAMES.MIN_AHEAD.toString(), DEFAULT_MIN_AHEAD_DAYS);
            Configuration conf = new Configuration(Configuration.CONFIGURATION_NAMES.MIN_AHEAD.toString(),
                    Integer.toString(DEFAULT_MIN_AHEAD_DAYS));
            repository.save(conf);
        }
        return value.orElse(DEFAULT_MIN_AHEAD_DAYS);
    }

    public int getMaxAheadDays() {
        Optional<Integer> value = getIntegerConfigurationByName(Configuration.CONFIGURATION_NAMES.MAX_AHEAD);
        if(value.isEmpty()) {
            logger.info(MISSING_RECORD_LOG_TEMPLATE,
                    Configuration.CONFIGURATION_NAMES.MAX_AHEAD.toString(), DEFAULT_MAX_AHEAD_DAYS);
            Configuration conf = new Configuration(Configuration.CONFIGURATION_NAMES.MAX_AHEAD.toString(),
                    Integer.toString(DEFAULT_MAX_AHEAD_DAYS));
            repository.save(conf);
        }
        return value.orElse(DEFAULT_MAX_AHEAD_DAYS);
    }

    public int getMaxDateRange() {
        Optional<Integer> value = getIntegerConfigurationByName(Configuration.CONFIGURATION_NAMES.MAX_DATE_RANGE);
        if(value.isEmpty()) {
            logger.info(MISSING_RECORD_LOG_TEMPLATE,
                    Configuration.CONFIGURATION_NAMES.MAX_DATE_RANGE.toString(), DEFAULT_MAX_DATE_RANGE);
            Configuration conf = new Configuration(Configuration.CONFIGURATION_NAMES.MAX_DATE_RANGE.toString(),
                    Integer.toString(DEFAULT_MAX_DATE_RANGE));
            repository.save(conf);
        }
        return value.orElse(DEFAULT_MAX_DATE_RANGE);
    }

    private Optional<Integer> getIntegerConfigurationByName(Configuration.CONFIGURATION_NAMES name) {
        Optional<Configuration> maxValueOpt = repository.findById(name.toString());
        return maxValueOpt.map(c -> Integer.valueOf(c.getValue()));
    }
}
