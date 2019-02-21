package com.upgrade.islandreservationsapi.validator;

import com.upgrade.islandreservationsapi.service.ConfigurationService;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Component
public class ReservationDatesValidator implements ConstraintValidator<ReservationDates, Object> {

    @Autowired
    private ConfigurationService configurationService;

    private String startDateField;
    private String endDateField;

    @Override
    public void initialize(ReservationDates constraintAnnotation) {
        this.startDateField = constraintAnnotation.startDateField();
        this.endDateField = constraintAnnotation.endDateField();
    }

    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {
        Object startDateObj = new BeanWrapperImpl(value)
                .getPropertyValue(startDateField);
        Object endDateObj = new BeanWrapperImpl(value)
                .getPropertyValue(endDateField);

        if(!(startDateObj instanceof LocalDate) || !(endDateObj instanceof LocalDate)) {
            return false;
        }

        context.disableDefaultConstraintViolation();

        LocalDate startDate = (LocalDate) startDateObj;
        LocalDate endDate = (LocalDate) endDateObj;

        boolean valid = true;

        long aheadTime = ChronoUnit.DAYS.between(LocalDate.now(), startDate);
        int minAheadDays = configurationService.getMinAheadDays();
        if(aheadTime < minAheadDays) {
            context.buildConstraintViolationWithTemplate(String.format("Reservations must be created at least %d day(s) ahead of arrival.", minAheadDays))
                    .addConstraintViolation();
            valid = false;
        }
        int maxAheadDays = configurationService.getMaxAheadDays();
        if(aheadTime > maxAheadDays) {
            context.buildConstraintViolationWithTemplate(String.format("Reservations can be created up to %d day(s) in advance.", maxAheadDays))
                    .addConstraintViolation();
            valid = false;
        }

        if(!endDate.isAfter(startDate)) {
            context.buildConstraintViolationWithTemplate("End date must be greater than start date").addConstraintViolation();
            valid = false;
        }

        long reservationDuration = ChronoUnit.DAYS.between(startDate, endDate);
        int maxReservation = configurationService.getMaxReservation();
        if(reservationDuration > maxReservation) {
            context.buildConstraintViolationWithTemplate(String.format("Max duration is %d day(s).", maxReservation))
                    .addConstraintViolation();
            valid = false;
        }

        return valid;
    }
}
