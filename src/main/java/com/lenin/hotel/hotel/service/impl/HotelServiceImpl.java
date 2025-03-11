package com.lenin.hotel.hotel.service.impl;

import com.lenin.hotel.authentication.model.User;
import com.lenin.hotel.authentication.repository.UserRepository;
import com.lenin.hotel.booking.model.Location;
import com.lenin.hotel.common.enumuration.ImageType;
import com.lenin.hotel.common.exception.BusinessException;
import com.lenin.hotel.common.exception.ResourceNotFoundException;
import com.lenin.hotel.hotel.model.Amenity;
import com.lenin.hotel.hotel.model.Hotel;
import com.lenin.hotel.hotel.model.Image;
import com.lenin.hotel.hotel.model.PriceTracking;
import com.lenin.hotel.hotel.repository.*;
import com.lenin.hotel.hotel.request.HotelRequest;
import com.lenin.hotel.hotel.response.HotelResponse;
import com.lenin.hotel.hotel.service.IHotelService;
import com.lenin.hotel.hotel.utils.HotelUtils;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static com.lenin.hotel.common.utils.SecurityUtils.getCurrentUsername;
import static com.lenin.hotel.hotel.utils.HotelUtils.*;
import static com.lenin.hotel.hotel.utils.ImageUtils.buildImage;

@Service
@Transactional
@RequiredArgsConstructor
public class HotelServiceImpl implements IHotelService {
    private final HotelRepository hotelRepository;
    private final LocationRepository locationRepository;
    private final UserRepository userRepository;
    private final AmenityRepository amenityRepository;
    private final ImageRepository imageRepository;
    private final PriceTrackingRepository priceTrackingRepository;

    @Override
    public void createHotel(HotelRequest hotelRequest) {
        Hotel hotel = buildHotel(hotelRequest);
        Location location = locationRepository.findById(hotelRequest.getLocationId()).orElseThrow(() -> new ResourceNotFoundException("Location not found!"));
        hotel.setLocation(location);

        List<Amenity> amenities = Optional.of(amenityRepository.findAllById(hotelRequest.getAmenities()))
                .orElse(Collections.emptyList());
        if (amenities.size() != hotelRequest.getAmenities().size()) {
            throw new ResourceNotFoundException("One or more amenities not found!");
        }
        hotel.setAmenities(amenities);
        for (Amenity amenity: amenities) {
            amenity.getHotels().add(hotel);
        }
        amenityRepository.saveAll(amenities);

        User user = userRepository.getByUsername(getCurrentUsername()).orElseThrow(()-> new ResourceNotFoundException("User not found!"));
        List<Hotel> hotelList = hotelRepository.findByOwner(user);
        if (!hotelList.isEmpty()) {
            throw new BusinessException("You can only create one hotel!");
        }
        hotel.setOwner(user);
        hotelRepository.save(hotel);

        PriceTracking priceTracking = buildPriceTracking(hotelRequest.getPrice());
        priceTracking.setHotel(hotel);
        priceTrackingRepository.save(priceTracking);
        hotel.setPriceTrackings(Collections.singletonList(priceTracking));


        List<Image> images = new ArrayList<>();
        if (hotelRequest.getImages() != null && !hotelRequest.getImages().isEmpty()) {
            images = hotelRequest.getImages().stream().map(imageRequest -> buildImage(imageRequest, hotel)).toList();
        }
        imageRepository.saveAll(images);
    }

    @Override
    public List<HotelResponse> getAllRoom(Pageable pageable, Integer hotelId, Integer ownerId) {
        List<Hotel> hotels = new ArrayList<>();

        if (hotelId != null && hotelId > 0) {
            hotels = List.of(hotelRepository.findById(hotelId)
                    .orElseThrow(() -> new ResourceNotFoundException("Hotel not found!")));
        } else if (ownerId != null && ownerId > 0) {
            hotels = hotelRepository.findAllByOwnerId(ownerId);
        } else {
            hotels = hotelRepository.findAll(pageable).toList();
        }



        return hotels.stream().map(hotel -> {
            List<Image> images = imageRepository.findByReferenceIdAndReferenceTableAndType(
                    hotel.getId(), "hotel", ImageType.HOTEL);

            PriceTracking priceTracking = getPriceTracking(hotel.getId());
            if (priceTracking == null){
                return null;
            }
            return HotelUtils.buildHotelResponse(hotel, images, priceTracking.getPrice());
        }).filter(Objects::nonNull) .toList();
    }
    public PriceTracking getPriceTracking(Integer hotelId){
        ZonedDateTime now = ZonedDateTime.now();
        Optional<PriceTracking> currentPrice = priceTrackingRepository.findByHotelIdAndValidFromBeforeAndValidToAfter(
                hotelId, now, now);

        // Nếu không tìm thấy currentPrice, tìm giá gần nhất (trước hoặc sau hiện tại)
        if (currentPrice.isEmpty()) {
            currentPrice = priceTrackingRepository.findTopByHotelIdAndValidToBeforeOrderByValidToDesc(hotelId, now);
        }
        if (currentPrice.isEmpty()) {
            currentPrice = priceTrackingRepository.findTopByHotelIdAndValidFromAfterOrderByValidFromAsc(hotelId, now);
        }
        return currentPrice.orElse(null);
    }

}
