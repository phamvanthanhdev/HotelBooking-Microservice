package com.microservice.roomservice.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.codec.binary.Base64;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
public class RoomQuantityResponse {
    private Long id;
    private String roomType;
    private BigDecimal roomPrice;
    private boolean isBooked = false;
    private String photo;
    private Long hotelId;
    private int quantity;

    public RoomQuantityResponse(Long id, String roomType, BigDecimal roomPrice) {
        this.id = id;
        this.roomType = roomType;
        this.roomPrice = roomPrice;
    }

    public RoomQuantityResponse(Long id, String roomType, BigDecimal roomPrice,
                                boolean isBooked, byte[] photoBytes, Long hotelId,
                                int quantity) {
        this.id = id;
        this.roomType = roomType;
        this.roomPrice = roomPrice;
        this.isBooked = isBooked;
        this.photo = photoBytes != null ? Base64.encodeBase64String(photoBytes): null;
        this.hotelId = hotelId;
        this.quantity = quantity;
    }
}
