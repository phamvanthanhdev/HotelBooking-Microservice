package com.microservice.repository;

import com.microservice.model.Hotel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface HotelRepository extends JpaRepository<Hotel, Long> {
    @Query("SELECT DISTINCT h.city FROM Hotel h")
    List<String> findDistinctCities();

    List<Hotel> findByCity(String city);
    @Query("SELECT h FROM Hotel h WHERE h.name LIKE %:keyword% ")
    List<Hotel> searchHotel(@Param("keyword") String keyword);
}
