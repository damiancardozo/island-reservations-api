package com.upgrade.islandreservationsapi.config;

import com.upgrade.islandreservationsapi.service.ConfigurationService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

@Component
public class ApplicationStartup implements ApplicationListener<ApplicationReadyEvent> {

    @Autowired
    private ConfigurationService configurationService;

    private final Logger logger = LogManager.getLogger(ApplicationStartup.class);

    @Override
    public void onApplicationEvent(ApplicationReadyEvent applicationReadyEvent) {
        logger.info("Starting application. Setting default values for configurations in case they are not defined.");
        configurationService.getMaxAvailability();
        configurationService.getMaxDateRange();
        configurationService.getMaxAheadDays();
        configurationService.getMinAheadDays();
        configurationService.getMaxReservation();
    }
}
