package com.microservice.bookingservice;

public class BookingExeption extends RuntimeException {
    private String message;

    public BookingExeption(String message) {
        this.message = message;
    }
}
