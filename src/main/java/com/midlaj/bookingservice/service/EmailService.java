package com.midlaj.bookingservice.service;

import com.midlaj.bookingservice.dto.EmailDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.net.URISyntaxException;

@Service
@Slf4j
public class EmailService {

    @Value("${service.email-service}")
    private String EMAIL_SERVICE;
    @Autowired
    private RestTemplate restTemplate;

    public ResponseEntity<?> sendEmail(EmailDTO emailDTO) throws URISyntaxException {

        URI uri = new URI(EMAIL_SERVICE);

        ResponseEntity<String> result;
        try {
            result = restTemplate.postForEntity(uri, emailDTO, String.class);
        } catch (HttpServerErrorException e) {
            log.error(e.getMessage());
            result = ResponseEntity.status(HttpStatus.CONFLICT).body("Email Service is not available at the moment");
        }
        return result;
    }
}
