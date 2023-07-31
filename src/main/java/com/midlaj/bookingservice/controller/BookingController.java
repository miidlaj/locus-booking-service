package com.midlaj.bookingservice.controller;

import com.midlaj.bookingservice.service.BookingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/bookings")
@Slf4j
public class BookingController {

    @Autowired
    private BookingService bookingService;

    @GetMapping("/{roomId}/{checkIn}/{checkOut}")
    public ResponseEntity<?> getAvailability(@PathVariable Long roomId, @PathVariable("checkIn") @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate checkIn, @DateTimeFormat(pattern = "yyyy-MM-dd") @PathVariable("checkOut") LocalDate checkOut) {
        log.info("Inside getAvailability method in Booking controller");

        return bookingService.findAvailableRooms(roomId, checkIn, checkOut);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getAllBookingsOfUser(@PathVariable Long userId) {
        log.info("Inside the getAllBookingsOfUser method of Booking controller");

        return bookingService.findAllBookings(userId);
    }
}
