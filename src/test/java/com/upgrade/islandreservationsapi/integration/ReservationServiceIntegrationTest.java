package com.upgrade.islandreservationsapi.integration;

import com.upgrade.islandreservationsapi.exception.NoAvailabilityForDateException;
import com.upgrade.islandreservationsapi.model.Reservation;
import com.upgrade.islandreservationsapi.repository.ReservationRepository;
import com.upgrade.islandreservationsapi.service.ReservationService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.LocalDate;
import java.util.LinkedList;
import java.util.List;

@SpringBootTest
@RunWith(SpringRunner.class)
public class ReservationServiceIntegrationTest {

    @Autowired
    private ReservationService service;
    @Autowired
    private ReservationRepository repository;

    @Test
    public void testConcurrentCreateReservation() throws InterruptedException {
        Reservation reservation = new Reservation("John", "Oliver", "johno@gmail.com",
                LocalDate.now().plusDays(4), LocalDate.now().plusDays(6), 30);
        runMultithreaded(() -> {
            try {
                service.createReservation(reservation);
            } catch (NoAvailabilityForDateException e) {
                System.out.println("no availability for date!");
            }
        }, 5);
    }

    public static void runMultithreaded(Runnable  runnable, int threadCount) throws InterruptedException {
        List<Thread> threadList = new LinkedList<>();

        for(int i = 0 ; i < threadCount; i++) {
            threadList.add(new Thread(runnable));
        }

        for( Thread t :  threadList) {
            t.start();
        }

        for( Thread t :  threadList) {
            t.join();
        }
    }
}
