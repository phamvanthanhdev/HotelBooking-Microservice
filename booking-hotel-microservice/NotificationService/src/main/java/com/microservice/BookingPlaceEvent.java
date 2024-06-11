package com.microservice;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookingPlaceEvent {
    private Long bookingId;
    private String guestFullName;
    private String guestEmail;
    private LocalDate checkInDate;
    private LocalDate checkOutDate;
    private String bookingConfirmationCode;
}

