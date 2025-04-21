package com.lenin.hotel.services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.fasterxml.jackson.databind.JsonNode;
import com.lenin.hotel.authentication.model.Role;
import com.lenin.hotel.authentication.model.User;
import com.lenin.hotel.authentication.repository.RoleRepository;
import com.lenin.hotel.authentication.repository.UserRepository;
import com.lenin.hotel.booking.model.Location;
import com.lenin.hotel.common.enumuration.ERole;
import com.lenin.hotel.common.enumuration.ImageType;
import com.lenin.hotel.common.exception.BusinessException;
import com.lenin.hotel.common.exception.ResourceNotFoundException;
import com.lenin.hotel.hotel.dto.request.ChangePriceRequest;
import com.lenin.hotel.hotel.dto.request.HotelRequest;
import com.lenin.hotel.hotel.dto.request.PriceTrackingRequest;
import com.lenin.hotel.hotel.dto.response.HotelResponse;
import com.lenin.hotel.hotel.model.Amenity;
import com.lenin.hotel.hotel.model.Hotel;
import com.lenin.hotel.hotel.model.Image;
import com.lenin.hotel.hotel.model.PriceTracking;
import com.lenin.hotel.hotel.repository.*;
import com.lenin.hotel.hotel.service.impl.HotelServiceImpl;
import com.stripe.model.Price;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.util.*;

