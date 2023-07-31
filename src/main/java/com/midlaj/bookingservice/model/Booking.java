package com.midlaj.bookingservice.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.util.Date;

@Document(collection = "bookings")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Booking {

    @Id
    private String id;

    private Long userId;

    private Long resortId;

    private Long roomId;

    private LocalDate checkInDate;

    private LocalDate checkOutDate;

    private Date bookedDate;

    private Double price;

    private Double handlingCharge;

    private Double totalPrice;

    private PaymentDetails paymentDetails;
}
