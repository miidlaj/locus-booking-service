package com.midlaj.bookingservice.controller;

import com.midlaj.bookingservice.exception.InsufficientAmountException;
import com.midlaj.bookingservice.exception.WalletNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class CustomExceptionHandler {

    @ExceptionHandler(WalletNotFoundException.class)
    public ResponseEntity<?> handleEntityNotFoundException(WalletNotFoundException ex) {

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }

    @ExceptionHandler(InsufficientAmountException.class)
    public ResponseEntity<?> handleAlreadyPresentWithNameException(InsufficientAmountException ex) {

        return ResponseEntity.status(HttpStatus.CONFLICT).body(ex.getMessage());
    }


}
