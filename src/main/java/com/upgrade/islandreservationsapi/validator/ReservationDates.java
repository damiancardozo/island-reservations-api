package com.upgrade.islandreservationsapi.validator;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Constraint(validatedBy = ReservationDatesValidator.class)
@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface ReservationDates {

    String message() default "Reservation dates are not valid";

    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};

    String startDateField();

    String endDateField();

}
