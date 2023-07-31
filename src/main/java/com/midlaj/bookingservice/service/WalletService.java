package com.midlaj.bookingservice.service;


import com.midlaj.bookingservice.exception.WalletNotFoundException;
import com.midlaj.bookingservice.model.Wallet;
import org.springframework.http.ResponseEntity;

public interface WalletService {

    Wallet createNewWallet(Long userId) throws WalletNotFoundException;

    void addMoney(Long userId, double amount, String description);

    void withdrawMoney(Long userId, double amount, String description);

    ResponseEntity<?> getWallet(Long userId);
}
