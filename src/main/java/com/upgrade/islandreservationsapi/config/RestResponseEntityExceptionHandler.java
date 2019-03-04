package com.upgrade.islandreservationsapi.config;

import com.upgrade.islandreservationsapi.dto.ApiError;
import com.upgrade.islandreservationsapi.dto.ApiFieldError;
import com.upgrade.islandreservationsapi.exception.*;
import org.springframework.beans.ConversionNotSupportedException;
import org.springframework.beans.TypeMismatchException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@ControllerAdvice
public class RestResponseEntityExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(value
            = { ReservationNotFoundException.class})
    protected ResponseEntity<Object> handleNotFound(
            ReservationNotFoundException ex, WebRequest request) {
        ApiError error = new ApiError(HttpStatus.NOT_FOUND, ex.getMessage());
        return handleExceptionInternal(ex, error,
                new HttpHeaders(), error.getStatus(), request);
    }

    @ExceptionHandler(value
            = { ReservationAlreadyCancelledException.class})
    protected ResponseEntity<Object> handleAlreadyCancelled(
            ReservationAlreadyCancelledException ex, WebRequest request) {
        ApiError error = new ApiError(HttpStatus.BAD_REQUEST, ex.getMessage());
        return handleExceptionInternal(ex, error,
                new HttpHeaders(), error.getStatus(), request);
    }

    @ExceptionHandler(value
            = { NoAvailabilityForDateException.class})
    protected ResponseEntity<Object> handleNoAvailability(
            NoAvailabilityForDateException ex, WebRequest request) {
        ApiError error = new ApiError(HttpStatus.BAD_REQUEST, ex.getMessage());
        return handleExceptionInternal(ex, error,
                new HttpHeaders(), error.getStatus(), request);
    }

    @ExceptionHandler(value
            = { InvalidDatesException.class})
    protected ResponseEntity<Object> handleInvalidDates(
            InvalidDatesException ex, WebRequest request) {
        ApiError error = new ApiError(HttpStatus.BAD_REQUEST, ex.getMessage());
        return handleExceptionInternal(ex, error,
                new HttpHeaders(), error.getStatus(), request);
    }

    @ExceptionHandler(value
            = { ReservationCancelledException.class})
    protected ResponseEntity<Object> handleReservationCancelled(
            ReservationCancelledException ex, WebRequest request) {
        ApiError error = new ApiError(HttpStatus.BAD_REQUEST, ex.getMessage());
        return handleExceptionInternal(ex, error,
                new HttpHeaders(), error.getStatus(), request);
    }

    @ExceptionHandler(value
            = { StatusChangeNotAllowedException.class})
    protected ResponseEntity<Object> handleStatusChangeNotAllowed(
            StatusChangeNotAllowedException ex, WebRequest request) {
        ApiError error = new ApiError(HttpStatus.BAD_REQUEST, ex.getMessage());
        return handleExceptionInternal(ex, error,
                new HttpHeaders(), error.getStatus(), request);
    }

    @ExceptionHandler(value
            = { ObjectOptimisticLockingFailureException.class})
    protected ResponseEntity<Object> handleOptimisticLock(
            ObjectOptimisticLockingFailureException ex, WebRequest request) {
        ApiError error = new ApiError(HttpStatus.CONFLICT, "Record was updated by another client at the same time.");
        return handleExceptionInternal(ex, error,
                new HttpHeaders(), error.getStatus(), request);
    }

    @ExceptionHandler({ ConstraintViolationException.class })
    public ResponseEntity<Object> handleConstraintViolation(
            ConstraintViolationException ex, WebRequest request) {
        List<ApiFieldError> errors = new ArrayList<>();
        for (ConstraintViolation<?> violation : ex.getConstraintViolations()) {
            errors.add(new ApiFieldError(violation.getRootBeanClass().getName()
                    + "/" + violation.getPropertyPath().toString(), violation.getMessage()));
        }

        ApiError apiError =
                new ApiError(HttpStatus.BAD_REQUEST, "Validation failed", errors);
        return new ResponseEntity<>(
                apiError, new HttpHeaders(), apiError.getStatus());
    }

    @ExceptionHandler({ Exception.class })
    public ResponseEntity<Object> handleAll(Exception ex, WebRequest request) {
        ApiError apiError = new ApiError(
                HttpStatus.INTERNAL_SERVER_ERROR,  ex.getLocalizedMessage());
        return new ResponseEntity<>(
                apiError, new HttpHeaders(), apiError.getStatus());
    }

    @Override
    protected ResponseEntity<Object> handleConversionNotSupported(ConversionNotSupportedException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
        ApiError apiError =
                new ApiError(status, ex.getLocalizedMessage());
        return new ResponseEntity<>(
                apiError, new HttpHeaders(), apiError.getStatus());
    }

    @Override
    protected ResponseEntity<Object> handleNoHandlerFoundException(NoHandlerFoundException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
        ApiError apiError =
                new ApiError(status, ex.getLocalizedMessage());
        return new ResponseEntity<>(
                apiError, new HttpHeaders(), apiError.getStatus());
    }

    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(HttpMessageNotReadableException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
        ApiError apiError =
                new ApiError(HttpStatus.BAD_REQUEST, ex.getLocalizedMessage());
        return new ResponseEntity<>(
                apiError, new HttpHeaders(), apiError.getStatus());
    }

    @Override
    protected ResponseEntity<Object> handleServletRequestBindingException(ServletRequestBindingException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
        ApiError apiError =
                new ApiError(HttpStatus.BAD_REQUEST, "Servlet binding error: " + ex.getLocalizedMessage());
        return new ResponseEntity<>(
                apiError, new HttpHeaders(), apiError.getStatus());
    }

    @Override
    protected ResponseEntity<Object> handleMissingServletRequestParameter(
            MissingServletRequestParameterException ex, HttpHeaders headers,
            HttpStatus status, WebRequest request) {
        String error = ex.getParameterName() + " parameter is missing";

        ApiError apiError =
                new ApiError(HttpStatus.BAD_REQUEST, error);
        return new ResponseEntity<>(
                apiError, new HttpHeaders(), apiError.getStatus());
    }

    @Override
    protected ResponseEntity<Object> handleTypeMismatch(TypeMismatchException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
        StringBuilder error = new StringBuilder();
        if(ex instanceof MethodArgumentTypeMismatchException) {
            MethodArgumentTypeMismatchException matme = (MethodArgumentTypeMismatchException) ex;
            error.append(matme.getName());
        } else if(ex.getPropertyName() != null) {
            error.append("Property ").append(ex.getPropertyName());
        } else {
            error.append("Field");
        }

        if(ex.getRequiredType() != null && ex.getRequiredType().equals(LocalDate.class)) {
            error.append(" must have format yyyy/MM/dd");
        } else if(ex.getRequiredType() != null) {
            error.append(" must be of type ")
                    .append(ex.getRequiredType().getName());
        } else {
            error.append(" has invalid value.");
        }

        ApiError apiError =
                new ApiError(HttpStatus.BAD_REQUEST, error.toString());
        return new ResponseEntity<>(
                apiError, new HttpHeaders(), apiError.getStatus());
    }

    @Override
    protected ResponseEntity<Object> handleBindException(BindException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
        List<ApiFieldError> errors = new ArrayList<>();
        for(FieldError error: ex.getFieldErrors()) {
            errors.add(new ApiFieldError(error.getField(), error.getDefaultMessage()));
        }
        for(ObjectError error: ex.getGlobalErrors()) {
            errors.add(new ApiFieldError(error.getObjectName(), error.getDefaultMessage()));
        }
        ApiError apiError =
                new ApiError(HttpStatus.BAD_REQUEST, "Binding error", errors);
        return new ResponseEntity<>(
                apiError, new HttpHeaders(), apiError.getStatus());
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex,
            HttpHeaders headers,
            HttpStatus status,
            WebRequest request) {
        List<ApiFieldError> errors = new ArrayList<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            errors.add(new ApiFieldError(error.getField(), error.getDefaultMessage()));
        }
        for (ObjectError error : ex.getBindingResult().getGlobalErrors()) {
            errors.add(new ApiFieldError(error.getObjectName(), error.getDefaultMessage()));
        }

        ApiError apiError =
                new ApiError(HttpStatus.BAD_REQUEST, "Validation failed", errors);
        return handleExceptionInternal(
                ex, apiError, headers, apiError.getStatus(), request);
    }

    @Override
    protected ResponseEntity<Object> handleHttpRequestMethodNotSupported(
            HttpRequestMethodNotSupportedException ex,
            HttpHeaders headers,
            HttpStatus status,
            WebRequest request) {
        String builder = ex.getMethod() +
                " method is not supported for this request.";
        ApiError apiError = new ApiError(HttpStatus.METHOD_NOT_ALLOWED,
                builder);
        return new ResponseEntity<>(
                apiError, new HttpHeaders(), apiError.getStatus());
    }

    @Override
    protected ResponseEntity<Object> handleHttpMediaTypeNotSupported(
            HttpMediaTypeNotSupportedException ex,
            HttpHeaders headers,
            HttpStatus status,
            WebRequest request) {
        StringBuilder builder = new StringBuilder();
        builder.append(ex.getContentType());
        builder.append(" media type is not supported. Supported media types are ");
        ex.getSupportedMediaTypes().forEach(t -> builder.append(t).append(", "));

        ApiError apiError = new ApiError(HttpStatus.UNSUPPORTED_MEDIA_TYPE,
                builder.substring(0, builder.length() - 2));
        return new ResponseEntity<>(
                apiError, new HttpHeaders(), apiError.getStatus());
    }

}
