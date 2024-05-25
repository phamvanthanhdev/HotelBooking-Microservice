package com.microservice.roomservice.controller;

import com.microservice.roomservice.dto.RoomResponse;
import com.microservice.roomservice.model.Room;
import com.microservice.roomservice.service.RoomService;
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

@RestController
@RequestMapping("/api/room")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class RoomController {
    private final RoomService roomService;
    @PostMapping("/add/new-room")
    public ResponseEntity<RoomResponse> createRoom(@RequestParam("photo") MultipartFile photo,
                                                   @RequestParam("roomType") String roomType,
                                                   @RequestParam("roomPrice") BigDecimal roomPrice,
                                                   @RequestParam("hotelId") Long hotelId) throws IOException, SQLException {
        Room savedRoom = roomService.createRoom(photo, roomType, roomPrice, hotelId);

        RoomResponse response = new RoomResponse(savedRoom.getId(),
                savedRoom.getRoomType(), savedRoom.getRoomPrice());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/room/types")
    public ResponseEntity<List<String>> getAllRoomType(){
        List<String> roomTypes = roomService.getAllRoomTypes();
        return ResponseEntity.ok(roomTypes);
    }

    @GetMapping("/all-rooms")
    public ResponseEntity<List<RoomResponse>> getAllRooms() throws SQLException {
        List<Room> roomList = roomService.getAllRooms();
        List<RoomResponse> roomResponses = new ArrayList<>();
        for (Room room:roomList) {
            byte[] photoBytes = roomService.getRoomPhotoByRoomId(room.getId());
            if(photoBytes != null && photoBytes.length > 0){
                String base64Photo = Base64.encodeBase64String(photoBytes);
                RoomResponse roomResponse = getRoomRespone(room);
                roomResponse.setPhoto(base64Photo);
                roomResponses.add(roomResponse);
            }
        }
        return ResponseEntity.ok(roomResponses);
    }

    @GetMapping("/get-rooms-hotel/{hotelId}")
    public ResponseEntity<List<RoomResponse>> getRoomsByHotelId(@PathVariable("hotelId") Long hotelId) throws SQLException {
        List<Room> roomList = roomService.getRoomsByHotelId(hotelId);
        List<RoomResponse> roomResponses = new ArrayList<>();
        for (Room room:roomList) {
            roomResponses.add(getRoomRespone(room));
        }
        return ResponseEntity.ok(roomResponses);
    }

    @DeleteMapping("/delete/room/{roomId}")
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
    }

    @GetMapping("/get/{roomId}")
    public ResponseEntity<RoomResponse> getRoomById(@PathVariable Long roomId) throws SQLException {
        Room room = roomService.getRoomById(roomId);
        RoomResponse response = getRoomRespone(room);
        return ResponseEntity.ok(response);
    }



    //Convert Room to RoomResponse return Frontend
    private RoomResponse getRoomRespone(Room room) throws SQLException {
        byte[] photoBytes = null;
        Blob photoBlob = room.getPhoto();
        if(photoBlob!=null){
            try {
                photoBytes = photoBlob.getBytes(1, (int)photoBlob.length());
            }catch (SQLException e){
                throw new SQLException("Error retrieving photo");
            }
        }
        return new RoomResponse(room.getId(),
                room.getRoomType(),
                room.getRoomPrice(),
                room.isBooked(), photoBytes,
                room.getHotelId());
    }
}
