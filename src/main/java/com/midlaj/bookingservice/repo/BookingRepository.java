package com.midlaj.bookingservice.repo;

import com.midlaj.bookingservice.model.Booking;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.LocalDate;
import java.util.List;

public interface BookingRepository extends MongoRepository<Booking, String> {

    List<Booking> findByRoomIdAndCheckOutDateGreaterThanEqualAndCheckInDateLessThanEqual(Long roomId, LocalDate checkInDate, LocalDate checkOutDate);

    List<Booking> findByUserId(Long userId);
}
