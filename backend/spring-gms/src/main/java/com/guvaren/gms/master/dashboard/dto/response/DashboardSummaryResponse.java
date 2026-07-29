package com.guvaren.gms.master.dashboard.dto.response;

import lombok.*;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardSummaryResponse {

    private SalesSummary salesSummary;
    private List<LowStockProduct> lowStockProducts;
    private List<TopSellingProduct> topSellingProducts;
    private List<RecentOrder> recentOrders;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SalesSummary {
        private BigDecimal totalRevenue;
        private Long totalOrders;
        private Long totalProducts;
        private Long totalCustomers;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LowStockProduct {
        private UUID productId;
        private String productName;
        private Integer currentStock;
        private Integer minimumStock;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TopSellingProduct {
        private UUID productId;
        private String productName;
        private Long totalSold;
        private BigDecimal revenue;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RecentOrder {
        private UUID orderId;
        private String customerName;
        private BigDecimal totalAmount;
        private String status;
        private java.time.Instant orderDate;
    }
}
