package com.upgrade.islandreservationsapi.controller;

import com.upgrade.islandreservationsapi.dto.DayAvailabilityDTO;
import com.upgrade.islandreservationsapi.exception.InvalidDatesException;
import com.upgrade.islandreservationsapi.model.DayAvailability;
import com.upgrade.islandreservationsapi.service.DayAvailabilityService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@RestController
public class AvailabilityController {

    @Autowired
    private DayAvailabilityService service;

    @GetMapping("v1/availability")
    @ResponseBody
    public List<DayAvailabilityDTO> getAvailabilities(
            @RequestParam(name = "fromDate", required = false) @DateTimeFormat(pattern = "yyyy/MM/dd") LocalDate fromDate,
            @RequestParam(name = "toDate", required = false) @DateTimeFormat(pattern = "yyyy/MM/dd") LocalDate toDate)
            throws InvalidDatesException {
        if(fromDate == null) {
            fromDate = LocalDate.now().plusDays(1);
        }
        if(toDate == null) {
            toDate = fromDate.plusMonths(1);
        }
        List<DayAvailability> availabilities = service.getAvailabilities(fromDate, toDate);
        final ModelMapper mapper = new ModelMapper();
        return availabilities.stream()
                .map(da -> mapper.map(da, DayAvailabilityDTO.class))
                .collect(Collectors.toList());
    }

}
