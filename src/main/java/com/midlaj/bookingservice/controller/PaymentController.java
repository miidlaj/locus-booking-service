package com.midlaj.bookingservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.midlaj.bookingservice.dto.*;
import com.midlaj.bookingservice.model.*;
import com.midlaj.bookingservice.repo.BookingRepository;
import com.midlaj.bookingservice.service.EmailService;
import com.midlaj.bookingservice.service.WalletService;
import com.midlaj.bookingservice.utils.AESService;
import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.StringReader;
import java.net.URISyntaxException;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.UUID;

@RestController
@RequestMapping(value = "/api/payment")
@Slf4j
public class PaymentController {

    @Autowired
    private RazorpayClient razorpayClient;

    @Autowired
    private AESService aesService;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private WalletService walletService;

    @Autowired
    private EmailService emailService;

    @Value("${service.resort}")
    private String RESORT_SERVICE;

    private final Double HANDLING_PERCENT = 0.05;


    @PostMapping(value = "/createOrder", consumes = MediaType.APPLICATION_XML_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> createOrder(@RequestBody String xml)  {
        log.info("Inside createOrder in PaymentController");

        PaymentRequest paymentRequest;
        try {
            JAXBContext jaxbContext = JAXBContext.newInstance(PaymentRequest.class);
            Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
            StringReader reader = new StringReader(xml);
            paymentRequest = (PaymentRequest) unmarshaller.unmarshal(reader);
        } catch (JAXBException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        try {
            // Create an order
            JSONObject orderRequest = new JSONObject();
            orderRequest.put("amount", paymentRequest.getAmount() * 100);
            orderRequest.put("currency", "INR");
            orderRequest.put("receipt",  "txn_" + UUID.randomUUID());
            orderRequest.put("payment_capture", 1);
            Order order = razorpayClient.orders.create(orderRequest);
            return ResponseEntity.ok(order.toJson().toString());
        } catch (RazorpayException e) {
            log.error("Could not create payment order");
            return ResponseEntity.badRequest().body("Could not create payment order");
        }

    }

    @PostMapping("/capturePayment")
    public ResponseEntity<?> capturePayment(@RequestBody EncryptedDTO encryptedData) throws URISyntaxException {
        log.info("Inside capturePayment in PaymentController");

        String decryptedData;
        try {
            decryptedData = aesService.decrypt(encryptedData.getEncryptedData());
        } catch (Exception e) {
            log.error(e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body("Not Valid");
        }

        ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());
        PaymentCaptureRequest paymentCaptureRequest;
        try {
            paymentCaptureRequest = mapper.readValue(decryptedData, PaymentCaptureRequest.class);
        } catch (Exception e) {
            log.error(e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Not Valid");
        }

        String url = RESORT_SERVICE + "/api/resort/userId/" + paymentCaptureRequest.getResortId();

        ResponseEntity<Long> response = null;
        try {
            response = restTemplate.getForEntity(url, Long.class);
        } catch (Exception e) {
            log.error(e.getMessage());
        }
        Long userId = response.getBody();

        PaymentDetails paymentDetails = new PaymentDetails();
        paymentDetails.setPaymentId(paymentCaptureRequest.getRazorpay_payment_id());
        paymentDetails.setPaymentMethod(PaymentMethod.RAZORPAY);
        paymentDetails.setPaymentStatus(PaymentStatus.SUCCESS);
        paymentDetails.setPaymentDate(new Date());
        paymentDetails.setSignature(paymentCaptureRequest.getRazorpay_signature());
        paymentDetails.setOrderId(paymentCaptureRequest.getRazorpay_order_id());
        paymentDetails.setPaymentTotal(paymentCaptureRequest.getTotal());

        for (Room room : paymentCaptureRequest.getRooms()) {

            Booking newBooking = Booking.builder()
                    .roomId(room.getRoomId())
                    .userId(paymentCaptureRequest.getUserId())
                    .resortId(paymentCaptureRequest.getResortId())
                    .bookedDate(new Date())
                    .checkInDate(paymentCaptureRequest.getCheckIn())
                    .checkOutDate(paymentCaptureRequest.getCheckOut())
                    .build();

            newBooking.setHandlingCharge(room.getPrice() * HANDLING_PERCENT);
            newBooking.setPrice(room.getPrice() - newBooking.getHandlingCharge());
            newBooking.setTotalPrice(room.getPrice());
            newBooking.setPaymentDetails(paymentDetails);

            Booking booking = bookingRepository.save(newBooking);

            //Adding money to Owner Wallet
            walletService.addMoney(userId, booking.getPrice() , "Booked room " + booking.getRoomId() + " from " + booking.getCheckInDate() + " - " + booking.getCheckOutDate() );
            // Adding money to Admin Wallet
            walletService.addMoney(Long.valueOf(0), booking.getHandlingCharge(), "Booked room " + booking.getRoomId() + " from " + booking.getCheckInDate() + " - " + booking.getCheckOutDate() );

            sendEmail(booking);
        }

        return ResponseEntity.status(HttpStatus.CREATED).body("Success");

    }



    public void sendEmail(Booking booking) throws URISyntaxException {

        String body = "<html>\n" +
                "\n" +
                "<body style=\"background-color:#e2e1e0;font-family: Open Sans, sans-serif;font-size:100%;font-weight:400;line-height:1.4;color:#000;\">\n" +
                "  <table style=\"max-width:670px;margin:50px auto 10px;background-color:#fff;padding:50px;-webkit-border-radius:3px;-moz-border-radius:3px;border-radius:3px;-webkit-box-shadow:0 1px 3px rgba(0,0,0,.12),0 1px 2px rgba(0,0,0,.24);-moz-box-shadow:0 1px 3px rgba(0,0,0,.12),0 1px 2px rgba(0,0,0,.24);box-shadow:0 1px 3px rgba(0,0,0,.12),0 1px 2px rgba(0,0,0,.24); border-top: solid 10px green;\">\n" +
                "    <thead>\n" +
                "      <tr>\n" +
                "        <th style=\"text-align:left;\"><img style=\"max-width: 150px;\" src=\"https://i.ibb.co/25SV90T/oie-f-CAaxhcl-GNkh.png\" alt=\"locus haunt\"></th>\n" +
                "        <th style=\"text-align:right;font-weight:400;\">[[bookedDate]]</th>\n" +
                "      </tr>\n" +
                "    </thead>\n" +
                "    <tbody>\n" +
                "      <tr>\n" +
                "        <td style=\"height:35px;\"></td>\n" +
                "      </tr>\n" +
                "      <tr>\n" +
                "        <td colspan=\"2\" style=\"border: solid 1px #ddd; padding:10px 20px;\">\n" +
                "          <p style=\"font-size:14px;margin:0 0 6px 0;\"><span style=\"font-weight:bold;display:inline-block;min-width:150px\">Order status</span><b style=\"color:green;font-weight:normal;margin:0\">Success</b></p>\n" +
                "          <p style=\"font-size:14px;margin:0 0 6px 0;\"><span style=\"font-weight:bold;display:inline-block;min-width:146px\">Transaction ID</span> [[transactionId]]</p>\n" +
                "          <p style=\"font-size:14px;margin:0 0 0 0;\"><span style=\"font-weight:bold;display:inline-block;min-width:146px\">Order amount</span> Rs. [[orderAmount]]</p>\n" +
                "        </td>\n" +
                "      </tr>\n" +
                "      <tr>\n" +
                "        <td style=\"height:35px;\"></td>\n" +
                "      </tr>\n" +
                "      <tr>\n" +
                "        <td colspan=\"2\" style=\"font-size:20px;padding:30px 15px 0 15px;\">Items</td>\n" +
                "      </tr>\n" +
                "      <tr>\n" +
                "        <td colspan=\"2\" style=\"padding:15px;\">\n" +
                "          <p style=\"font-size:14px;margin:0;padding:10px;border:solid 1px #ddd;font-weight:bold;\">\n" +
                "            <span style=\"display:block;font-size:13px;font-weight:normal;\">[[roomCode]] x [[nights]] nights </span> Rs. [[roomPrice]] <b style=\"font-size:12px;font-weight:300;\"> /night</b>\n" +
                "          </p>\n" +
                "        </td>\n" +
                "      </tr>\n" +
                "    </tbody>\n" +
                "    <tfooter>\n" +
                "      <tr>\n" +
                "        <td colspan=\"2\" style=\"font-size:14px;padding:50px 15px 0 15px;\">\n" +
                "          <strong style=\"display:block;margin:0 0 10px 0;\">Regards</strong> Locus Haunt<br>, Pin/Zip - 670645, Kerala, India<br><br>\n" +
                "          <b>Phone:</b> 807825xxxx<br>\n" +
                "          <b>Email:</b> locushaunt@gmail.com\n" +
                "        </td>\n" +
                "      </tr>\n" +
                "    </tfooter>\n" +
                "  </table>\n" +
                "</body>\n" +
                "\n" +
                "</html>";
        body = body.replace("[[transactionId]]", booking.getPaymentDetails().getOrderId());
        body = body.replace("[[orderAmount]]", booking.getPaymentDetails().getPaymentTotal().toString());
        body = body.replace("[[roomCode]]", booking.getRoomId().toString());
        body = body.replace("[[bookedDate]]", booking.getBookedDate().toLocaleString());
        long daysBetween = ChronoUnit.DAYS.between(booking.getCheckInDate(), booking.getCheckOutDate());
        body = body.replace("[[nights]]", String.valueOf(daysBetween));



        EmailDTO emailDTO = EmailDTO.builder()
                .to("mumidlaj@gmail.com")
                .body(body)
                .subject("Order Confirmation")
                .build();
        emailService.sendEmail(emailDTO);
    }



}