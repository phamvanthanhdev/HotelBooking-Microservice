package com.microservice.controller;

import com.microservice.dto.HotelDetailResponse;
import com.microservice.dto.HotelResponse;
import com.microservice.model.Hotel;
import com.microservice.service.HotelService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.codec.binary.Base64;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.sql.rowset.serial.SerialBlob;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Blob;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/hotel")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class HotelController {
    private final HotelService hotelService;
    //PHAM VAN THANH
    @PostMapping
    public ResponseEntity<HotelResponse> createHotel(@RequestParam("photo") MultipartFile photo,
                                                     @RequestParam("name") String name,
                                                     @RequestParam("address") String address,
                                                     @RequestParam("city") String city,
                                                     @RequestParam("description") String description,
                                                     @RequestParam("price") BigDecimal price) throws IOException, SQLException {
        Hotel savedHotel = hotelService.createHotel(photo, name, address, city, description, price);
        HotelResponse response = convertHotelToResponse(savedHotel);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/all-hotels")
    public ResponseEntity<List<HotelResponse>> getAllHotels() throws SQLException {
        List<Hotel> hotelList = hotelService.getAllHotels();
        List<HotelResponse> roomResponses = new ArrayList<>();
        for (Hotel hotel:hotelList) {
            byte[] photoBytes = hotelService.getHotelPhotoByHotelId(hotel.getId());
            if(photoBytes != null && photoBytes.length > 0){
                String base64Photo = Base64.encodeBase64String(photoBytes);
                HotelResponse roomResponse = convertHotelToResponse(hotel);
                roomResponse.setPhoto(base64Photo);
                roomResponses.add(roomResponse);
            }
        }
        return ResponseEntity.ok(roomResponses);
    }

    @GetMapping("/get/{hotelId}")
    public ResponseEntity<?> getHotelByHotelId(@PathVariable Long hotelId)
                                                throws SQLException {
        try {
            Hotel hotel = hotelService.getHotelByHotelId(hotelId);
            HotelDetailResponse hotelDetail = convertHotelToDetail(hotel);
            return ResponseEntity.ok(hotelDetail);
        }catch (RuntimeException ex){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
        }
    }

    @GetMapping("/get/locations")
    public ResponseEntity<List<String>> getAllCities(){
        List<String> cities = hotelService.getAllCities();
        return ResponseEntity.ok(cities);
    }

//    @GetMapping("/city/{cityName}")
//    public ResponseEntity<List<HotelResponse>> getHotelsByCity(@PathVariable String cityName) {
//        List<Hotel> hotels = hotelService.getHotelsByCity(cityName);
//        List<HotelResponse> hotelResponses = hotels.stream()
//                .map(hotel -> convertHotelToResponse(hotel))
//                .collect(Collectors.toList());
//        return ResponseEntity.ok(hotelResponses);
//    }
    @GetMapping("/city/{cityName}")
    public ResponseEntity<List<HotelDetailResponse>> getHotelsByCity(@PathVariable String cityName) {
        List<Hotel> hotels = hotelService.getHotelsByCity(cityName);
        List<HotelDetailResponse> hotelDetailsResponses = hotels.stream()
                .map(hotel -> {
                    try {
                        return convertHotelToDetail(hotel);
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                })
                .collect(Collectors.toList());
        return ResponseEntity.ok(hotelDetailsResponses);
    }

    @GetMapping("/search")
    public ResponseEntity<List<HotelResponse>> searchHotels(@RequestParam String keyword) throws SQLException {
        List<Hotel> hotelList = hotelService.searchHotel(keyword);
        List<HotelResponse> roomResponses = new ArrayList<>();
        for (Hotel hotel:hotelList) {
            byte[] photoBytes = hotelService.getHotelPhotoByHotelId(hotel.getId());
            if(photoBytes != null && photoBytes.length > 0){
                String base64Photo = Base64.encodeBase64String(photoBytes);
                HotelResponse roomResponse = convertHotelToResponse(hotel);
                roomResponse.setPhoto(base64Photo);
                roomResponses.add(roomResponse);
            }
        }
        return new ResponseEntity<>(roomResponses, HttpStatus.OK);
    }

    /*@DeleteMapping("/delete/room/{roomId}")
    public ResponseEntity<Void> deleteRoom(@PathVariable Long roomId){
        roomService.deleteRoom(roomId);
        return  new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @PutMapping("/update/{roomId}")
    public ResponseEntity<RoomResponse> updateRoom(
            @PathVariable Long roomId,
            @RequestParam(required = false)  String roomType,
            @RequestParam(required = false) BigDecimal roomPrice,
            @RequestParam(required = false) MultipartFile photo) throws SQLException, IOException {
        byte[] photoBytes = photo != null && !photo.isEmpty()
                ? photo.getBytes()
                : roomService.getRoomPhotoByRoomId(roomId);
        Blob photoBlob = photoBytes != null && photoBytes.length >0
                ? new SerialBlob(photoBytes)
                : null;
        Room room = roomService.updateRoom(roomId, roomType, roomPrice, photoBytes);
        room.setPhoto(photoBlob);
        RoomResponse roomResponse = getRoomRespone(room);
        return ResponseEntity.ok(roomResponse);
    }*/



    //Convert Hotel to HotelResponse return Frontend
    private HotelResponse convertHotelToResponse(Hotel hotel) {
        byte[] photoBytes = null;
        Blob photoBlob = hotel.getPhoto();
        try {
            if(photoBlob != null) {
                photoBytes = photoBlob.getBytes(1, (int) photoBlob.length());
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving photo: " + e.getMessage());
            // Trả về một giá trị mặc định hoặc thông báo lỗi trong trường hợp xảy ra lỗi
            // Ví dụ: Trả về một ảnh mặc định hoặc làm gì đó thích hợp với ứng dụng của bạn
        }
        return new HotelResponse(hotel.getId(),
                hotel.getName(),
                hotel.getAddress(),
                hotel.getCity(),
                hotel.getRating(),
                hotel.getPrice()
                , photoBytes);
    }

    //Convert Hotel to HotelResponse return Frontend
    private HotelDetailResponse convertHotelToDetail(Hotel hotel) throws SQLException {
        byte[] photoBytes = null;
        Blob photoBlob = hotel.getPhoto();
        try {
            if(photoBlob != null) {
                photoBytes = photoBlob.getBytes(1, (int) photoBlob.length());
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving photo: " + e.getMessage());
            // Trả về một giá trị mặc định hoặc thông báo lỗi trong trường hợp xảy ra lỗi
            // Ví dụ: Trả về một ảnh mặc định hoặc làm gì đó thích hợp với ứng dụng của bạn
        }
        return new HotelDetailResponse(hotel.getId(),
                hotel.getName(),
                hotel.getAddress(),
                hotel.getCity(),
                hotel.getRating(),
                hotel.getPrice(),
                hotel.getDescription(),
                photoBytes);
    }
}
