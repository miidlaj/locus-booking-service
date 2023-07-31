package com.midlaj.bookingservice.controller;

import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.stereotype.Component;

@Component
@Endpoint(id = " ")
public class CustomActuator {

    @ReadOperation
    public String currentDbDetails() {
        return "Give DB status of the application.";
    }
}
