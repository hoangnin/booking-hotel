package com.lenin.hotel.integration.repository.hotel;

    import com.lenin.hotel.authentication.model.User;
    import com.lenin.hotel.common.enumuration.BookingStatus;
    import com.lenin.hotel.configuration.DatabaseTestContainer;
    import com.lenin.hotel.configuration.TestDynamicProperties;
    import com.lenin.hotel.hotel.model.Booking;
    import com.lenin.hotel.hotel.model.Hotel;
    import com.lenin.hotel.hotel.model.Location;
    import com.lenin.hotel.hotel.model.PriceTracking;
    import com.lenin.hotel.hotel.repository.BookingRepository;
    import org.junit.jupiter.api.BeforeEach;
    import org.junit.jupiter.api.Test;
    import org.springframework.beans.factory.annotation.Autowired;
    import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
    import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
    import org.springframework.context.annotation.Import;
    import org.springframework.test.annotation.Rollback;
    import org.springframework.test.context.DynamicPropertyRegistry;
    import org.springframework.test.context.DynamicPropertySource;
    import org.testcontainers.containers.PostgreSQLContainer;

    import java.math.BigDecimal;
    import java.time.ZonedDateTime;
    import java.util.List;
    import java.util.Optional;

    import static org.assertj.core.api.Assertions.assertThat;

    @DataJpaTest
    @AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
    @Rollback
    @Import(DatabaseTestContainer.class)
    public class BookingRepositoryTest extends TestDynamicProperties {

        @Autowired
        private BookingRepository bookingRepository;

        @Autowired
        private org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager entityManager;

        private User user;
        private Hotel hotel;
        private Location location;
        private PriceTracking priceTracking;



        @BeforeEach
        public void setUp() {
            // Clear existing data
            bookingRepository.deleteAll();
            entityManager.flush();

            user = new User();
            user.setUsername("testuser");
            user.setEmail("test@example.com");
            user.setPassword("password123");
            user = entityManager.persistAndFlush(user);

            location = new Location();
            location.setName("location");
            location = entityManager.persistAndFlush(location);

            hotel = new Hotel();
            hotel.setName("Test Hotel");
            hotel.setLatitude(10.762622);
            hotel.setLongitude(106.660172);
            hotel.setLocation(location);
            hotel.setOwner(user);
            hotel = entityManager.persistAndFlush(hotel);

            priceTracking = new PriceTracking();
            priceTracking.setPrice(BigDecimal.valueOf(100.0));
            priceTracking.setHotel(hotel);
            priceTracking = entityManager.persistAndFlush(priceTracking);
        }
        @Test
        public void testExistsOverlappingBooking() {
            // Arrange
            Booking booking = new Booking();
            booking.setHotel(hotel);
            booking.setUser(user);
            booking.setPriceTracking(priceTracking);
            booking.setCheckIn(ZonedDateTime.now().plusDays(1));
            booking.setCheckOut(ZonedDateTime.now().plusDays(3));
            bookingRepository.save(booking);

            // Act
            boolean exists = bookingRepository.existsOverlappingBooking(
                hotel.getId(),
                ZonedDateTime.now().plusDays(2),
                ZonedDateTime.now().plusDays(4)
            );

            // Assert
            assertThat(exists).isTrue();
        }

        @Test
        public void testFindAllByUser() {
            // Arrange
            Booking booking = new Booking();
            booking.setHotel(hotel);
            booking.setUser(user);
            booking.setPriceTracking(priceTracking);
            booking.setCheckIn(ZonedDateTime.now().plusDays(1));
            booking.setCheckOut(ZonedDateTime.now().plusDays(3));
            bookingRepository.save(booking);

            // Act
            List<Booking> bookings = bookingRepository.findAllByUser(user, null).getContent();

            // Assert
            assertThat(bookings).hasSize(1);
            assertThat(bookings.get(0).getUser().getId()).isEqualTo(user.getId());
        }

        @Test
        public void testExistsByUserAndHotelId() {
            // Arrange
            Booking booking = new Booking();
            booking.setHotel(hotel);
            booking.setUser(user);
            booking.setPriceTracking(priceTracking);
            booking.setCheckIn(ZonedDateTime.now().plusDays(1));
            booking.setCheckOut(ZonedDateTime.now().plusDays(3));
            bookingRepository.save(booking);

            // Act
            boolean exists = bookingRepository.existsByUserAndHotelId(user, hotel.getId());

            // Assert
            assertThat(exists).isTrue();
        }

        @Test
        public void testFindByStatusAndCreateDtBetween() {
            // Arrange
            Booking booking = new Booking();
            booking.setHotel(hotel);
            booking.setUser(user);
            booking.setPriceTracking(priceTracking);
            booking.setStatus(BookingStatus.CONFIRMED);
            booking.setCheckIn(ZonedDateTime.now().plusDays(1));
            booking.setCheckOut(ZonedDateTime.now().plusDays(3));
            bookingRepository.save(booking);

            // Act
            List<Booking> bookings = bookingRepository.findByStatusAndCreateDtBetween(
                BookingStatus.CONFIRMED,
                ZonedDateTime.now().minusDays(1),
                ZonedDateTime.now().plusDays(5)
            );

            // Assert
            assertThat(bookings).hasSize(1);
        }

        @Test
        public void testCountByStatusAndCreateDtBetween() {
            // Arrange
            Booking booking = new Booking();
            booking.setHotel(hotel);
            booking.setUser(user);
            booking.setPriceTracking(priceTracking);
            booking.setStatus(BookingStatus.CONFIRMED);
            booking.setCheckIn(ZonedDateTime.now().plusDays(1));
            booking.setCheckOut(ZonedDateTime.now().plusDays(3));
            bookingRepository.save(booking);

            // Act
            long count = bookingRepository.countByStatusAndCreateDtBetween(
                BookingStatus.CONFIRMED,
                ZonedDateTime.now().minusDays(1),
                ZonedDateTime.now().plusDays(5)
            );

            // Assert
            assertThat(count).isEqualTo(1);
        }

        @Test
        public void testCountBookingsBetweenDates() {
            // Arrange
            Booking booking = new Booking();
            booking.setHotel(hotel);
            booking.setUser(user);
            booking.setPriceTracking(priceTracking);
            booking.setStatus(BookingStatus.CONFIRMED);
            booking.setCheckIn(ZonedDateTime.now().plusDays(1));
            booking.setCheckOut(ZonedDateTime.now().plusDays(3));
            bookingRepository.save(booking);

            // Act
            List<Object[]> counts = bookingRepository.countBookingsBetweenDates(
                ZonedDateTime.now().minusDays(1),
                ZonedDateTime.now().plusDays(5)
            );

            // Assert
            assertThat(counts).isNotEmpty();
        }

        @Test
        public void testFindByStatus() {
            // Arrange
            Booking booking = new Booking();
            booking.setHotel(hotel);
            booking.setUser(user);
            booking.setPriceTracking(priceTracking);
            booking.setStatus(BookingStatus.CONFIRMED);
            booking.setCheckIn(ZonedDateTime.now().plusDays(1));
            booking.setCheckOut(ZonedDateTime.now().plusDays(3));
            bookingRepository.saveAndFlush(booking);

            // Act
            List<Booking> bookings = bookingRepository.findByStatus(BookingStatus.CONFIRMED);

            // Assert
            assertThat(bookings).hasSize(1);
            assertThat(bookings.get(0).getStatus()).isEqualTo(BookingStatus.CONFIRMED);
        }
    }