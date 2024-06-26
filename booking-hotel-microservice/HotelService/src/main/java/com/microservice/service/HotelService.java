package com.microservice.service;

import com.microservice.dto.User;
import com.microservice.dto.UserResponse;
import com.microservice.model.Hotel;
import com.microservice.repository.HotelRepository;
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
public class HotelService {
    private final HotelRepository hotelRepository;
    private final WebClient.Builder webClientBuilder;

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

    public Hotel createHotelAuth(MultipartFile file, String name, String address,
                                 String city, String description,
                                 BigDecimal price, String jwt) throws IOException, SQLException {
        //Call UserService
        UserResponse user = webClientBuilder.defaultHeader("Authorization", jwt)
                .build() .get()
                .uri("http://user-service/api/users/profile",
                        uriBuilder -> uriBuilder.build())
                .retrieve()
                .bodyToMono(UserResponse.class)//Kiểu dữ liệu trả về
                .block();
        System.out.println("User" + user.toString());
        if(user.getRole().equals("ROLE_ADMIN")) {
            Hotel hotel = new Hotel();
            hotel.setName(name);
            hotel.setAddress(address);
            hotel.setCity(city);
            hotel.setDescription(description);
            hotel.setPrice(price);
            hotel.setRating(5.0);
            if (!file.isEmpty()) {
                byte[] photoBytes = file.getBytes();
                Blob photoBlod = new SerialBlob(photoBytes);
                hotel.setPhoto(photoBlod);
            }
            return hotelRepository.save(hotel);
        }
        throw new RuntimeException("User has not role create Hotel");
    }

    public Hotel updateHotel(Long id,MultipartFile file, String name, String address, String city, String description, BigDecimal price) throws IOException, SQLException {
        Hotel hotel = getHotelByHotelId(id);
        if(name != null)
            hotel.setName(name);
        if(address != null)
            hotel.setAddress(address);
        if(city != null)
            hotel.setCity(city);
        if(description != null)
            hotel.setDescription(description);
        if(price != null)
            hotel.setPrice(price);
        if(file != null){
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

    public List<String> getAllCities() {
        List<String> citiesDistinct = hotelRepository.findDistinctCities();
        return citiesDistinct;
    }

    public List<Hotel> getHotelsByCity(String city) {
        return hotelRepository.findByCity(city);
    }

    public List<Hotel> searchHotel(String keyword) {
        return hotelRepository.searchHotel(keyword);
    }

    public List<byte[]> getHotelsImageByIds(List<Long> ids) throws SQLException {
        List<byte[]> hotelsImage = new ArrayList<>();
        for (Long id:ids) {
            hotelsImage.add(getHotelPhotoByHotelId(id));
            //break;
        }
        return hotelsImage;
    }

    public void deleteHotel(Long id) {
        Hotel hotel = getHotelByHotelId(id);
        hotelRepository.delete(hotel);
    }
}
