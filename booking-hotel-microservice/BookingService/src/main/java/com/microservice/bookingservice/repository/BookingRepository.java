package com.microservice.bookingservice.repository;

import com.microservice.bookingservice.model.BookedRoom;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookingRepository extends JpaRepository<BookedRoom, Long> {
}
