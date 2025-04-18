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
@Table(name = "hotels")
public class Hotel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String name;
    private String description;
    private String address;
    private String phone;
    private String email;
    private String avatar;
    private Double rating;
    private Integer totalReview = 0;
    private String policy;

    @Column(name = "latitude", nullable = false)
    private Double latitude; // Tọa độ Lat

    @Column(name = "longitude", nullable = false)
    private Double longitude; // Tọa độ Lng

    @Column(name = "google_map_embed", columnDefinition = "TEXT")
    private String googleMapEmbed; // Link nhúng Google Map (iframe URL)

    @ManyToMany(mappedBy = "favoriteHotels")
    private Set<User> userFavorited = new HashSet<>();

    @OneToMany(mappedBy = "hotel", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Booking> bookings;

    @OneToMany(mappedBy = "hotel", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PriceTracking> priceTrackings;

    @OneToMany(mappedBy = "hotel", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Review> reviews = new ArrayList<>();

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

    @ManyToOne
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    @Column(name = "create_dt")
    @CreationTimestamp
    private ZonedDateTime createDt;

    @Column(name = "update_dt")
    @UpdateTimestamp
    private ZonedDateTime updateDt;



}
