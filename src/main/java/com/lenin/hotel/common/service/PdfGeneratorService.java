package com.lenin.hotel.common.service;


import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.UnitValue;
import com.lenin.hotel.hotel.model.Hotel;
import com.lenin.hotel.hotel.model.Booking;
import org.springframework.stereotype.Service;
import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class PdfGeneratorService {

    // 🏨 Báo cáo dành cho chủ khách sạn (Không có biểu đồ)
    public byte[] generateOwnerHotelReport(String ownerName, List<Hotel> hotels) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try (PdfWriter writer = new PdfWriter(outputStream);
             PdfDocument pdfDoc = new PdfDocument(writer);
             Document document = new Document(pdfDoc)) {

            document.add(new Paragraph("Hotel Report for: " + ownerName)
                    .setBold().setFontSize(16));
            document.add(new Paragraph("\n"));

            double[] totalRevenueAllHotels = {0.0}; // Dùng mảng để giữ giá trị cập nhật

            for (Hotel hotel : hotels) {
                document.add(new Paragraph("Hotel: " + hotel.getName())
                        .setBold().setFontSize(14));
                document.add(new Paragraph("Location: " + hotel.getLocation().getName()));
                document.add(new Paragraph("\n"));

                Table table = createBookingTable(hotel, totalRevenueAllHotels);
                document.add(table);
                document.add(new Paragraph("\n"));
            }

            document.add(new Paragraph("Total Revenue for All Hotels: " + String.format("$%.2f", totalRevenueAllHotels[0]))
                    .setBold().setFontSize(14));


        } catch (Exception e) {
            throw new RuntimeException("Error while generating PDF", e);
        }
        return outputStream.toByteArray();
    }

    // 📊 Báo cáo dành cho Admin (Có biểu đồ)
    public byte[] generateAdminHotelReport(List<Hotel> hotels) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try (PdfWriter writer = new PdfWriter(outputStream);
             PdfDocument pdfDoc = new PdfDocument(writer);
             Document document = new Document(pdfDoc)) {

            document.add(new Paragraph("Admin Hotel Revenue Report")
                    .setBold().setFontSize(16));
            document.add(new Paragraph("\n"));

            double[] totalRevenueAllHotels = {0.0}; // Dùng mảng để giữ giá trị cập nhật

            for (Hotel hotel : hotels) {
                document.add(new Paragraph("Hotel: " + hotel.getName())
                        .setBold().setFontSize(14));
                document.add(new Paragraph("Location: " + hotel.getLocation().getName()));
                document.add(new Paragraph("\n"));

                Table table = createBookingTable(hotel, totalRevenueAllHotels);
                document.add(table);
                document.add(new Paragraph("\n"));
            }

            document.add(new Paragraph("Total Revenue for All Hotels: " + String.format("$%.2f", totalRevenueAllHotels[0]))
                    .setBold().setFontSize(14));


            // 🛠️ Thêm biểu đồ doanh thu
            byte[] chartImage = ChartGenerator.createRevenueChart(hotels);
            Image img = new Image(com.itextpdf.io.image.ImageDataFactory.create(chartImage));
            document.add(img);

        } catch (Exception e) {
            throw new RuntimeException("Error while generating PDF", e);
        }
        return outputStream.toByteArray();
    }

    private Table createBookingTable(Hotel hotel, double[] totalHotelRevenue) {
        Table table = new Table(UnitValue.createPercentArray(new float[]{2, 2, 2, 2, 2})).useAllAvailableWidth();
        table.addHeaderCell("Booking ID");
        table.addHeaderCell("Customer Name");
        table.addHeaderCell("Check-in");
        table.addHeaderCell("Check-out");
        table.addHeaderCell("Price");

        double revenue = 0.0;

        for (Booking booking : hotel.getBookings()) {
            table.addCell(String.valueOf(booking.getId()));
            table.addCell(booking.getUser().getUsername());
            table.addCell(booking.getCheckIn().toString());
            table.addCell(booking.getCheckOut().toString());
            table.addCell(String.format("$%.2f", booking.getPriceTracking().getPrice()));

            revenue += booking.getPriceTracking().getPrice().doubleValue();
        }

        table.addCell(new Cell(1, 4).add(new Paragraph("Total Revenue:")).setBold());
        table.addCell(new Cell().add(new Paragraph(String.format("$%.2f", revenue))).setBold());

        totalHotelRevenue[0] += revenue; // Cập nhật tổng doanh thu
        return table;
    }


    public String generatePdfFileName() {
        LocalDate today = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        return "HotelReport_" + today.format(formatter) + ".pdf";
    }
}
