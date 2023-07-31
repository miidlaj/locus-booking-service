package com.midlaj.bookingservice.service;

import org.springframework.http.ResponseEntity;

import java.time.LocalDate;

public interface BookingService {

    public ResponseEntity<?> findAvailableRooms(Long roomId, LocalDate checkInDate, LocalDate checkOutDate);


    ResponseEntity<?> findAllBookings(Long userId);
}
