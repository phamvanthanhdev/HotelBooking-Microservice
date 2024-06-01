package com.microservice.bookingservice;

public class BookingCancelDateExeption extends RuntimeException {
    private String message;

    public BookingCancelDateExeption(String message) {
        this.message = message;
    }
}
