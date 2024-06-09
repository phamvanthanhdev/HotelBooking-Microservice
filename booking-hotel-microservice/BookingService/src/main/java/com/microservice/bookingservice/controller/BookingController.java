package com.microservice.bookingservice.controller;

import com.microservice.bookingservice.BookingCancelDateExeption;
import com.microservice.bookingservice.BookingCancelStatusExeption;
import com.microservice.bookingservice.BookingExeption;
import com.microservice.bookingservice.dto.BookingRequest;
import com.microservice.bookingservice.dto.BookingResponse;
import com.microservice.bookingservice.dto.HistoryBookingResponse;
import com.microservice.bookingservice.dto.MessageBooking;
import com.microservice.bookingservice.model.BookedRoom;
import com.microservice.bookingservice.service.BookingService;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.github.resilience4j.retry.annotation.Retry;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import jakarta.ws.rs.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    int count = 0;
    private final BookingService bookingService;

    private static final Logger LOGGER
            = LoggerFactory.getLogger(BookingController.class);

    @PostMapping("/book")
    @CircuitBreaker(name="inventory", fallbackMethod = "fallbackMethod")
    /*@TimeLimiter(name = "inventory")*/
    /*@Retry(name = "inventory", fallbackMethod = "fallbackMethodRetry")*/
    /*@RateLimiter(name = "inventory", fallbackMethod = "fallbackMethodRateLimiter")*/
    //CompletableFuture<String> : cũ
    public ResponseEntity<MessageBooking> bookingRoom(@RequestBody BookingRequest bookingRequest){
        LOGGER.info("Add booked room");
        /*System.out.println("Booking " + bookingRequest.toString());
        System.out.println("Retry count " + ++count);*/
        if(bookingRequest.getCheckInDate() == null || bookingRequest.getCheckOutDate() == null
                || bookingRequest.getGuestFullName() == null || bookingRequest.getGuestEmail() == null
                || bookingRequest.getNumOfAdults() <= 0 || bookingRequest.getNumOfChildren() < 0
                || bookingRequest.getTotalPrice() == null ||  bookingRequest.getRoomId() == null
                || bookingRequest.getHotelId() == null){
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

    public ResponseEntity<MessageBooking> fallbackMethodRetry(RuntimeException runtimeException){
        //return new ResponseEntity<>(HttpStatus.SERVICE_UNAVAILABLE);
        return ResponseEntity.ok(new MessageBooking(503, "Đã cố gắng kết nối lại lần thứ " + count + ", Sever bị ngắt kết nối hoặc quá tải, vui lòng thử lại sau!"));
    }

    public ResponseEntity<MessageBooking> fallbackMethodRateLimiter(RuntimeException runtimeException){
        //return new ResponseEntity<>(HttpStatus.SERVICE_UNAVAILABLE);
        return ResponseEntity.ok(new MessageBooking(503, "Vượt quá số lượng yêu cầu được cho phép, vui lòng thử lại sau!"));
    }

    //Lấy danh sách đặt phòng theo email
    @GetMapping("/get-by-email")
    public ResponseEntity<List<BookingResponse>> getBookedRoomsByEmail(@RequestParam("guestEmail") String guestEmail){
        LOGGER.info("Get list booked room by email");
        List<BookedRoom> bookedRooms = bookingService.getBookedRoomsByGuestEmail(guestEmail);
        List<BookingResponse> bookingResponses = bookedRooms.stream().map(this::convertBookedToBookingResponse).toList();
        return ResponseEntity.ok(bookingResponses);
    }

    @GetMapping("/history-booked-email")
    public ResponseEntity<List<HistoryBookingResponse>> getHistoryBookedRoomsByEmail(@RequestParam("guestEmail") String guestEmail){
        LOGGER.info("Get list history booked room by email");
        List<HistoryBookingResponse> historyBookingResponses = bookingService.getHistoryBookedByGuestEmail(guestEmail);

        return new ResponseEntity<>(historyBookingResponses, HttpStatus.OK);
    }

    //Lấy thông tin đặt phòng theo id
    @GetMapping("/get/{bookedId}")
    public ResponseEntity<BookedRoom> getBookedRoomById(@PathVariable("bookedId") Long bookedId){
        LOGGER.info("Get booked room by id");
        try {
            return ResponseEntity.ok(bookingService.getBookedById(bookedId));
        }catch (BookingExeption e){
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    //hủy đặt phòng
    @GetMapping("/cancel-booking/{bookedId}")
    public ResponseEntity<MessageBooking> cancelBookingRoom(@PathVariable("bookedId") Long bookedId){
        LOGGER.info("Cancel booking");
        try {
            bookingService.cancelBooking(bookedId);
            return ResponseEntity.ok(new MessageBooking(200, "Hủy đơn đặt phòng thành công!"));
        } catch (BookingExeption e){
            return ResponseEntity.ok(new MessageBooking(204, "Không tìm tháy đơn đặt phòng!"));
        } catch (BookingCancelStatusExeption ex){
            return ResponseEntity.ok(new MessageBooking(205, "Chỉ được hủy đơn đặt phòng chưa thanh toán!"));
        } catch (BookingCancelDateExeption exx){
            return ResponseEntity.ok(new MessageBooking(206, "Chỉ được hủy đơn đặt phòng sớm hơn 7 ngày trước ngày CheckIn!"));
        }
    }

    @PutMapping("/success-booking/{bookedId}")
    public ResponseEntity<BookingResponse> successBookingRoom(@PathVariable("bookedId") Long bookedId){
        LOGGER.info("Success booked room");
        BookedRoom bookedRoom = bookingService.successBooking(bookedId);
        BookingResponse response = convertBookedToBookingResponse(bookedRoom);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PutMapping("/success-booking-code")
    public ResponseEntity<BookingResponse> successBookingByCode(@RequestParam String code){
        LOGGER.info("Success booked room by Code");
        BookedRoom bookedRoom = bookingService.successBookingByConfirmationCode(code);
        BookingResponse response = convertBookedToBookingResponse(bookedRoom);
        return new ResponseEntity<>(response, HttpStatus.OK);
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
                request.getRoomId(),
                request.getHotelId());
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
                bookedRoom.getRoomId(),
                bookedRoom.getHotelId(),
                bookedRoom.getBookingStatus());
    }
}
