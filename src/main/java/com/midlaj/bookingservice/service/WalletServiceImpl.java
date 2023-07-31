package com.midlaj.bookingservice.service;

import com.midlaj.bookingservice.exception.InsufficientAmountException;
import com.midlaj.bookingservice.exception.WalletNotFoundException;
import com.midlaj.bookingservice.model.Transaction;
import com.midlaj.bookingservice.model.TransactionType;
import com.midlaj.bookingservice.model.Wallet;
import com.midlaj.bookingservice.repo.WalletRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
@Slf4j
public class WalletServiceImpl implements WalletService{

    @Autowired
    private WalletRepository walletRepository;

    @Override
    public Wallet createNewWallet(Long userId) throws WalletNotFoundException {

        Optional<Wallet> walletOptional = walletRepository.findByUserId(userId);
        if (walletOptional.isPresent()) {
            throw new WalletNotFoundException("Wallet with user id " + userId + " Already present");
        }
        List<Transaction> transactions = new ArrayList<>();
        transactions.add(new Transaction(new Date(), TransactionType.WALLET_CREATED, (double) 0, "Wallet Created"));

        Wallet wallet = new Wallet();
        wallet.setBalance((double) 0);
        wallet.setUserId(userId);
        wallet.setTransactions(transactions);
        return walletRepository.save(wallet);
    }

    @Override
    public void addMoney(Long userId, double amount, String description) {
        Optional<Wallet> walletOptional = walletRepository.findByUserId(userId);

        if (walletOptional.isEmpty()) {
            throw new WalletNotFoundException("Wallet not found");
        }

        Wallet wallet = walletOptional.get();

        Transaction transaction = new Transaction();
        transaction.setTimestamp(new Date());
        transaction.setType(TransactionType.DEPOSIT);
        transaction.setAmount(amount);
        transaction.setDescription(description);

        List<Transaction> transactions = wallet.getTransactions();
        transactions.add(transaction);
        wallet.setTransactions(transactions);

        double balance = wallet.getBalance();
        balance += amount;
        wallet.setBalance(balance);

        walletRepository.save(wallet);
    }

    @Override
    public void withdrawMoney(Long userId, double amount, String description) {
        Optional<Wallet> walletOptional = walletRepository.findByUserId(userId);

        if (walletOptional.isEmpty()) {
            throw new WalletNotFoundException("Wallet not found.");
        }

        Wallet wallet = walletOptional.get();

        double balance = wallet.getBalance();
        if (amount > balance) {
            throw new InsufficientAmountException("Insufficient balance.");
        }

        Transaction transaction = new Transaction();
        transaction.setTimestamp(new Date());
        transaction.setType(TransactionType.WITHDRAWAL);
        transaction.setAmount(amount);

        List<Transaction> transactions = wallet.getTransactions();
        transactions.add(transaction);
        wallet.setTransactions(transactions);

        balance -= amount;
        wallet.setBalance(balance);

        walletRepository.save(wallet);
    }

    @Override
    public ResponseEntity<?> getWallet(Long userId) {
        log.info("Inside the getWallet Method in walletServiceImpl");

        Optional<Wallet> walletOptional = walletRepository.findByUserId(userId);
        if (walletOptional.isEmpty()) {
            throw new WalletNotFoundException("Wallet not found");
        }

        return ResponseEntity.ok(walletOptional.get());
    }

    @Scheduled(cron = "0 0 0 1 * ?") // run at midnight on the 1st of every month
    public void withdrawMonthly() {
        System.out.println("Running withdrawMonthly() method");
        // get all wallets
        List<Wallet> wallets = walletRepository.findAll();

        // withdraw amount from each wallet
        for (Wallet wallet : wallets) {
            Double balance = wallet.getBalance();
            if (balance.compareTo((double) 0) > 0) { // check if balance is positive
                // TODO: implement withdrawal logic
                log.warn("Withdrawing money of user " + wallet.getUserId() + " of amount " + balance);
                withdrawMoney(wallet.getUserId(), balance, "Auto withdrawal from company.");
            }
        }

        // log success message
    }


}
