package com.microservice.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.codec.binary.Base64;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
public class HotelResponse {
    private Long id;
    private String name;
    private String address;
    private String city;
    private double rating;
    private BigDecimal price;
    private String photo;
    public HotelResponse(Long id, String name, String address,
                         String city, double rating,
                         BigDecimal price, byte[] photoBytes) {
        this.id = id;
        this.name = name;
        this.address = address;
        this.city = city;
        this.rating = rating;
        this.price = price;
        this.photo = photoBytes != null ? Base64.encodeBase64String(photoBytes): null;
    }
}
