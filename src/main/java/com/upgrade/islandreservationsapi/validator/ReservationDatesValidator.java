package com.upgrade.islandreservationsapi.validator;

import com.upgrade.islandreservationsapi.service.ConfigurationService;
import org.springframework.beans.BeanWrapper;
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
    private String action;

    @Override
    public void initialize(ReservationDates constraintAnnotation) {
        this.startDateField = constraintAnnotation.startDateField();
        this.endDateField = constraintAnnotation.endDateField();
        this.action = constraintAnnotation.action();
    }

    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {
        BeanWrapper bean = new BeanWrapperImpl(value);
        Object startDateObj = bean.getPropertyValue(startDateField);
        Object endDateObj = bean.getPropertyValue(endDateField);

        context.disableDefaultConstraintViolation();

        if(!(startDateObj instanceof LocalDate) || !(endDateObj instanceof LocalDate)) {
            context.buildConstraintViolationWithTemplate("dates are not valid")
                    .addPropertyNode("start").addConstraintViolation();
            return false;
        }

        final LocalDate startDate = (LocalDate) startDateObj;
        final LocalDate endDate = (LocalDate) endDateObj;

        boolean valid = true;

        final long aheadTime = ChronoUnit.DAYS.between(LocalDate.now(), startDate);
        final int minAheadDays = configurationService.getMinAheadDays();
        if("CREATING".equals(action) && aheadTime < minAheadDays) {
            context.buildConstraintViolationWithTemplate(String.format("start date must be at least %d day(s) in the future.", minAheadDays))
                    .addPropertyNode("start").addConstraintViolation();
            valid = false;
        }
        final int maxAheadDays = configurationService.getMaxAheadDays();
        if(aheadTime > maxAheadDays) {
            context.buildConstraintViolationWithTemplate(String.format("reservations can't be created with more than %d day(s) in advance.", maxAheadDays))
                    .addPropertyNode("start").addConstraintViolation();
            valid = false;
        }

        if(!endDate.isAfter(startDate)) {
            context.buildConstraintViolationWithTemplate("end date must be greater than start date")
                    .addPropertyNode("end").addConstraintViolation();
            valid = false;
        }

        final long reservationDuration = ChronoUnit.DAYS.between(startDate, endDate);
        final int maxReservation = configurationService.getMaxReservation();
        if(reservationDuration > maxReservation) {
            context.buildConstraintViolationWithTemplate(String.format("max duration is %d day(s).", maxReservation))
                    .addPropertyNode("end").addConstraintViolation();
            valid = false;
        }

        return valid;
    }
}
