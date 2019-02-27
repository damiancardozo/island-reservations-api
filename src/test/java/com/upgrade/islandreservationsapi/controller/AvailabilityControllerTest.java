package com.upgrade.islandreservationsapi.controller;

import com.upgrade.islandreservationsapi.exception.InvalidDatesException;
import com.upgrade.islandreservationsapi.model.DayAvailability;
import com.upgrade.islandreservationsapi.service.ConfigurationService;
import com.upgrade.islandreservationsapi.service.DayAvailabilityService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@WebMvcTest(AvailabilityController.class)
public class AvailabilityControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private DayAvailabilityService availabilityService;

    @MockBean
    private ConfigurationService configurationService;

    private static final int DEFAULT_MAX_AVAILABILITY = 100;
    private static final int DEFAULT_MAX_DATE_RANGE = 30;

    @Before
    public void initialize() {
        given(configurationService.getMaxDateRange()).willReturn(DEFAULT_MAX_DATE_RANGE);
    }

    @Test
    public void testGetAvailability() throws Exception {
        LocalDate fromDate = LocalDate.now().plusDays(1);
        LocalDate toDate = LocalDate.now().plusDays(10);
        List<DayAvailability> availabilities = new ArrayList<>();
        for(int i = 1; i <= 10; i++) {
            availabilities.add(new DayAvailability(LocalDate.now().plusDays(i), DEFAULT_MAX_AVAILABILITY, DEFAULT_MAX_AVAILABILITY));
        }

        given(availabilityService.getAvailabilities(fromDate, toDate)).willReturn(availabilities);

        String format = "yyyy/MM/dd";
        mvc.perform(get("/v1/availability")
                .param("fromDate", fromDate.format(DateTimeFormatter.ofPattern(format)))
                .param("toDate", toDate.format(DateTimeFormatter.ofPattern(format)))
                .contentType(MediaType.APPLICATION_JSON)
                .characterEncoding("UTF-8"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()", is(10)))
                .andDo(print());
    }

    @Test
    public void testGetAvailabilityInvalidDates() throws Exception {
        LocalDate fromDate = LocalDate.now().plusDays(4);
        LocalDate toDate = LocalDate.now().plusDays(2);

        given(availabilityService.getAvailabilities(fromDate, toDate))
                .willThrow(new InvalidDatesException("toDate must be after fromDate."));

        String format = "yyyy/MM/dd";
        mvc.perform(get("/v1/availability")
                .param("fromDate", fromDate.format(DateTimeFormatter.ofPattern(format)))
                .param("toDate", toDate.format(DateTimeFormatter.ofPattern(format)))
                .contentType(MediaType.APPLICATION_JSON)
                .characterEncoding("UTF-8"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", is("toDate must be after fromDate.")))
                .andDo(print());
    }

    @Test
    public void testGetAvailabilityInvalidDateFormat() throws Exception {
        LocalDate fromDate = LocalDate.now().plusDays(4);
        LocalDate toDate = LocalDate.now().plusDays(2);

        String format = "yyyy-MM-dd";
        mvc.perform(get("/v1/availability")
                .param("fromDate", fromDate.format(DateTimeFormatter.ofPattern(format)))
                .param("toDate", toDate.format(DateTimeFormatter.ofPattern(format)))
                .contentType(MediaType.APPLICATION_JSON)
                .characterEncoding("UTF-8"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", is("fromDate must have format yyyy/MM/dd")))
                .andDo(print());
    }


}
