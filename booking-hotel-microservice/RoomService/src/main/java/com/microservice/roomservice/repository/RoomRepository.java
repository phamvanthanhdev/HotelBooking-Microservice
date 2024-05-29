package com.microservice.roomservice.repository;

import com.microservice.roomservice.model.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface RoomRepository extends JpaRepository<Room, Long> {
    @Query("SELECT DISTINCT r.roomType FROM Room r")
    List<String> findDistinctRoomTypes();

    List<Room> findAllByHotelId(Long hotelId);

    // Tìm các loại phòng theo hotelId
    @Query("SELECT DISTINCT r.roomType FROM Room r WHERE r.hotelId = :hotelId")
    List<String> findDistinctRoomTypesByHotelId(Long hotelId);

    // Tìm các phòng theo hotelId và RoomType
    @Query("SELECT r FROM Room r WHERE r.hotelId = :hotelId AND r.roomType = :roomType")
    List<Room> findByRoomTypeAndHotelId(Long hotelId,String roomType);

    //sine
}
