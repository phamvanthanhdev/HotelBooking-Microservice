package com.microservice.bookingservice;

public class BookingCancelStatusExeption extends RuntimeException {
    private String message;

    public BookingCancelStatusExeption(String message) {
        this.message = message;
    }
}
