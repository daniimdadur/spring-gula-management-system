package com.guvaren.gms.dashboard.service;

import com.guvaren.gms.dashboard.dto.response.DashboardSummaryResponse;
import com.guvaren.gms.inventory.entity.Inventory;
import com.guvaren.gms.inventory.repository.InventoryRepository;
import com.guvaren.gms.order.entity.Order;
import com.guvaren.gms.order.entity.OrderStatus;
import com.guvaren.gms.order.repository.OrderRepository;
import com.guvaren.gms.product.repository.ProductRepository;
import com.guvaren.gms.customer.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final ProductRepository productRepository;
    private final InventoryRepository inventoryRepository;
    private final OrderRepository orderRepository;
    private final CustomerRepository customerRepository;

    @Transactional(readOnly = true)
    @Cacheable(value = "dashboard_summary", unless = "#result == null")
    public DashboardSummaryResponse getDashboardSummary() {
        long totalProducts = productRepository.count();
        long totalCustomers = customerRepository.count();

        List<Order> allOrders = orderRepository.findAll();
        long totalOrders = allOrders.size();
        BigDecimal totalRevenue = allOrders.stream()
                .filter(o -> o.getStatus() == OrderStatus.COMPLETED || o.getStatus() == OrderStatus.PAID)
                .map(Order::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        List<Inventory> allInventories = inventoryRepository.findAll();
        List<DashboardSummaryResponse.LowStockProduct> lowStockProducts = allInventories.stream()
                .filter(inv -> inv.getCurrentStock() < inv.getMinimumStock())
                .map(inv -> DashboardSummaryResponse.LowStockProduct.builder()
                        .productId(inv.getProduct().getId())
                        .productName(inv.getProduct().getName())
                        .currentStock(inv.getCurrentStock())
                        .minimumStock(inv.getMinimumStock())
                        .build())
                .toList();

        List<DashboardSummaryResponse.RecentOrder> recentOrders = allOrders.stream()
                .sorted((a, b) -> b.getOrderDate().compareTo(a.getOrderDate()))
                .limit(10)
                .map(order -> DashboardSummaryResponse.RecentOrder.builder()
                        .orderId(order.getId())
                        .customerName(order.getCustomer().getName())
                        .totalAmount(order.getTotalAmount())
                        .status(order.getStatus().name())
                        .orderDate(order.getOrderDate())
                        .build())
                .toList();

        return DashboardSummaryResponse.builder()
                .salesSummary(DashboardSummaryResponse.SalesSummary.builder()
                        .totalRevenue(totalRevenue)
                        .totalOrders(totalOrders)
                        .totalProducts(totalProducts)
                        .totalCustomers(totalCustomers)
                        .build())
                .lowStockProducts(lowStockProducts)
                .topSellingProducts(List.of())
                .recentOrders(recentOrders)
                .build();
    }

    @CacheEvict(value = "dashboard_summary")
    public void evictDashboardCache() {
    }
}
