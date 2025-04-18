package com.lenin.hotel.common.service;

import com.lenin.hotel.hotel.model.Hotel;
import org.jfree.chart.*;
import org.jfree.chart.axis.*;
import org.jfree.chart.block.BlockBorder;
import org.jfree.chart.encoders.EncoderUtil;
import org.jfree.chart.encoders.ImageFormat;
import org.jfree.chart.plot.*;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.LineAndShapeRenderer;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.List;

public class ChartGenerator {

    public static byte[] createRevenueChart(List<Hotel> hotels) {
        DefaultCategoryDataset revenueDataset = new DefaultCategoryDataset();
        DefaultCategoryDataset bookingDataset = new DefaultCategoryDataset();

        for (Hotel hotel : hotels) {
            double totalRevenue = hotel.getBookings().stream()
                    .mapToDouble(b -> b.getPriceTracking().getPrice().doubleValue())
                    .sum();
            int totalBookings = hotel.getBookings().size();
            double avgRevenue = totalRevenue / (totalBookings > 0 ? totalBookings : 1);

            revenueDataset.addValue(totalRevenue, "Total Revenue", hotel.getName());
            bookingDataset.addValue(totalBookings, "Total Bookings", hotel.getName());
        }

        // Biểu đồ chính (Revenue - BarChart)
        JFreeChart chart = ChartFactory.createBarChart(
                "Hotel Performance Overview", "Hotels", "Revenue ($)",
                revenueDataset, PlotOrientation.VERTICAL, true, true, false
        );

        CategoryPlot plot = chart.getCategoryPlot();
        BarRenderer barRenderer = new BarRenderer();
        barRenderer.setSeriesPaint(0, new Color(79, 129, 189)); // Màu xanh dương
        plot.setRenderer(barRenderer);

        // Trục phụ cho Booking (Line Chart)
        NumberAxis bookingAxis = new NumberAxis("Total Bookings");
        bookingAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
        plot.setRangeAxis(1, bookingAxis);
        plot.setDataset(1, bookingDataset);
        plot.mapDatasetToRangeAxis(1, 1);

        LineAndShapeRenderer lineRenderer = new LineAndShapeRenderer();
        lineRenderer.setSeriesPaint(0, Color.RED);
        lineRenderer.setSeriesStroke(0, new BasicStroke(2.0f));
        plot.setRenderer(1, lineRenderer);

        // Tùy chỉnh giao diện
        plot.setBackgroundPaint(Color.WHITE);
        plot.setDomainGridlinePaint(Color.LIGHT_GRAY);
        plot.setRangeGridlinePaint(Color.LIGHT_GRAY);
        chart.getLegend().setFrame(BlockBorder.NONE);
        chart.getTitle().setFont(new Font("Arial", Font.BOLD, 16));

        BufferedImage bufferedImage = chart.createBufferedImage(700, 500);
        try {
            return EncoderUtil.encode(bufferedImage, ImageFormat.PNG);
        } catch (IOException e) {
            throw new RuntimeException("Error generating chart image", e);
        }
    }
}
