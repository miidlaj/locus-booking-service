package com.midlaj.bookingservice;

import com.midlaj.bookingservice.service.WalletService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class BookingServiceApplicationTests {

	@Test
	void contextLoads() {
	}

	@Autowired
	private WalletService walletService;

	@Test
	public void createWalletForAdmin() {
		walletService.createNewWallet(Long.valueOf(0));
	}
}
