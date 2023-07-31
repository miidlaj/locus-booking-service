package com.midlaj.bookingservice.service;

import com.midlaj.bookingservice.model.Booking;
import com.midlaj.bookingservice.repo.BookingRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@Slf4j
public class BookingServiceImpl implements BookingService{

    @Autowired
    private BookingRepository bookingRepository;


    @Override
    public ResponseEntity<?> findAvailableRooms(Long roomId, LocalDate checkInDate, LocalDate checkOutDate) {
        log.info("Inside the findAvailableRooms method in BookingServiceImpl");
        List<Booking> bookings = bookingRepository.findByRoomIdAndCheckOutDateGreaterThanEqualAndCheckInDateLessThanEqual(roomId, checkInDate, checkOutDate);

        return ResponseEntity.ok(bookings);
    }

    @Override
    public ResponseEntity<?> findAllBookings(Long userId) {
        log.info("Inside the findAllBookings method in BookingServiceImpl");
        List<Booking> bookings = bookingRepository.findByUserId(userId);

        return ResponseEntity.ok(bookings);
    }
}
