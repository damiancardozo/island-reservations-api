package com.upgrade.islandreservationsapi.integration;

import com.upgrade.islandreservationsapi.service.ConfigurationService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@SpringBootTest
@AutoConfigureMockMvc
@RunWith(SpringRunner.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class DayAvailabilityControllerIntegrationTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ConfigurationService configService;

    private int defaultRange;
    private  int maxRange;
    private int minAheadDays;

    @Before
    public void init() {
        defaultRange = configService.getDefaultDateRange();
        maxRange = configService.getMaxDateRange();
        minAheadDays = configService.getMinAheadDays();
    }

    @Test
    public void getAvailabilitiesNoParameters() throws Exception {

        mvc.perform(get("/v1/availability")
                .contentType(MediaType.APPLICATION_JSON)
                .characterEncoding("UTF-8"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(defaultRange)));
    }

    @Test
    public void getAvailabilitiesValidRange() throws Exception {
        LocalDate d1 = LocalDate.now().plusDays(minAheadDays);
        LocalDate d2 = d1.plusDays(maxRange - 1);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        mvc.perform(get("/v1/availability?fromDate=" + d1.format(formatter) + "&toDate=" + d2.format(formatter))
                .contentType(MediaType.APPLICATION_JSON)
                .characterEncoding("UTF-8"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(maxRange)));
    }

    @Test
    public void getAvailabilitiesInvalidRange() throws Exception {

        LocalDate d1 = LocalDate.now().plusDays(minAheadDays);
        LocalDate d2 = d1.plusDays(maxRange);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        mvc.perform(get("/v1/availability?fromDate=" + d1.format(formatter) + "&toDate=" + d2.format(formatter))
                .contentType(MediaType.APPLICATION_JSON)
                .characterEncoding("UTF-8"))
                .andExpect(status().isBadRequest());
    }

}
