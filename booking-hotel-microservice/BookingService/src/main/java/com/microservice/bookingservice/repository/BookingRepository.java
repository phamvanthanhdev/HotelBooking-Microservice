package com.microservice.bookingservice.repository;

import com.microservice.bookingservice.model.BookedRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface BookingRepository extends JpaRepository<BookedRoom, Long> {
    @Query("SELECT b FROM BookedRoom b WHERE b.guestEmail = :email ORDER BY b.bookingId DESC")
    List<BookedRoom> getBookedRoomsByGuestEmailDESC(@Param("email") String email);

    BookedRoom findByBookingConfirmationCode(String code);
}
