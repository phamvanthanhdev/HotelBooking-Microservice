package com.microservice.bookingservice.service;

import brave.Span;
import brave.Tracer;
import com.microservice.bookingservice.BookingCancelDateExeption;
import com.microservice.bookingservice.BookingCancelStatusExeption;
import com.microservice.bookingservice.BookingExeption;
import com.microservice.bookingservice.dto.*;
//import com.microservice.bookingservice.event.BookingPlaceEvent;//
import com.microservice.bookingservice.model.BookedRoom;
import com.microservice.bookingservice.repository.BookingRepository;
import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationRegistry;
import jakarta.ws.rs.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
//import org.springframework.kafka.core.KafkaTemplate;//
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class BookingService {
    private final BookingRepository bookingRepository;
    private final WebClient.Builder webClientBuilder;
    private final Tracer tracer;
    private final ObservationRegistry observationRegistry;
//    private final KafkaTemplate<String, BookingPlaceEvent> kafkaTemplate;//

    public String saveBooking(BookedRoom bookingRequest) {
        if(bookingRequest.getCheckOutDate().isBefore(bookingRequest.getCheckInDate())){
            throw  new BookingExeption("Check-in date must come before check-out date");
        }
        Long roomId = bookingRequest.getRoomId();

        //Call Inventory Server, and booking room success if room is available
        Observation inventoryServiceObservation = Observation.createNotStarted("inventory-service-lookup",
                this.observationRegistry);
        inventoryServiceObservation.lowCardinalityKeyValue("call", "inventory-service");
        return inventoryServiceObservation.observe(() -> {
                    InventoryResponse inventoryResponse = webClientBuilder.build().get()
                            .uri("http://inventory-service/api/inventory",
                                    uriBuilder -> uriBuilder.queryParam("roomId", roomId).build())
                            .retrieve()
                            .bodyToMono(InventoryResponse.class)
                            .block();

                    Boolean roomIsAvailable = inventoryResponse.isAvailable();

                    if(roomIsAvailable) {
                        bookingRequest.setRoomId(roomId);
                        String bookingCode = RandomStringUtils.randomNumeric(10);
                        bookingRequest.setBookingConfirmationCode(bookingCode);

                        bookingRepository.save(bookingRequest);

                    /*kafkaTemplate.send("notificationTopic",
                            new BookingPlaceEvent(bookingRequest.getBookingId(), bookingRequest.getGuestFullName(),
                                    bookingRequest.getGuestEmail(), bookingRequest.getCheckInDate(),
                                    bookingRequest.getCheckOutDate(), bookingRequest.getBookingConfirmationCode()));//*/

                        return "Room booked successfully, Your booking confirmation code is : " + bookingRequest.getBookingConfirmationCode();
                    }else {
                        throw new IllegalArgumentException("Room is not available, please try again later");
                        //return "Room is not available, please try again later";
                    }
                });


        /*InventoryResponse inventoryRespone = webClientBuilder.build() .get()
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

//            kafkaTemplate.send("notificationTopic", new BookingPlaceEvent(bookingRequest.getBookingId()));

            return "Room booked successfully, Your booking confirmation code is : " + bookingRequest.getBookingConfirmationCode();
        }else {
            throw new IllegalArgumentException("Room is not available, please try again later");
            //return "Room is not available, please try again later";
        }*/
    }

    public List<BookedRoom> getBookedRoomsByGuestEmail(String guestEmail) {
        return bookingRepository.getBookedRoomsByGuestEmailDESC(guestEmail);
    }

    public List<BookedRoom> getAllBookedRooms() {
        return bookingRepository.getAllBookedRoomsDESC();
    }

    public List<HistoryBookingResponse> getHistoryBookedByGuestEmail(String guestEmail) {
        List<BookedRoom> bookedRooms = bookingRepository.getBookedRoomsByGuestEmailDESC(guestEmail);

        List<Long> idsHotel = new ArrayList<>();
        for (BookedRoom booked:bookedRooms) {
            idsHotel.add(booked.getHotelId());
        }
        //Call HotelService
        String[] hotelsImage = webClientBuilder.build() .get()
                .uri("http://hotel-service/api/hotel/hotels-by-ids",
                        uriBuilder -> uriBuilder.queryParam("idsHotel", idsHotel).build())
                .retrieve()
                .bodyToMono(String[].class)//Kiểu dữ liệu trả về
                .block();

        List<HistoryBookingResponse> historyBookingResponses = new ArrayList<>();
        for (int i = 0; i < bookedRooms.size(); i++) {
            HistoryBookingResponse historyBookingResponse =
                    convertBookedToHistoryBookingResponse(bookedRooms.get(i), hotelsImage[i]);
            historyBookingResponses.add(historyBookingResponse);
//            break;
        }

        return historyBookingResponses;
    }

    private HistoryBookingResponse convertBookedToHistoryBookingResponse(BookedRoom bookedRoom, String hotelImage) {
        return new HistoryBookingResponse(
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
                hotelImage,
                bookedRoom.getBookingStatus());
    }

    public BookedRoom getBookedById(Long bookedId) {
        if(bookingRepository.findById(bookedId).isPresent()){
            return bookingRepository.findById(bookedId).get();
        }
        throw new BookingExeption("Booked room not found!");
    }

    public void cancelBooking(Long bookedId) {
        if(bookingRepository.findById(bookedId).isPresent()){
            BookedRoom bookedRoom = bookingRepository.findById(bookedId).get();

            if(!bookedRoom.getBookingStatus().equals("Chưa thanh toán")){
                throw new BookingCancelStatusExeption("Chỉ được hủy đơn đặt phòng chưa thanh toán!");
            }
            long daysBetween = ChronoUnit.DAYS.between(bookedRoom.getCheckInDate(), LocalDate.now());
            System.out.println("day between: " + daysBetween);
            if(daysBetween >= 7){
                throw new BookingCancelDateExeption("Chỉ được hủy đơn đặt phòng sớm hơn 7 ngày trước ngày CheckIn!");
            }

            bookedRoom.setBookingStatus("Đã hủy");
            bookingRepository.save(bookedRoom);
            return;
        }
        throw new BookingExeption("Booked room not found!");
    }

    public BookedRoom cancelBookingRoom(Long bookedId) {
        if(bookingRepository.findById(bookedId).isPresent()){
            BookedRoom bookedRoom = bookingRepository.findById(bookedId).get();

            if(!bookedRoom.getBookingStatus().equals("Chưa thanh toán")){
                throw new BookingCancelStatusExeption("Chỉ được hủy đơn đặt phòng chưa thanh toán!");
            }
            long daysBetween = ChronoUnit.DAYS.between(bookedRoom.getCheckInDate(), LocalDate.now());
            System.out.println("day between: " + daysBetween);
            if(daysBetween >= 7){
                throw new BookingCancelDateExeption("Chỉ được hủy đơn đặt phòng sớm hơn 7 ngày trước ngày CheckIn!");
            }

            bookedRoom.setBookingStatus("Đã hủy");
            return bookingRepository.save(bookedRoom);
        }
        throw new BookingExeption("Booked room not found!");
    }


    public BookedRoom successBooking(Long bookedId) {
        if(bookingRepository.findById(bookedId).isPresent()){
            BookedRoom bookedRoom = bookingRepository.findById(bookedId).get();

            bookedRoom.setBookingStatus("Đã thanh toán");
            return bookingRepository.save(bookedRoom);
        }
        throw new BookingExeption("Booked room not found!");
    }

    public BookedRoom successBookingByConfirmationCode(String code) {
        BookedRoom bookedRoom = bookingRepository.findByBookingConfirmationCode(code);

        bookedRoom.setBookingStatus("Đã thanh toán");
        return bookingRepository.save(bookedRoom);
    }

    public BookedRoom updateStatus(Long id, String status) throws Exception {
        if(bookingRepository.findById(id).isPresent()){
            BookedRoom bookedRoom = bookingRepository.findById(id).get();
            if(status.trim().equals("Đã thanh toán")
                    || status.trim().equals("Chưa thanh toán")
                    || status.trim().equals("Đã hủy")) {
                bookedRoom.setBookingStatus(status);

                return bookingRepository.save(bookedRoom);
            }
            throw new Exception("Status booked is invalid!");
        }
        throw new BookingExeption("Booked room not found!");
    }

    public List<StatisticResponse> getStatistic(String dateStart, String dateEnd) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        LocalDate localDateStart = LocalDate.parse(dateStart, formatter);
        LocalDate localDateEnd = LocalDate.parse(dateEnd, formatter);

        List<StatisticResponse> statisticResponses = new ArrayList<>();
        for (int i = localDateStart.getMonthValue(); i < localDateEnd.getMonthValue(); i++) {
            StatisticResponse response = new StatisticResponse();
            response.setMonth(i);
            List<BookedRoom> bookedRooms = bookingRepository.findBookedRoomsByMonthAndYear(i, localDateStart, localDateEnd);
            response.setQuantity(bookedRooms.size());
            long total = 0L;
            for (BookedRoom bookedRoom:bookedRooms) {
                total += bookedRoom.getTotalPrice().longValue();
            }
            response.setTotal(total);
            statisticResponses.add(response);
        }

        return statisticResponses;
    }
}
