package com.microservice.bookingservice.service;

import com.microservice.bookingservice.BookingExeption;
import com.microservice.bookingservice.dto.InventoryResponse;
import com.microservice.bookingservice.model.BookedRoom;
import com.microservice.bookingservice.repository.BookingRepository;
import jakarta.ws.rs.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang.RandomStringUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class BookingService {
    private final BookingRepository bookingRepository;
    private final WebClient.Builder webClientBuilder;
    public String saveBooking(BookedRoom bookingRequest) {
        if(bookingRequest.getCheckOutDate().isBefore(bookingRequest.getCheckInDate())){
            throw  new BookingExeption("Check-in date must come before check-out date");
        }
        Long roomId = bookingRequest.getRoomId();
        //Call Inventory Server, and booking room success if room is available
        InventoryResponse inventoryRespone = webClientBuilder.build() .get()
                .uri("http://inventory-service/api/inventory",
                        uriBuilder -> uriBuilder.queryParam("roomId", roomId).build())
                .retrieve()
                .bodyToMono(InventoryResponse.class)//Kiểu dữ liệu trả về
                .block();

        Boolean roomIsAvailable = inventoryRespone.isAvailable();

        if(roomIsAvailable) {
            bookingRequest.setRoomId(roomId);
            String bookingCode = RandomStringUtils.randomNumeric(10);
            bookingRequest.setBookingConfirmationCode(bookingCode);

            bookingRepository.save(bookingRequest);
            return "Room booked successfully, Your booking confirmation code is : " + bookingRequest.getBookingConfirmationCode();
        }else {
            throw new IllegalArgumentException("Room is not available, please try again later");
            //return "Room is not available, please try again later";
        }

    }

    public List<BookedRoom> getBookedRoomsByGuestEmail(String guestEmail) {
        return bookingRepository.getBookedRoomsByGuestEmailDESC(guestEmail);
    }

    public BookedRoom getBookedById(Long bookedId) {
        if(bookingRepository.findById(bookedId).isPresent()){
            return bookingRepository.findById(bookedId).get();
        }
        throw new BookingExeption("Booked room not found!");
    }
}
