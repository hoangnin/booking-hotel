package com.lenin.hotel.hotel.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class QuickOverview {
    private BigDecimal revenue;
    private Integer revenueDiff;

    private BigDecimal profit;
    private Integer profitDiff;

    private Integer booking;
    private Integer bookingDiff;

    private Integer newCustomer;
    private Integer newCustomerDiff;
}
