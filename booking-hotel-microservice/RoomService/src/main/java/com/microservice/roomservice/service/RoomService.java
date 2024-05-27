package com.microservice.roomservice.service;

import com.microservice.roomservice.model.Room;
import com.microservice.roomservice.repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.sql.rowset.serial.SerialBlob;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Blob;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RoomService {
    private final RoomRepository roomRepository;

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

    public Room getRoomById(Long roomId) {
        return roomRepository.findById(roomId).get();
    }

    // Phương thức để lấy các loại phòng theo hotelId
    public List<String> getRoomTypesByHotelId(Long hotelId) {
        return roomRepository.findDistinctRoomTypesByHotelId(hotelId);
    }

    // phương thức đẻ lấy danh sách phòng thep loại phòng và id hotel
    public List<Room> getRoomsByHotelIdAndTypeRoom(Long hotelId,String roomType) {
        return roomRepository.findByRoomTypeAndHotelId(hotelId,roomType);
    }
}
