package com.lenin.hotel.hotel;

import com.lenin.hotel.booking.model.Booking;
import com.lenin.hotel.booking.model.Location;
import com.lenin.hotel.hotel.model.Hotel;
import com.lenin.hotel.hotel.model.PriceTracking;
import jakarta.persistence.criteria.*;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class HotelSpecification {

    public static Specification<Hotel> filterHotels(
            String name, Integer locationId, Double rating,
            Set<Integer> amenityIds, Double minPrice, Double maxPrice,
            Integer minRoomsAvailable, String hotelType, ZonedDateTime checkIn,
            ZonedDateTime checkOut) {

        return (Root<Hotel> root, CriteriaQuery<?> query, CriteriaBuilder cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (name != null && !name.isEmpty()) {
                String likePattern = "%" + name.toLowerCase() + "%";

                // hotel.name
                Predicate namePredicate = cb.like(cb.lower(root.get("name")), likePattern);

                // hotel.address
                Predicate addressPredicate = cb.like(cb.lower(root.get("address")), likePattern);

                // location.name (cần join location trước)
                Join<Hotel, Location> locationJoin = root.join("location", JoinType.INNER);
                Predicate locationNamePredicate = cb.like(cb.lower(locationJoin.get("name")), likePattern);

                predicates.add(cb.or(namePredicate, addressPredicate, locationNamePredicate));
            }

            // Join bảng Location
            Join<Hotel, Location> locationJoin = root.join("location", JoinType.INNER);

            // Lọc theo locationId
            if (locationId != null) {
                predicates.add(cb.equal(locationJoin.get("id"), locationId));
            }


            if (rating != null) {
                if (rating == 5) {
                    predicates.add(cb.equal(root.get("rating"), 5));
                } else {
                    predicates.add(cb.between(root.get("rating"), rating, rating + 1));
                }
            }


            if (amenityIds != null && !amenityIds.isEmpty()) {
                // Subquery để đếm số lượng tiện ích của khách sạn có trong danh sách amenityIds
                Subquery<Long> subquery = query.subquery(Long.class);
                Root<Hotel> subRoot = subquery.from(Hotel.class);
                Join<Object, Object> subAmenityJoin = subRoot.join("amenities", JoinType.INNER);

                subquery.select(cb.count(subAmenityJoin.get("id")))
                        .where(cb.equal(subRoot.get("id"), root.get("id")),
                                subAmenityJoin.get("id").in(amenityIds));

                // Điều kiện: Số lượng amenities phải bằng đúng số lượng trong amenityIds
                predicates.add(cb.equal(subquery, (long) amenityIds.size()));
            }

            // Tạo JOIN giữa Hotel và PriceTracking
            Join<Hotel, PriceTracking> priceTrackingJoin = root.join("priceTrackings", JoinType.INNER);

            // Lọc theo khoảng giá từ bảng PriceTracking
            if (minPrice != null) {
                predicates.add(cb.greaterThanOrEqualTo(priceTrackingJoin.get("price"), minPrice));
            }
            if (maxPrice != null) {
                predicates.add(cb.lessThanOrEqualTo(priceTrackingJoin.get("price"), maxPrice));
            }


            // Lọc theo số lượng phòng trống
            if (minRoomsAvailable != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("roomsAvailable"), minRoomsAvailable));
            }

            // Lọc theo loại khách sạn (hotelType)
            if (hotelType != null && !hotelType.isEmpty()) {
                predicates.add(cb.equal(cb.lower(root.get("hotelType")), hotelType.toLowerCase()));
            }

            // Tạo Subquery để kiểm tra khách sạn có bị đặt hết không
            Subquery<Integer> subquery = query.subquery(Integer.class);
            Root<Booking> subRoot = subquery.from(Booking.class);
            subquery.select(cb.literal(1)); // Chỉ cần check xem có booking nào trùng hay không
            subquery.where(
                    cb.equal(subRoot.get("hotel").get("id"), root.get("id")), // Booking thuộc khách sạn đó
                    cb.or(
                            // TH1: checkIn yêu cầu nằm giữa một booking đã có (phòng đã có khách ở)
                            cb.and(
                                    cb.lessThanOrEqualTo(subRoot.get("checkIn"), checkIn),
                                    cb.greaterThan(subRoot.get("checkOut"), checkIn)
                            ),
                            // TH2: checkOut yêu cầu nằm giữa một booking đã có (phòng đã có khách ở)
                            cb.and(
                                    cb.lessThan(subRoot.get("checkIn"), checkOut),
                                    cb.greaterThanOrEqualTo(subRoot.get("checkOut"), checkOut)
                            ),
                            // TH3: Khoảng thời gian yêu cầu bao trùm khoảng thời gian đã đặt trước đó
                            cb.and(
                                    cb.greaterThanOrEqualTo(subRoot.get("checkIn"), checkIn),
                                    cb.lessThanOrEqualTo(subRoot.get("checkOut"), checkOut)
                            )
                    )
            );

        // Loại bỏ các khách sạn đã bị đặt kín trong khoảng thời gian yêu cầu
            predicates.add(cb.not(cb.exists(subquery)));



            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

}
