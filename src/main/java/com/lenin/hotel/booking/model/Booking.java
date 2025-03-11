package com.lenin.hotel.booking.model;

import com.lenin.hotel.authentication.model.User;
import com.lenin.hotel.booking.enumuration.BookingStatus;
import com.lenin.hotel.hotel.model.Hotel;
import com.lenin.hotel.hotel.model.PriceTracking;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.ZonedDateTime;
import java.util.Date;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
//@org.hibernate.annotations.SQLRestriction("EXCLUDE USING gist (hotel_id WITH =, tstzrange(check_in, check_out, '[]') WITH &&)")
public class Booking {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @Enumerated(EnumType.STRING)
    private BookingStatus status;
    @ManyToOne
    @JoinColumn(name = "hotel_id", nullable = false)
    private Hotel hotel;

    private String note;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "price_tracking_id", nullable = false)
    private PriceTracking priceTracking;

    @Column(name = "check_in")
    private ZonedDateTime checkIn;

    @Column(name = "check_out")
    private ZonedDateTime checkOut;

    @Column(name = "create_dt")
    @CreationTimestamp
    private ZonedDateTime createDt;

    @Column(name = "update_dt")
    @UpdateTimestamp
    private ZonedDateTime updateDt;
}
