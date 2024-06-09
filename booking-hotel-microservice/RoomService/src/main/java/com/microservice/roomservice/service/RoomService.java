package com.microservice.roomservice.service;

import com.microservice.roomservice.dto.InventoryQuantityResponse;
import com.microservice.roomservice.dto.RoomQuantityResponse;
import com.microservice.roomservice.dto.RoomResponse;
import com.microservice.roomservice.model.Room;
import com.microservice.roomservice.repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.client.WebClient;

import javax.sql.rowset.serial.SerialBlob;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Blob;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RoomService {
    private final RoomRepository roomRepository;
    private final WebClient.Builder webClientBuilder;

    public Room createRoom(MultipartFile file, String roomType,
                            BigDecimal roomPrice, Long hotelId) throws IOException, SQLException {
        Room room = new Room();
        room.setRoomType(roomType);
        room.setRoomPrice(roomPrice);
        room.setHotelId(hotelId);
        if(!file.isEmpty()){
            byte[] photoBytes = file.getBytes();
            Blob photoBlod = new SerialBlob(photoBytes);
            room.setPhoto(photoBlod);
        }
        return roomRepository.save(room);
    }

    public List<Room> getAllRooms() {
        return roomRepository.findAll();
    }

    public byte[] getRoomPhotoByRoomId(Long id) throws SQLException {
        Optional<Room> theRoom = roomRepository.findById(id);
        if(theRoom.isEmpty()){
            throw new RuntimeException("Sorry, Room not found!");
        }
        Blob photoBlob = theRoom.get().getPhoto();
        if(photoBlob != null){
            return photoBlob.getBytes(1, (int)photoBlob.length());
        }
        return null;
    }

    public List<String> getAllRoomTypes() {
        return roomRepository.findDistinctRoomTypes();
    }

    public void deleteRoom(Long roomId) {
        Optional<Room> theRoom = roomRepository.findById(roomId);
        if(theRoom.isPresent()){
            roomRepository.deleteById(roomId);
        }
    }


    public Room updateRoom(Long roomId, String roomType,
                           BigDecimal roomPrice, byte[] photoBytes) throws SQLException {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(()-> new RuntimeException("Room not found!"));
        if(roomType != null) room.setRoomType(roomType);
        if(roomPrice != null) room.setRoomPrice(roomPrice);
        if(photoBytes != null && photoBytes.length > 0) {
            try {
                room.setPhoto(new SerialBlob(photoBytes));
            }catch (SQLException ex){
                throw new SQLException("Error update room");
            }
        }
        return roomRepository.save(room);
    }

    public List<Room> getRoomsByHotelId(Long hotelId) {
        return roomRepository.findAllByHotelId(hotelId);
    }

    public List<RoomQuantityResponse> getRoomsQuantityByHotelId(Long hotelId) throws SQLException {
        List<Room> rooms = roomRepository.findAllByHotelId(hotelId);
        List<Long> roomIds = new ArrayList<>();
        for (Room r:rooms) {
            roomIds.add(r.getId());
        }

        InventoryQuantityResponse[] inventoryRes = webClientBuilder.build() .get()
                .uri("http://inventory-service/api/inventory/quantity",
                        uriBuilder -> uriBuilder.queryParam("roomIds", roomIds).build())
                .retrieve()
                .bodyToMono(InventoryQuantityResponse[].class)//Kiểu dữ liệu trả về
                .block();

        List<RoomQuantityResponse> roomQuantityResponses = new ArrayList<>();
        for (int i = 0; i < rooms.size(); i++) {
            RoomQuantityResponse roomQuantityResponse =
                    convertRoomToRoomQuantityResponse(rooms.get(i), inventoryRes[i].getQuantity());
            roomQuantityResponses.add(roomQuantityResponse);
        }

        return roomQuantityResponses;
    }


    public Room getRoomById(Long roomId) {
        return roomRepository.findById(roomId).get();
    }

    // Phương thức để lấy các loại phòng theo hotelId
    public List<String> getRoomTypesByHotelId(Long hotelId) {
        return roomRepository.findDistinctRoomTypesByHotelId(hotelId);
    }

    // phương thức đẻ lấy danh sách phòng thep loại phòng và id hotel
    public List<RoomQuantityResponse> getRoomsByHotelIdAndTypeRoom(Long hotelId,String roomType) throws SQLException {
        List<Room> rooms = roomRepository.findByRoomTypeAndHotelId(hotelId,roomType);

        List<Long> roomIds = new ArrayList<>();
        for (Room r:rooms) {
            roomIds.add(r.getId());
        }

        InventoryQuantityResponse[] inventoryRes = webClientBuilder.build() .get()
                .uri("http://inventory-service/api/inventory/quantity",
                        uriBuilder -> uriBuilder.queryParam("roomIds", roomIds).build())
                .retrieve()
                .bodyToMono(InventoryQuantityResponse[].class)//Kiểu dữ liệu trả về
                .block();

        List<RoomQuantityResponse> roomQuantityResponses = new ArrayList<>();
        for (int i = 0; i < rooms.size(); i++) {
            RoomQuantityResponse roomQuantityResponse =
                    convertRoomToRoomQuantityResponse(rooms.get(i), inventoryRes[i].getQuantity());
            roomQuantityResponses.add(roomQuantityResponse);
        }

        return roomQuantityResponses;
    }

    private RoomQuantityResponse convertRoomToRoomQuantityResponse(Room room, int quantity) throws SQLException {
        byte[] photoBytes = null;
        Blob photoBlob = room.getPhoto();
        if(photoBlob!=null){
            try {
                photoBytes = photoBlob.getBytes(1, (int)photoBlob.length());
            }catch (SQLException e){
                throw new SQLException("Error retrieving photo");
            }
        }
        return new RoomQuantityResponse(room.getId(),
                room.getRoomType(),
                room.getRoomPrice(),
                room.isBooked(), photoBytes,
                room.getHotelId(),
                quantity);
    }
}
