package com.lenin.hotel.hotel.model;

import com.lenin.hotel.authentication.model.User;
import com.lenin.hotel.booking.model.Booking;
import com.lenin.hotel.booking.model.Location;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Hotel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String name;
    private String description;
    private String address;
    private String phone;
    private String email;
    private Double rating;
    private Integer reviews;
    private String policy;

    @ManyToMany(mappedBy = "favoriteHotels")
    private Set<User> userFavorited = new HashSet<>();

    @OneToMany(mappedBy = "hotel", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Booking> bookings;

    @OneToMany(mappedBy = "hotel", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PriceTracking> priceTrackings;

    @ManyToOne
    @JoinColumn(name = "location_id", nullable = false)
    private Location location;

    @ManyToMany
    @JoinTable(
            name = "hotel_amenity",
            joinColumns = @JoinColumn(name = "hotel_id"),
            inverseJoinColumns = @JoinColumn(name = "amenity_id")
    )
    private List<Amenity> amenities = new ArrayList<>();

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "owner_id", unique = true)
    private User owner;

    @Column(name = "create_dt")
    @CreationTimestamp
    private ZonedDateTime createDt;

    @Column(name = "update_dt")
    @UpdateTimestamp
    private ZonedDateTime updateDt;



}
