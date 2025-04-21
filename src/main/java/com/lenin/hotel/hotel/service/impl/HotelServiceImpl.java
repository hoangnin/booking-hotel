package com.lenin.hotel.hotel.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.lenin.hotel.authentication.model.Role;
import com.lenin.hotel.authentication.model.User;
import com.lenin.hotel.authentication.repository.RoleRepository;
import com.lenin.hotel.authentication.repository.UserRepository;
import com.lenin.hotel.booking.enumuration.BookingStatus;
import com.lenin.hotel.booking.model.Booking;
import com.lenin.hotel.booking.model.Location;
import com.lenin.hotel.common.enumuration.ERole;
import com.lenin.hotel.common.enumuration.ImageType;
import com.lenin.hotel.common.exception.BusinessException;
import com.lenin.hotel.common.exception.ResourceNotFoundException;
import com.lenin.hotel.common.service.IEmailService;
import com.lenin.hotel.common.service.PdfGeneratorService;
import com.lenin.hotel.hotel.HotelSpecification;
import com.lenin.hotel.hotel.dto.request.ChangePriceRequest;
import com.lenin.hotel.hotel.dto.request.PriceTrackingRequest;
import com.lenin.hotel.hotel.dto.response.QuickOverview;
import com.lenin.hotel.hotel.model.Amenity;
import com.lenin.hotel.hotel.model.Hotel;
import com.lenin.hotel.hotel.model.Image;
import com.lenin.hotel.hotel.model.PriceTracking;
import com.lenin.hotel.hotel.repository.*;
import com.lenin.hotel.hotel.dto.request.HotelRequest;
import com.lenin.hotel.hotel.dto.response.BookedDateRange;
import com.lenin.hotel.hotel.dto.response.HotelResponse;
import com.lenin.hotel.hotel.service.IHotelService;
import com.lenin.hotel.hotel.utils.HotelUtils;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

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
    private final RoleRepository roleRepository;
    private final PdfGeneratorService pdfGeneratorService;
    private final IEmailService emailService;
    private final BookingRepository bookingRepository;

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
        for (Amenity amenity : amenities) {
            amenity.getHotels().add(hotel);
        }
        amenityRepository.saveAll(amenities);

        User user = userRepository.getByUsername(getCurrentUsername()).orElseThrow(() -> new ResourceNotFoundException("User not found!"));
        hotel.setOwner(user);
        hotelRepository.save(hotel);

        PriceTracking priceTracking = buildPriceTracking(hotelRequest.getPrice());
        priceTracking.setHotel(hotel);
        priceTrackingRepository.save(priceTracking);
        hotel.setPriceTrackings(Collections.singletonList(priceTracking));


        List<Image> images = new ArrayList<>();
        if (hotelRequest.getImages() != null && !hotelRequest.getImages().isEmpty()) {
            images = hotelRequest.getImages().stream().map(imageRequest -> buildImage(imageRequest, hotel.getId(), "hotels")).toList();
        }
        imageRepository.saveAll(images);
    }

    @Override
    public List<HotelResponse> getAllHotel(Pageable pageable, Integer hotelId, Integer ownerId, Double latitude, Double longitude) {
        List<Hotel> hotels = new ArrayList<>();

        if (hotelId != null && hotelId > 0) {
            hotels = List.of(hotelRepository.findById(hotelId)
                    .orElseThrow(() -> new ResourceNotFoundException("Hotel not found!")));
        } else if (ownerId != null && ownerId > 0) {
            hotels = hotelRepository.findAllByOwnerId(ownerId);
        } else if (latitude != null && longitude != null) {
            int offset = pageable.getPageSize() * pageable.getPageNumber();
            hotels = hotelRepository.findNearbyHotels(latitude, longitude, pageable.getPageSize(), offset);
        } else {
            hotels = hotelRepository.findAll(pageable).toList();
        }


        return hotels.stream()
                .map(this::convertToHotelResponse)
                .filter(Objects::nonNull)
                .toList();
    }
    public HotelResponse convertToHotelResponse(Hotel hotel) {
        List<BookedDateRange> bookedDateRanges = Optional.ofNullable(hotel.getBookings())
                .orElse(Collections.emptyList()) // Handle null bookings
                .stream()
                .map(booking -> BookedDateRange.builder()
                        .checkIn(booking.getCheckIn())
                        .checkOut(booking.getCheckOut())
                        .build())
                .toList();

        List<Image> images = imageRepository.findByReferenceIdAndReferenceTableAndType(
                hotel.getId(), "hotels", ImageType.ROOM);

        PriceTracking priceTracking = getLatestPrice(Long.valueOf(hotel.getId()));


        return HotelUtils.buildHotelResponse(hotel, images, priceTracking.getPrice(), bookedDateRanges);
    }

    public PriceTracking getLatestPrice(Long hotelId) {
        return priceTrackingRepository.findTopByHotelIdOrderByCreateDtDesc(hotelId)
                .orElseThrow(() -> new ResourceNotFoundException("Not found price for hotelId: " + hotelId));
    }

    @Override
    public void activeHotelOwner(Integer hotelOwnerId) {
        User user = userRepository.getById(hotelOwnerId).orElseThrow(() -> new ResourceNotFoundException("User not found!"));

        Role hotelOwnerRole = roleRepository.findByName(ERole.ROLE_HOTEL)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found!"));
        user.addRole(hotelOwnerRole);
        userRepository.save(user);
    }

    @Override
    public List<HotelResponse> searchHotels(String name, Integer locationId, Double rating,
                                            Set<Integer> amenityIds, Double minPrice, Double maxPrice,
                                            Integer minRoomsAvailable, String hotelType,
                                            ZonedDateTime checkin, ZonedDateTime checkout) {
        Specification<Hotel> spec = HotelSpecification.filterHotels(name, locationId, rating,
                amenityIds, minPrice, maxPrice, minRoomsAvailable, hotelType, checkin, checkout);
        return hotelRepository.findAll(spec).stream().map(this::convertToHotelResponse).toList();
    }

    @Override
    public void changePrice(ChangePriceRequest changePriceRequest) {
        User user = userRepository.getByUsername(getCurrentUsername()).orElseThrow(() -> new ResourceNotFoundException("User not found!"));
        Hotel hotel = hotelRepository.findById(changePriceRequest.getHotelId()).orElseThrow(() -> new RuntimeException("Hotel not found!"));

        if (!hotel.getOwner().getId().equals(user.getId())) {
            throw new AccessDeniedException("You do not have permission to change this hotel's price.");
        }

        PriceTracking priceTracking = PriceTracking.builder()
                .price(changePriceRequest.getNewPrice())
                .hotel(hotel)
                .build();
        priceTrackingRepository.save(priceTracking);
    }

    @Override
    public void generateAndSendHotelOwnerReport() {
        User user = userRepository.getByUsername(getCurrentUsername())
                .orElseThrow(() -> new RuntimeException("User not found!"));

        List<Hotel> hotels = hotelRepository.findByOwner(user);

        // Tạo PDF
        if (hotels.isEmpty()) {
            throw new RuntimeException("No hotels found for this user.");
        }
        byte[] pdfData;
        if (user.getRoles().stream().anyMatch(role -> role.getName() == ERole.ROLE_HOTEL)) {
            pdfData = pdfGeneratorService.generateAdminHotelReport(hotels);
        }else {
            pdfData = pdfGeneratorService.generateOwnerHotelReport(user.getUsername(), hotels);
        }

        // Gửi email
        String fileName = pdfGeneratorService.generatePdfFileName();
        emailService.sendEmailWithPdf(
                user.getEmail(),
                "Hotel Report",
                "Attached is the list of hotels you own.",
                pdfData,
                fileName
        );
    }

    @Override
    public Map<String, String> addFavorite(Integer hotelId) {
        User user = userRepository.getByUsername(getCurrentUsername()).orElseThrow(() -> new RuntimeException("User not found!"));
        Hotel hotel = hotelRepository.findById(hotelId).orElseThrow(() -> new RuntimeException("Hotel not found!"));
        user.getFavoriteHotels().add(hotel);
        userRepository.save(user);
        return Map.of("message", "Successfully added favorite hotel.");
    }

    @Override
    public void removeFavorite(Integer hotelId) {
        User user = userRepository.getByUsername(getCurrentUsername()).orElseThrow(() -> new RuntimeException("User not found!"));
        Hotel hotel = hotelRepository.findById(hotelId).orElseThrow(() -> new RuntimeException("Hotel not found!"));
        user.getFavoriteHotels().remove(hotel);
        userRepository.save(user);
    }

    @Override
    public List<Integer> getHotelsByHotelOwnerId() {
        User user = userRepository.getByUsername(getCurrentUsername()).orElseThrow(() -> new RuntimeException("User not found!"));
        List<Hotel> hotels = hotelRepository.findByOwner(user);
        return hotels.stream().map(Hotel::getId).collect(Collectors.toList());
    }

    public void updateHotel(Integer id, JsonNode hotelJson) {
        User user = userRepository.getByUsername(getCurrentUsername()).orElseThrow(() -> new RuntimeException("User not found!"));

        Hotel hotel = hotelRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Hotel not found"));

        if (!user.getId().equals(hotel.getOwner().getId())) {
            throw new BusinessException("You do not have permission to change this hotel.");
        }

        if (hotelJson.has("title")) {
            hotel.setName(hotelJson.get("title").asText());
        }

        if (hotelJson.has("description")) {
            hotel.setDescription(hotelJson.get("description").asText());
        }

        if (hotelJson.has("address")) {
            hotel.setAddress(hotelJson.get("address").asText());
        }

        if (hotelJson.has("price")) {
            PriceTrackingRequest priceTrackingRequest = PriceTrackingRequest.builder().price(new BigDecimal(String.valueOf(hotelJson.get("price")))).build();
            PriceTracking priceTracking = buildPriceTracking(priceTrackingRequest);
            priceTracking.setHotel(hotel);
            priceTrackingRepository.save(priceTracking);
            hotel.getPriceTrackings().add(priceTracking);
        }

        if (hotelJson.has("phone")) {
            hotel.setPhone(hotelJson.get("phone").asText());
        }

        if (hotelJson.has("email")) {
            hotel.setEmail(hotelJson.get("email").asText());
        }

        if (hotelJson.has("policy")) {
            hotel.setPolicy(hotelJson.get("policy").asText());
        }

        if (hotelJson.has("latitude")) {
            hotel.setLatitude(hotelJson.get("latitude").asDouble());
        }

        if (hotelJson.has("longitude")) {
            hotel.setLongitude(hotelJson.get("longitude").asDouble());
        }

        if (hotelJson.has("googleMapEmbed")) {
            hotel.setGoogleMapEmbed(hotelJson.get("googleMapEmbed").asText());
        }

        // Xử lý amenities (List<String> ID tiện ích)
        if (hotelJson.has("amenities") && hotelJson.get("amenities").isArray()) {
            List<String> amenityIds = new ArrayList<>();
            for (JsonNode node : hotelJson.get("amenities")) {
                amenityIds.add(node.asText());
            }
            List<Amenity> amenities = amenityRepository.findAllByIdIn(amenityIds);
            hotel.setAmenities(amenities);
        }

        // Xử lý ảnh
        if (hotelJson.has("images") && hotelJson.get("images").isArray()) {
            List<Image> newImages = new ArrayList<>();
            Set<String> incomingImageIds = new HashSet<>();

            for (JsonNode imageNode : hotelJson.get("images")) {
                if (imageNode.has("id")) {
                    // Ảnh cũ, giữ lại
                    incomingImageIds.add(imageNode.get("id").asText());
                } else {
                    // Ảnh mới
                    Image image = Image.builder()
                            .url(imageNode.get("url").asText())
                            .type(ImageType.ROOM)
                            .referenceId(hotel.getId())
                            .referenceTable("hotels")
                            .build();
                    newImages.add(image);
                }
            }

            // Lấy tất cả ảnh hiện tại
            List<Image> existingImages = imageRepository.findByReferenceIdAndReferenceTableAndType(hotel.getId(), "hotels", ImageType.ROOM);

            // Xác định ảnh cần xóa
            List<Image> toDelete = existingImages.stream()
                    .filter(img -> !incomingImageIds.contains(img.getId().toString()))
                    .collect(Collectors.toList());

            // Xóa ảnh không còn nữa
            imageRepository.deleteAll(toDelete);

            // Lưu ảnh mới
            imageRepository.saveAll(newImages);
        }


        hotelRepository.save(hotel);
    }

    @Override
    public List<Map<String, Object>> getCombinedChartData() {
        List<YearMonth> last5Months = IntStream.rangeClosed(0, 4)
                .mapToObj(i -> YearMonth.now().minusMonths(4 - i))
                .toList();

        List<Map<String, Object>> result = new ArrayList<>();

        for (YearMonth ym : last5Months) {
            BigDecimal revenue = calculateRevenueByMonth(ym);
            BigDecimal profit = revenue.multiply(BigDecimal.valueOf(0.3)); // 30%
            long bookings = countBookingByMonth(ym);

            Map<String, Object> entry = new HashMap<>();
            entry.put("date", ym.getMonth().getDisplayName(TextStyle.SHORT, Locale.ENGLISH) + " " + (ym.getYear() % 100));
            entry.put("Revenue", revenue);
            entry.put("Profit", profit);
            entry.put("Bookings", bookings);

            result.add(entry);
        }

        return result;
    }

    @Override
    public List<Map<String, String>> getTopHotelsByRevenue(int numTop) {
        List<Booking> bookings = bookingRepository.findByStatus(BookingStatus.CONFIRMED);

        Map<Long, BigDecimal> revenueMap = new HashMap<>();

        for (Booking booking : bookings) {
            Long hotelId = Long.valueOf(booking.getHotel().getId());
            long days = ChronoUnit.DAYS.between(booking.getCheckIn(), booking.getCheckOut());
            BigDecimal revenue = booking.getPriceTracking().getPrice().multiply(BigDecimal.valueOf(days));

            revenueMap.merge(hotelId, revenue, BigDecimal::add);
        }

        return revenueMap.entrySet().stream()
                .sorted(Map.Entry.<Long, BigDecimal>comparingByValue().reversed())
                .limit(numTop)
                .map(entry -> {
                    Hotel hotel = hotelRepository.findById(Math.toIntExact(entry.getKey()))
                            .orElseThrow(() -> new RuntimeException("Hotel not found"));

                    Map<String, String> map = new HashMap<>();
                    map.put("hotelId", String.valueOf(hotel.getId()));
                    map.put("hotelName", hotel.getName());
                    map.put("revenue", entry.getValue().toString());
                    return map;
                })
                .collect(Collectors.toList());
    }




    @Override
    public List<Map<String, Object>> dashboardMonthlyBooking() {
        // Lấy 12 tháng gần nhất (bao gồm tháng hiện tại)
        List<YearMonth> last12Months = IntStream.rangeClosed(0, 11)
                .mapToObj(i -> YearMonth.now().minusMonths(11 - i))
                .toList();

        // Truy vấn toàn bộ bookings trong 12 tháng gần nhất
        ZonedDateTime fromDate = last12Months.get(0).atDay(1).atStartOfDay(ZoneId.systemDefault());
        ZonedDateTime toDate = last12Months.get(11).atEndOfMonth().atTime(23, 59).atZone(ZoneId.systemDefault());

        List<Object[]> results = bookingRepository.countBookingsBetweenDates(fromDate, toDate);

        // Dữ liệu trả về
        List<Map<String, Object>> chartData = new ArrayList<>();
        for (YearMonth ym : last12Months) {
            Optional<Object[]> match = results.stream()
                    .filter(obj -> {
                        int year = ((Integer) obj[0]);
                        int month = ((Integer) obj[1]);
                        return ym.getYear() == year && ym.getMonthValue() == month;
                    })
                    .findFirst();

            Long count = match.map(obj -> (Long) obj[2]).orElse(0L);

            chartData.add(Map.of(
                    "date", ym.getMonth().getDisplayName(TextStyle.SHORT, Locale.ENGLISH) + " " + ym.getYear(),
                    "bookings", count
            ));
        }

        return chartData;
    }

    @Override
    public Map<String, Integer> hotelByLocation() {
        List<Location> locations = locationRepository.findAll();
        Map<String, Integer> hotelByLocation = new HashMap<>();
        for (Location location : locations) {
            Integer numOfHotel = location.getHotels().size();
            hotelByLocation.put(location.getName(), numOfHotel);
        }
        return hotelByLocation;
    }

    @Override
    public QuickOverview dashboardOverview() {
        YearMonth currentMonth = YearMonth.now();
        YearMonth lastMonth = currentMonth.minusMonths(1);

        // Revenue
        BigDecimal currentRevenue = calculateRevenueByMonth(currentMonth);
        BigDecimal lastRevenue = calculateRevenueByMonth(lastMonth);
        int revenueDiff = calculateDiffPercentage(currentRevenue, lastRevenue);

        // Profit (giả sử bạn lấy 30% revenue)
        BigDecimal currentProfit = currentRevenue.multiply(BigDecimal.valueOf(0.3));
        BigDecimal lastProfit = lastRevenue.multiply(BigDecimal.valueOf(0.3));
        int profitDiff = calculateDiffPercentage(currentProfit, lastProfit);

        // Booking count
        long currentBooking = countBookingByMonth(currentMonth);
        long lastBooking = countBookingByMonth(lastMonth);
        int bookingDiff = calculateDiffPercentage(BigDecimal.valueOf(currentBooking), BigDecimal.valueOf(lastBooking));

        // New customers
        long currentCustomers = countNewCustomersByMonth(currentMonth);
        long lastCustomers = countNewCustomersByMonth(lastMonth);
        int customerDiff = calculateDiffPercentage(BigDecimal.valueOf(currentCustomers), BigDecimal.valueOf(lastCustomers));

        return QuickOverview.builder()
                .revenue(currentRevenue)
                .revenueDiff(revenueDiff)
                .profit(currentProfit)
                .profitDiff(profitDiff)
                .booking((int) currentBooking)
                .bookingDiff(bookingDiff)
                .newCustomer((int) currentCustomers)
                .newCustomerDiff(customerDiff)
                .build();
    }

    public BigDecimal calculateRevenueByMonth(@NotNull YearMonth month) {
        LocalDate start = month.atDay(1);
        LocalDate end = month.atEndOfMonth();

        List<Booking> bookings = bookingRepository.findByStatusAndCreateDtBetween(
                BookingStatus.CONFIRMED,
                start.atStartOfDay(ZoneId.systemDefault()),
                end.plusDays(1).atStartOfDay(ZoneId.systemDefault())
        );

        return bookings.stream()
                .map(b -> {
                    long days = ChronoUnit.DAYS.between(b.getCheckIn(), b.getCheckOut());
                    BigDecimal price = b.getPriceTracking().getPrice();
                    return price.multiply(BigDecimal.valueOf(days));
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    public long countBookingByMonth(@NotNull YearMonth month) {
        LocalDate start = month.atDay(1);
        LocalDate end = month.atEndOfMonth();

        return bookingRepository.countByStatusAndCreateDtBetween(
                BookingStatus.CONFIRMED,
                start.atStartOfDay(ZoneId.systemDefault()),
                end.plusDays(1).atStartOfDay(ZoneId.systemDefault())
        );
    }
    public long countNewCustomersByMonth(@NotNull YearMonth month) {
        LocalDate start = month.atDay(1);
        LocalDate end = month.atEndOfMonth();

        return userRepository.countByCreateDtBetween(
                start.atStartOfDay(ZoneId.systemDefault()),
                end.plusDays(1).atStartOfDay(ZoneId.systemDefault())
        );
    }
    private int calculateDiffPercentage(BigDecimal current, @NotNull BigDecimal previous) {
        if (previous.compareTo(BigDecimal.ZERO) == 0) return current.compareTo(BigDecimal.ZERO) == 0 ? 0 : 100;
        return current.subtract(previous)
                .multiply(BigDecimal.valueOf(100))
                .divide(previous, RoundingMode.HALF_UP)
                .intValue();
    }



    @Override
    public List<Integer> getAllUserFavorite() {
        User user = userRepository.getByUsername(getCurrentUsername()).orElseThrow(() -> new RuntimeException("User not found!"));
        return user.getFavoriteHotels().stream().map(Hotel::getId).collect(Collectors.toList());
    }

    @Override
    public HotelResponse getHotelById(Integer id) {
        Hotel hotel = hotelRepository.findById(id).orElseThrow(()-> new BusinessException("Hotel id not found"));
        return convertToHotelResponse(hotel);
    }

    @Scheduled(cron = "0 0 0 * * ?") // Chạy lúc 00:00 mỗi ngày
    public void sendDailyHotelReports() {
        Role role = roleRepository.findByName(ERole.ROLE_HOTEL).orElseThrow(() -> new RuntimeException("Role not found!"));
        List<User> hotelOwners = userRepository.findAllByRoles(Collections.singleton(role)).orElseThrow(()
                -> new ResourceNotFoundException("doesnt exist any hotel owner in db")); // Lấy danh sách tất cả chủ khách sạn

        for (User owner : hotelOwners) {
            List<Hotel> hotels = hotelRepository.findByOwner(owner);

            if (!hotels.isEmpty()) {
                // Tạo PDF
                byte[] pdfData = pdfGeneratorService.generateOwnerHotelReport(owner.getUsername(), hotels);

                // Đặt tên file theo ngày
                String fileName = "HotelReport_" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + ".pdf";

                // Gửi email
                emailService.sendEmailWithPdf(owner.getEmail(), "Daily Hotel Report", "Your daily hotel report is attached.", pdfData, fileName);
            }
        }
    }

}

