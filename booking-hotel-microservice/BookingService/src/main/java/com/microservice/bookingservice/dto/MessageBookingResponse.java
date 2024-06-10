package com.microservice.bookingservice.dto;

import com.microservice.bookingservice.model.BookedRoom;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MessageBookingResponse {
    private int code;
    private String message;
    private BookedRoom bookedRoom;
}