@ExtendWith(MockitoExtension.class)
class HotelServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private HotelRepository hotelRepository;
    @Mock
    private LocationRepository locationRepository;
    @Mock
    private PriceTrackingRepository priceTrackingRepository;
    @Mock
    private SecurityContext securityContext;
    @Mock
    private AmenityRepository amenityRepository;
    @Mock
    private ImageRepository imageRepository;

    @Mock
    private Authentication authentication;


    @Mock
    private RoleRepository roleRepository;

    @InjectMocks
    private HotelServiceImpl hotelService;
    private User user;
    private Role hotelOwnerRole;


    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);

        hotelOwnerRole = new Role();
        hotelOwnerRole.setName(ERole.ROLE_HOTEL);

        securityContext = mock(SecurityContext.class);  // Tạo mock SecurityContext
        authentication = mock(Authentication.class);    // Tạo mock Authentication

        SecurityContextHolder.setContext(securityContext);
        lenient().when(securityContext.getAuthentication()).thenReturn(authentication);
        lenient().when(authentication.getPrincipal()).thenReturn("testUser");  // Giả lập username từ JWT

    }

    @Test
    void testActiveHotelOwner_Success() {
        when(userRepository.getById(1)).thenReturn(Optional.of(user));
        when(roleRepository.findByName(ERole.ROLE_HOTEL)).thenReturn(Optional.of(hotelOwnerRole));

        hotelService.activeHotelOwner(1);

        assertTrue(user.getRoles().contains(hotelOwnerRole));
        verify(userRepository).save(user);
    }

    @Test
    void testActiveHotelOwner_UserNotFound() {
        when(userRepository.getById(1)).thenReturn(Optional.empty());

        Exception exception = assertThrows(ResourceNotFoundException.class, () -> hotelService.activeHotelOwner(1));
        assertEquals("User not found!", exception.getMessage());
    }

    @Test
    void testActiveHotelOwner_RoleNotFound() {
        when(userRepository.getById(1)).thenReturn(Optional.of(user));
        when(roleRepository.findByName(ERole.ROLE_HOTEL)).thenReturn(Optional.empty());

        Exception exception = assertThrows(ResourceNotFoundException.class, () -> hotelService.activeHotelOwner(1));
        assertEquals("Role not found!", exception.getMessage());
    }

    @Test
    void testCreateHotel() {
        // Arrange
        HotelRequest request = new HotelRequest();
        request.setLocationId(1);
        request.setAmenities(Set.of(1, 2, 3));
        request.setPrice(PriceTrackingRequest.builder().build());
        request.setImages(Collections.emptyList());

        List<Hotel> sampleHotels = List.of(Hotel.builder().avatar("hi").name("hotel1").build(), Hotel.builder().avatar("avatar2").name("hotel2").build());

        Location location = new Location();
        when(locationRepository.findById(request.getLocationId())).thenReturn(Optional.of(location));

        User user = new User();
        when(userRepository.getByUsername(any())).thenReturn(Optional.of(user));

        Hotel hotel = new Hotel();
        when(hotelRepository.save(any())).thenReturn(hotel);

        when(amenityRepository.findAllById(request.getAmenities())).thenReturn(List.of(Amenity.builder().id(1).hotels(new ArrayList<>(sampleHotels)).build(),
                Amenity.builder().id(2).hotels(new ArrayList<>(sampleHotels)).build(), Amenity.builder().id(3).hotels(new ArrayList<>(sampleHotels)).build()));
        when(imageRepository.saveAll(Collections.emptyList())).thenReturn(List.of());
        // Act
        hotelService.createHotel(request);

        // Assert
        verify(hotelRepository, times(1)).save(any());
        verify(amenityRepository, times(1)).findAllById(request.getAmenities());
    }

    @Test
    void testGetLatestPrice() {
        // Arrange
        PriceTracking priceTracking = new PriceTracking();
        priceTracking.setPrice(BigDecimal.valueOf(150.0));
        when(priceTrackingRepository.findTopByHotelIdOrderByCreateDtDesc(anyLong()))
                .thenReturn(Optional.of(priceTracking));

        // Act
        PriceTracking result = hotelService.getLatestPrice(1L);

        // Assert
        assertNotNull(result);
        assertEquals(BigDecimal.valueOf(150.0), result.getPrice());
    }

    @Test
    void testChangePrice() {
        // Arrange
        User user = new User();
        user.setId(1L);

        Hotel hotel = new Hotel();
        hotel.setOwner(user);

        ChangePriceRequest request = new ChangePriceRequest();
        request.setHotelId(1);
        request.setNewPrice(BigDecimal.valueOf(200.0));

        when(userRepository.getByUsername(any())).thenReturn(Optional.of(user));
        when(hotelRepository.findById(any())).thenReturn(Optional.of(hotel));

        // Act
        hotelService.changePrice(request);

        // Assert
        verify(priceTrackingRepository, times(1)).save(any());
    }

    @Test
    void testActiveHotelOwner() {
        // Arrange
        User user = new User();
        user.setId(1L);

        Role role = new Role();
        role.setName(ERole.ROLE_HOTEL);

        lenient().when(userRepository.getById(anyLong())).thenReturn(Optional.of(new User()));
        lenient().when(roleRepository.findByName(any())).thenReturn(Optional.of(new Role()));

        // Act
        hotelService.activeHotelOwner(1);

        // Assert
        verify(userRepository, times(1)).save(any());
    }

    @Test
    void testAddFavorite() {
        // Arrange
        User user = new User();
        Hotel hotel = new Hotel();
        hotel.setId(1);

        when(userRepository.getByUsername(any())).thenReturn(Optional.of(user));
        when(hotelRepository.findById(any())).thenReturn(Optional.of(hotel));

        // Act
        Map<String, String> response = hotelService.addFavorite(1);

        // Assert
        assertEquals("Successfully added favorite hotel.", response.get("message"));
    }

    @Test
    void testGetAllUserFavorite() {
        // Arrange
        User user = new User();
        Hotel hotel1 = new Hotel();
        hotel1.setId(1);
        Hotel hotel2 = new Hotel();
        hotel2.setId(2);
        user.setFavoriteHotels(new HashSet<>(List.of(hotel1, hotel2)));

        when(userRepository.getByUsername(any())).thenReturn(Optional.of(user));

        // Act
        List<Integer> result = hotelService.getAllUserFavorite();

        // Assert
        assertEquals(2, result.size());
        assertTrue(result.contains(1));
        assertTrue(result.contains(2));
    }

    @Test
    void testRemoveFavorite() {
        // Arrange
        User user = new User();
        Hotel hotel = new Hotel();
        hotel.setId(1);
        user.setFavoriteHotels(new HashSet<>(Set.of(hotel)));

        when(userRepository.getByUsername(any())).thenReturn(Optional.of(user));
        when(hotelRepository.findById(any())).thenReturn(Optional.of(hotel));

        // Act
        hotelService.removeFavorite(1);

        // Assert
        assertFalse(user.getFavoriteHotels().contains(hotel));
        verify(userRepository, times(1)).save(user);
    }

    @Test
    void testGetHotelsByHotelOwnerId() {
        // Arrange
        User user = new User();
        user.setId(1L);
        Hotel hotel1 = new Hotel();
        hotel1.setId(1);
        Hotel hotel2 = new Hotel();
        hotel2.setId(2);
        when(userRepository.getByUsername(any())).thenReturn(Optional.of(user));
        when(hotelRepository.findByOwner(user)).thenReturn(List.of(hotel1, hotel2));

        // Act
        List<Integer> result = hotelService.getHotelsByHotelOwnerId();

        // Assert
        assertEquals(2, result.size());
        assertTrue(result.contains(1));
        assertTrue(result.contains(2));
    }

    @Test
    void testUpdateHotel_Success() {
        // Arrange
        User user = new User();
        user.setId(1L);
        Hotel hotel = new Hotel();
        hotel.setId(1);
        hotel.setOwner(user);

        JsonNode hotelJson = mock(JsonNode.class);
        when(hotelJson.has("title")).thenReturn(true);
        when(hotelJson.get("title")).thenReturn(mock(JsonNode.class)); // Ensure non-null JsonNode
        when(hotelJson.get("title").asText()).thenReturn("Updated Hotel Name");

        when(userRepository.getByUsername(any())).thenReturn(Optional.of(user));
        when(hotelRepository.findById(any())).thenReturn(Optional.of(hotel));

        // Act
        hotelService.updateHotel(1, hotelJson);

        // Assert
        assertEquals("Updated Hotel Name", hotel.getName());
        verify(hotelRepository, times(1)).save(hotel);
    }

    @Test
    void testUpdateHotel_Unauthorized() {
        // Arrange
        User user = new User();
        user.setId(1L);
        Hotel hotel = new Hotel();
        hotel.setId(1);
        hotel.setOwner(new User()); // Different owner

        JsonNode hotelJson = mock(JsonNode.class);

        when(userRepository.getByUsername(any())).thenReturn(Optional.of(user));
        when(hotelRepository.findById(any())).thenReturn(Optional.of(hotel));

        // Act & Assert
        Exception exception = assertThrows(BusinessException.class, () -> hotelService.updateHotel(1, hotelJson));
        assertEquals("You do not have permission to change this hotel.", exception.getMessage());
    }

    @Test
    void testGetHotelById_Success() {
        // Arrange
        Hotel hotel = new Hotel();
        hotel.setId(1);
        hotel.setName("Test Hotel");
        hotel.setDescription("Sample description");
        hotel.setAddress("123 Street");
        hotel.setPhone("123456789");
        hotel.setEmail("hotel@example.com");
        hotel.setPolicy("No pets");
        hotel.setTotalReview(10);
        hotel.setRating(4.5);
        hotel.setLongitude(106.1234);
        hotel.setLatitude(10.5678);
        hotel.setGoogleMapEmbed("<iframe></iframe>");

// Owner
        User owner = new User();
        owner.setId(1L);
        hotel.setOwner(owner);

// Amenities
        Amenity amenity = new Amenity();
        amenity.setId(1);
        amenity.setName("Free Wi-Fi");
        hotel.setAmenities(List.of(amenity));

// Location
        Location location = new Location();
        location.setName("Ho Chi Minh City");
        hotel.setLocation(location);

// Bookings
        hotel.setBookings(Collections.emptyList());

// Images
        List<Image> images = Collections.emptyList(); // hoặc mock List<Image>

// Price
        PriceTracking priceTracking = new PriceTracking();
        priceTracking.setPrice(BigDecimal.valueOf(1200000));

// Mock repository calls
        when(hotelRepository.findById(1)).thenReturn(Optional.of(hotel));
        when(priceTrackingRepository.findTopByHotelIdOrderByCreateDtDesc(1L)).thenReturn(Optional.of(priceTracking));
        when(imageRepository.findByReferenceIdAndReferenceTableAndType(1, "hotels", ImageType.ROOM))
                .thenReturn(images);

        // Act
        HotelResponse response = hotelService.getHotelById(1);

        // Assert
        assertNotNull(response);
        assertEquals("Test Hotel", response.getName());
    }

    @Test
    void testGetHotelById_NotFound() {
        // Arrange
        when(hotelRepository.findById(any())).thenReturn(Optional.empty());

        // Act & Assert
        Exception exception = assertThrows(BusinessException.class, () -> hotelService.getHotelById(1));
        assertEquals("Hotel id not found", exception.getMessage());
    }

}
