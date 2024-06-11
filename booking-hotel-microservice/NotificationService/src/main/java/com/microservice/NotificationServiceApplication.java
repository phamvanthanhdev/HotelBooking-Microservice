package com.microservice;

import jakarta.mail.MessagingException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.annotation.KafkaListener;

@SpringBootApplication
@Slf4j
public class NotificationServiceApplication {
    @Autowired
    private EmailSenderService senderService;
    public static void main(String[] args) {
        SpringApplication.run(NotificationServiceApplication.class, args);
    }

    @KafkaListener(topics = "notificationTopic")
    public void handleNotification(BookingPlaceEvent bookingPlaceEvent) throws MessagingException {
        // can send out an email notification here
        log.info("Receive Notification for Booking - {}", bookingPlaceEvent.getBookingId());
        log.info("Guest Email - {}", bookingPlaceEvent.getGuestEmail());
        log.info("Guest FullName - {}", bookingPlaceEvent.getGuestFullName());
        log.info("CheckIn Date - {}", bookingPlaceEvent.getCheckInDate());
        log.info("CheckOut Date - {}", bookingPlaceEvent.getCheckOutDate());
        log.info("Confirmation Code - {}", bookingPlaceEvent.getBookingConfirmationCode());
        triggerMail(bookingPlaceEvent);
    }
    public void triggerMail(BookingPlaceEvent bookingPlaceEvent) throws MessagingException {
        senderService.sendSimpleEmail("thanhdever@gmail.com",
                "Thông tin đặt phòng khách sạn",
                "Thông tin đặt phòng của bạn:"
                        + "\n\tHọ tên khách hàng: " + bookingPlaceEvent.getGuestFullName()
                        + "\n\tNgày nhận phòng: " + bookingPlaceEvent.getCheckInDate()
                        + "\n\tNgày trả phòng: " + bookingPlaceEvent.getCheckOutDate()
                        + "\n\tMã đặt phòng: " + bookingPlaceEvent.getBookingConfirmationCode()
        );

    }
}

