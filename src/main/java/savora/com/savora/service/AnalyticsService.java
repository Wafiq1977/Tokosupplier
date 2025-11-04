package savora.com.savora.service;

import savora.com.savora.model.Order;
import savora.com.savora.model.Product;
import savora.com.savora.model.User;
import savora.com.savora.repository.OrderRepository;
import savora.com.savora.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AnalyticsService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ProductRepository productRepository;

    public Map<String, Object> getSupplierAnalytics(User supplier) {
        Map<String, Object> analytics = new HashMap<>();

        // Get all orders for this supplier
        List<Order> orders = orderRepository.findBySupplier(supplier);

        // Basic metrics
        analytics.put("totalOrders", orders.size());
        analytics.put("totalRevenue", orders.stream()
                .map(Order::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add));

        // Order status distribution
        Map<Order.Status, Long> statusDistribution = orders.stream()
                .collect(Collectors.groupingBy(Order::getStatus, Collectors.counting()));
        analytics.put("statusDistribution", statusDistribution);

        // Monthly revenue for the last 12 months
        Map<String, BigDecimal> monthlyRevenue = new LinkedHashMap<>();
        LocalDate now = LocalDate.now();
        for (int i = 11; i >= 0; i--) {
            LocalDate month = now.minusMonths(i);
            String monthKey = month.format(DateTimeFormatter.ofPattern("MMM yyyy"));
            BigDecimal revenue = orders.stream()
                    .filter(order -> order.getCreatedAt().getMonth() == month.getMonth() &&
                                    order.getCreatedAt().getYear() == month.getYear())
                    .map(Order::getTotalAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            monthlyRevenue.put(monthKey, revenue);
        }
        analytics.put("monthlyRevenue", monthlyRevenue);

        // Top products
        List<Product> products = productRepository.findBySupplier(supplier);
        Map<String, Long> productOrderCount = new HashMap<>();
        for (Product product : products) {
            long orderCount = orders.stream()
                    .mapToLong(order -> order.getOrderItems().stream()
                            .mapToLong(item -> item.getProduct().getId().equals(product.getId()) ? item.getQuantity() : 0)
                            .sum())
                    .sum();
            if (orderCount > 0) {
                productOrderCount.put(product.getName(), orderCount);
            }
        }
        analytics.put("topProducts", productOrderCount.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(5)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue,
                        (e1, e2) -> e1, LinkedHashMap::new)));

        // Recent orders (last 10)
        analytics.put("recentOrders", orders.stream()
                .sorted((o1, o2) -> o2.getCreatedAt().compareTo(o1.getCreatedAt()))
                .limit(10)
                .collect(Collectors.toList()));

        return analytics;
    }

    public Map<String, Object> getBuyerAnalytics(User buyer) {
        Map<String, Object> analytics = new HashMap<>();

        // Get all orders for this buyer
        List<Order> orders = orderRepository.findByBuyer(buyer);

        // Basic metrics
        analytics.put("totalOrders", orders.size());
        analytics.put("totalSpent", orders.stream()
                .map(Order::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add));

        // Average order value
        if (!orders.isEmpty()) {
            analytics.put("averageOrderValue",
                    orders.stream()
                            .map(Order::getTotalAmount)
                            .reduce(BigDecimal.ZERO, BigDecimal::add)
                            .divide(BigDecimal.valueOf(orders.size()), 2, BigDecimal.ROUND_HALF_UP));
        } else {
            analytics.put("averageOrderValue", BigDecimal.ZERO);
        }

        // Order status distribution
        Map<Order.Status, Long> statusDistribution = orders.stream()
                .collect(Collectors.groupingBy(Order::getStatus, Collectors.counting()));
        analytics.put("statusDistribution", statusDistribution);

        // Monthly spending for the last 12 months
        Map<String, BigDecimal> monthlySpending = new LinkedHashMap<>();
        LocalDate now = LocalDate.now();
        for (int i = 11; i >= 0; i--) {
            LocalDate month = now.minusMonths(i);
            String monthKey = month.format(DateTimeFormatter.ofPattern("MMM yyyy"));
            BigDecimal spending = orders.stream()
                    .filter(order -> order.getCreatedAt().getMonth() == month.getMonth() &&
                                    order.getCreatedAt().getYear() == month.getYear())
                    .map(Order::getTotalAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            monthlySpending.put(monthKey, spending);
        }
        analytics.put("monthlySpending", monthlySpending);

        // Favorite suppliers
        Map<String, Long> supplierOrderCount = orders.stream()
                .collect(Collectors.groupingBy(order -> order.getSupplier().getCompanyName(), Collectors.counting()));
        analytics.put("favoriteSuppliers", supplierOrderCount.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(5)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue,
                        (e1, e2) -> e1, LinkedHashMap::new)));

        // Recent orders (last 10)
        analytics.put("recentOrders", orders.stream()
                .sorted((o1, o2) -> o2.getCreatedAt().compareTo(o1.getCreatedAt()))
                .limit(10)
                .collect(Collectors.toList()));

        return analytics;
    }
}