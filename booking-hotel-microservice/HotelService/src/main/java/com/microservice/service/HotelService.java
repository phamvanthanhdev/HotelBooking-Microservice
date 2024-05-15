package com.microservice.service;

import com.microservice.model.Hotel;
import com.microservice.repository.HotelRepository;
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
public class HotelService {
    private final HotelRepository hotelRepository;

    public byte[] getHotelPhotoByHotelId(Long id) throws SQLException {
        Optional<Hotel> theHotel = hotelRepository.findById(id);
        if(theHotel.isEmpty()){
            throw new RuntimeException("Sorry, Hotel not found!");
        }
        Blob photoBlob = theHotel.get().getPhoto();
        if(photoBlob != null){
            return photoBlob.getBytes(1, (int)photoBlob.length());
        }
        return null;
    }


    /*public void deleteRoom(Long roomId) {
        Optional<Room> theRoom = roomRepository.findById(roomId);
        if(theRoom.isPresent()){
            roomRepository.deleteById(roomId);
        }
    }


    public Room updateRoom(Long roomId, String roomType,
                           BigDecimal roomPrice, byte[] photoBytes) throws SQLException {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(()-> new RuntimeException("Hotel not found!"));
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
    }*/

    public Hotel createHotel(MultipartFile file, String name, String address, String city, String description, BigDecimal price) throws IOException, SQLException {
        Hotel hotel = new Hotel();
        hotel.setName(name);
        hotel.setAddress(address);
        hotel.setCity(city);
        hotel.setDescription(description);
        hotel.setPrice(price);
        hotel.setRating(5.0);
        if(!file.isEmpty()){
            byte[] photoBytes = file.getBytes();
            Blob photoBlod = new SerialBlob(photoBytes);
            hotel.setPhoto(photoBlod);
        }
        return hotelRepository.save(hotel);
    }

    public List<Hotel> getAllHotels() {
        return hotelRepository.findAll();
    }

    public Hotel getHotelByHotelId(Long hotelId) {
        Optional<Hotel> theHotel = hotelRepository.findById(hotelId);
        if(theHotel.isEmpty()){
            throw new RuntimeException("Sorry, Hotel not found!");
        }
        return hotelRepository.findById(hotelId).get();
    }
}
