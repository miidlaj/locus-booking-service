package com.midlaj.bookingservice.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PaymentDetails {

    private Date paymentDate;
    private String orderId;
    private String paymentId;
    private String signature;
    private PaymentMethod paymentMethod;
    private PaymentStatus paymentStatus;
    private Double paymentTotal;

}
