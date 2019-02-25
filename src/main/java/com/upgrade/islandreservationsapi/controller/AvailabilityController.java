package com.upgrade.islandreservationsapi.controller;

import com.upgrade.islandreservationsapi.model.DayAvailability;
import com.upgrade.islandreservationsapi.service.DayAvailabilityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
public class AvailabilityController {

    @Autowired
    private DayAvailabilityService service;

    @GetMapping("v1/availability")
    @ResponseBody
    public List<DayAvailability> getAvailabilities(
            @RequestParam(name = "fromDate", required = false) @DateTimeFormat(pattern = "MM/dd/yyyy") LocalDate fromDate,
            @RequestParam(name = "toDate", required = false) @DateTimeFormat(pattern = "MM/dd/yyyy") LocalDate toDate) {
        if(fromDate == null) {
            fromDate = LocalDate.now().plusDays(1);
        }
        if(toDate == null) {
            toDate = fromDate.plusMonths(1);
        }
        return service.getAvailabilities(fromDate, toDate);
    }

}
