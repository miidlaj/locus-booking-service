package com.midlaj.bookingservice.controller;

import com.midlaj.bookingservice.model.Wallet;
import com.midlaj.bookingservice.service.WalletService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/wallet")
@Slf4j
public class WalletController {

    @Autowired
    private WalletService walletService;


    @GetMapping("/create/{userId}")
    public ResponseEntity<?> createWallet(@PathVariable Long userId) {
        log.info("Inside the createWallet method of WalletController");

        walletService.createNewWallet(userId);
        return ResponseEntity.status(HttpStatus.CREATED).body("Wallet created for user with id " + userId);
    }

    @GetMapping("/{userId}")
    public ResponseEntity<?> getWalletUsingUserId(@PathVariable Long userId) {
        log.info("Inside the getWalletUsingUserId method of WalletController");

        return walletService.getWallet(userId);
    }

    @GetMapping("/admin")
    public ResponseEntity<?> getWalletForAdmin() {
        log.info("Inside the getWalletForAdmin method of WalletController");

        return walletService.getWallet(Long.valueOf(0));
    }

}
