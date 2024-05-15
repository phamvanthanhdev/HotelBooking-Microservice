package com.microservice.bookingservice.controller;

import com.microservice.bookingservice.model.BookedRoom;
import com.microservice.bookingservice.service.BookingService;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("api/booking")
/*@CrossOrigin(origins = "*")*/
@RequiredArgsConstructor
public class BookingController {
    private final BookingService bookingService;
    @PostMapping("/room/{roomId}/booking")
    @CircuitBreaker(name="inventory", fallbackMethod = "fallbackMethod")
    @TimeLimiter(name = "inventory")
    /*@Retry(name = "inventory")*/
    public CompletableFuture<String> bookingRoom(@PathVariable Long roomId,
                                              @RequestBody BookedRoom bookingRequest){
        /*try{
            String messageBooking = bookingService.saveBooking(roomId, bookingRequest);
            return ResponseEntity.ok(messageBooking);
        }catch (RuntimeException e){
            return ResponseEntity.badRequest().body(e.getMessage());
        }*/
        String messageBooking = bookingService.saveBooking(roomId, bookingRequest);
        return CompletableFuture.supplyAsync(()->messageBooking);
    }

    //Logic dự phòng khi inventory service gặp sự cố
    public CompletableFuture<String> fallbackMethod(RuntimeException runtimeException){
        return CompletableFuture.supplyAsync(()->"Oops! Something went wrong, please booking after some time!");
    }
}
