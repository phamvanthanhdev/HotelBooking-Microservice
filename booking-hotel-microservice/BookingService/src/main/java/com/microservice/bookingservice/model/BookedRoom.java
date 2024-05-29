package com.microservice.bookingservice.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class BookedRoom {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long bookingId;
    @Column(name = "check_In")
    private LocalDate checkInDate;
    @Column(name = "check_Out")
    private LocalDate checkOutDate;
    @Column(name = "guest_FullName")
    private String guestFullName;
    @Column(name = "guest_Email")
    private String guestEmail;
    @Column(name = "adults")
    private int numOfAdults;
    @Column(name = "children")
    private int numOfChildren;
    @Column(name = "total_guest")
    private int totalNumOfGuest;
    @Lob
    @Column(name = "note", length = 1000)
    private String note;
    @Column(name = "total_price")
    private BigDecimal totalPrice;
    @Column(name = "confirmation_Code")
    private String bookingConfirmationCode;
    @Column(name = "room_Id")
    private Long roomId;

    public BookedRoom(LocalDate checkInDate, LocalDate checkOutDate, String guestFullName,
                      String guestEmail, int numOfAdults, int numOfChildren,
                      int totalNumOfGuest, String note, BigDecimal totalPrice, Long roomId) {
        this.checkInDate = checkInDate;
        this.checkOutDate = checkOutDate;
        this.guestFullName = guestFullName;
        this.guestEmail = guestEmail;
        this.numOfAdults = numOfAdults;
        this.numOfChildren = numOfChildren;
        this.totalNumOfGuest = totalNumOfGuest;
        this.note = note;
        this.totalPrice = totalPrice;
        this.roomId = roomId;
    }

    public void calculateTotalNumberOfGuest(){
        this.totalNumOfGuest = this.numOfAdults + this.numOfChildren;
    }

    public void setNumOfAdults(int numOffAdults) {
        this.numOfAdults = numOffAdults;
        calculateTotalNumberOfGuest();
    }

    public void setNumOfChildren(int numOffChildren) {
        this.numOfChildren = numOffChildren;
        calculateTotalNumberOfGuest();
    }

    public void setBookingConfirmationCode(String bookingConfirmationCode) {
        this.bookingConfirmationCode = bookingConfirmationCode;
    }
}
