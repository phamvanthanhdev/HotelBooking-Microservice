package com.microservice.bookingservice.controller;

import com.microservice.bookingservice.BookingExeption;
import com.microservice.bookingservice.dto.BookingRequest;
import com.microservice.bookingservice.dto.BookingResponse;
import com.microservice.bookingservice.dto.MessageBooking;
import com.microservice.bookingservice.model.BookedRoom;
import com.microservice.bookingservice.service.BookingService;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.sql.Blob;
import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("api/booking")
/*@CrossOrigin(origins = "*")*/
@RequiredArgsConstructor
public class BookingController {
    private final BookingService bookingService;
    @PostMapping("/book")
    @CircuitBreaker(name="inventory", fallbackMethod = "fallbackMethod")
    /*@TimeLimiter(name = "inventory")*/
    /*@Retry(name = "inventory")*/
    //CompletableFuture<String> : cũ
    public ResponseEntity<MessageBooking> bookingRoom(@RequestBody BookingRequest bookingRequest){
        System.out.println("Booking " + bookingRequest.toString());
        if(bookingRequest.getCheckInDate() == null || bookingRequest.getCheckOutDate() == null
                || bookingRequest.getGuestFullName() == null || bookingRequest.getGuestEmail() == null
                || bookingRequest.getNumOfAdults() <= 0 || bookingRequest.getNumOfChildren() < 0
                || bookingRequest.getTotalPrice() == null ||  bookingRequest.getRoomId() == null){
            return ResponseEntity.ok().body(new MessageBooking(400, "Dữ liệu đặt phòng chưa chính xác, vui lòng thử lại!"));
            //return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        try {
            BookedRoom bookedRoom = convertRequestToBooked(bookingRequest);
            String messageBooking = bookingService.saveBooking(bookedRoom);
            return ResponseEntity.ok().body(new MessageBooking(200, messageBooking));
            //return ResponseEntity.ok().body(new MessageBooking("Du lieu dat phong chua chinh xac"));
        }catch (IllegalArgumentException e){
            //return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            return ResponseEntity.ok().body(new MessageBooking(204, "Phòng không còn trống, vui lòng thử lại sau!"));
        }catch (BookingExeption ex){
            return ResponseEntity.ok().body(new MessageBooking(400,"Ngày nhận phòng phải sớm hơn ngày trả phòng!"));
        }

    }
    public ResponseEntity<MessageBooking> fallbackMethod(RuntimeException runtimeException){
        //return new ResponseEntity<>(HttpStatus.SERVICE_UNAVAILABLE);
        return ResponseEntity.ok(new MessageBooking(503, "Sever bị ngắt kết nối hoặc quá tải, vui lòng thử lại sau!"));
    }

    //Lấy lịch sử đặt phòng theo email
    @GetMapping("/get-by-email")
    public ResponseEntity<List<BookingResponse>> getBookedRoomsByEmail(@RequestParam("guestEmail") String guestEmail){
        List<BookedRoom> bookedRooms = bookingService.getBookedRoomsByGuestEmail(guestEmail);
        List<BookingResponse> bookingResponses = bookedRooms.stream().map(this::convertBookedToBookingResponse).toList();
        return ResponseEntity.ok(bookingResponses);
    }

    // Convert Booking Request to Booked Room and save in database
    private BookedRoom convertRequestToBooked(BookingRequest request) {
        return new BookedRoom(request.getCheckInDate(),
                request.getCheckOutDate(),
                request.getGuestFullName(),
                request.getGuestEmail(),
                request.getNumOfAdults(),
                request.getNumOfChildren(),
                request.getNumOfAdults() + request.getNumOfChildren(),
                request.getNote(),
                request.getTotalPrice(),
                request.getRoomId());
    }

    private BookingResponse convertBookedToBookingResponse(BookedRoom bookedRoom) {
        return new BookingResponse(
                bookedRoom.getBookingId(),
                bookedRoom.getCheckInDate(),
                bookedRoom.getCheckOutDate(),
                bookedRoom.getGuestFullName(),
                bookedRoom.getGuestEmail(),
                bookedRoom.getNumOfAdults(),
                bookedRoom.getNumOfChildren(),
                bookedRoom.getTotalNumOfGuest(),
                bookedRoom.getNote(),
                bookedRoom.getTotalPrice(),
                bookedRoom.getBookingConfirmationCode(),
                bookedRoom.getRoomId());
    }
}
