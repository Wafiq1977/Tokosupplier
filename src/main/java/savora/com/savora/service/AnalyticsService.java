package savora.com.savora.service;

import savora.com.savora.model.Order;
import savora.com.savora.model.Product;
import savora.com.savora.model.Review;
import savora.com.savora.model.User;
import savora.com.savora.repository.OrderRepository;
import savora.com.savora.repository.ProductRepository;
import savora.com.savora.repository.ReviewRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
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

    @Autowired
    private ReviewRepository reviewRepository;

    @Cacheable("supplierAnalytics")
    public Map<String, Object> getSupplierAnalytics(User supplier) {
        try {
            Map<String, Object> analytics = new HashMap<>();

            // Get all orders for this supplier
            List<Order> orders = orderRepository.findBySupplier(supplier);

        // Basic metrics
        analytics.put("totalOrders", orders.size());
        analytics.put("totalRevenue", orders.stream()
                .map(Order::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add));

        // Total customers (distinct buyers)
        long totalCustomers = orders.stream()
                .map(Order::getBuyer)
                .distinct()
                .count();
        analytics.put("totalCustomers", totalCustomers);

        // Average rating for supplier's products
        List<Product> supplierProducts = productRepository.findBySupplier(supplier);
        double totalRating = 0.0;
        int totalReviews = 0;
        for (Product product : supplierProducts) {
            Double avgRating = reviewRepository.findAverageRatingByProduct(product);
            if (avgRating != null) {
                Long reviewCount = reviewRepository.countReviewsByProduct(product);
                totalRating += avgRating * reviewCount;
                totalReviews += reviewCount;
            }
        }
        double averageRating = totalReviews > 0 ? totalRating / totalReviews : 0.0;
        analytics.put("averageRating", BigDecimal.valueOf(averageRating));

        // Order status distribution
        Map<Order.Status, Long> statusDistribution = orders.stream()
                .collect(Collectors.groupingBy(Order::getStatus, Collectors.counting()));
        analytics.put("statusDistribution", statusDistribution);

        // Status distribution for chart
        List<String> statusLabels = statusDistribution.keySet().stream()
                .map(Enum::name)
                .collect(Collectors.toList());
        List<Long> statusData = statusDistribution.values().stream()
                .collect(Collectors.toList());
        // Ensure status chart has data
        if (statusLabels.isEmpty()) {
            statusLabels.add("PENDING");
            statusLabels.add("CONFIRMED");
            statusLabels.add("SHIPPED");
            statusLabels.add("DELIVERED");
            statusLabels.add("CANCELLED");
            statusData.add(0L);
            statusData.add(0L);
            statusData.add(0L);
            statusData.add(0L);
            statusData.add(0L);
        }
        analytics.put("statusLabels", statusLabels);
        analytics.put("statusData", statusData);

        // Monthly revenue for the last 12 months
        Map<String, BigDecimal> monthlyRevenue = new LinkedHashMap<>();
        List<String> chartLabels = new ArrayList<>();
        List<Double> chartData = new ArrayList<>();
        LocalDate now = LocalDate.now();
        for (int i = 11; i >= 0; i--) {
            LocalDate month = now.minusMonths(i);
            String monthKey = month.format(DateTimeFormatter.ofPattern("MMM yyyy"));
            BigDecimal revenue = orders.stream()
                    .filter(order -> order.getCreatedAt().toLocalDate().getMonth() == month.getMonth() &&
                                    order.getCreatedAt().toLocalDate().getYear() == month.getYear())
                    .map(Order::getTotalAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            monthlyRevenue.put(monthKey, revenue);
            chartLabels.add(monthKey);
            chartData.add(revenue.doubleValue());
        }
        analytics.put("monthlyRevenue", monthlyRevenue);
        // Ensure chart has data, add default if empty
        if (chartLabels.isEmpty()) {
            for (int i = 11; i >= 0; i--) {
                LocalDate month = now.minusMonths(i);
                chartLabels.add(month.format(DateTimeFormatter.ofPattern("MMM yyyy")));
                chartData.add(0.0);
            }
        }
        analytics.put("chartLabels", chartLabels);
        analytics.put("chartData", chartData);

        // Calculate growth percentages
        if (chartData.size() >= 2) {
            Double currentMonthRevenue = chartData.get(chartData.size() - 1);
            Double lastMonthRevenue = chartData.get(chartData.size() - 2);
            if (lastMonthRevenue > 0) {
                double revenueGrowth = ((currentMonthRevenue - lastMonthRevenue) / lastMonthRevenue) * 100;
                analytics.put("revenueGrowthPercent", Math.round(revenueGrowth));
            } else {
                analytics.put("revenueGrowthPercent", 0);
            }
        } else {
            analytics.put("revenueGrowthPercent", 0);
        }

        // For orders, similar logic
        long currentMonthOrders = orders.stream()
                .filter(order -> order.getCreatedAt().toLocalDate().getMonth() == LocalDate.now().getMonth() &&
                                order.getCreatedAt().toLocalDate().getYear() == LocalDate.now().getYear())
                .count();
        long lastMonthOrders = orders.stream()
                .filter(order -> order.getCreatedAt().toLocalDate().getMonth() == LocalDate.now().minusMonths(1).getMonth() &&
                                order.getCreatedAt().toLocalDate().getYear() == LocalDate.now().minusMonths(1).getYear())
                .count();
        if (lastMonthOrders > 0) {
            double orderGrowth = ((double)(currentMonthOrders - lastMonthOrders) / lastMonthOrders) * 100;
            analytics.put("orderGrowthPercent", Math.round(orderGrowth));
        } else {
            analytics.put("orderGrowthPercent", 0);
        }

        // For customers and rating, set to 0 for now
        analytics.put("customerGrowthPercent", 0);
        analytics.put("ratingGrowthPercent", 0);

        // Top products
        List<Product> products = productRepository.findBySupplier(supplier);
        List<Map<String, Object>> topProducts = new ArrayList<>();
        for (Product product : products) {
            // Count orders containing this product
            long salesCount = orders.stream()
                    .filter(order -> order.getOrderItems().stream()
                            .anyMatch(item -> item.getProduct().getId().equals(product.getId())))
                    .count();

            if (salesCount > 0) {
                // Calculate revenue from orders containing this product
                BigDecimal revenue = orders.stream()
                        .filter(order -> order.getOrderItems().stream()
                                .anyMatch(item -> item.getProduct().getId().equals(product.getId())))
                        .map(Order::getTotalAmount)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);

                // Average rating for this product
                Double avgRating = reviewRepository.findAverageRatingByProduct(product);
                double avgRatingValue = avgRating != null ? avgRating : 0.0;

                Map<String, Object> productData = new HashMap<>();
                productData.put("name", product.getName());
                productData.put("imageUrl", product.getImageUrl());
                productData.put("price", product.getPrice());
                productData.put("salesCount", salesCount);
                productData.put("revenue", revenue);
                productData.put("averageRating", BigDecimal.valueOf(avgRatingValue));
                productData.put("salesTrend", 0); // Placeholder, could calculate from previous period

                topProducts.add(productData);
            }
        }
        // Sort by salesCount descending and limit to 5
        topProducts.sort((p1, p2) -> Long.compare((Long) p2.get("salesCount"), (Long) p1.get("salesCount")));
        if (topProducts.size() > 5) {
            topProducts = topProducts.subList(0, 5);
        }
        analytics.put("topProducts", topProducts);

        // Recent orders (last 10)
        analytics.put("recentOrders", orders.stream()
                .sorted((o1, o2) -> o2.getCreatedAt().compareTo(o1.getCreatedAt()))
                .limit(10)
                .collect(Collectors.toList()));

        return analytics;
    } catch (Exception e) {
        // Log error and return empty analytics
        System.err.println("Error generating supplier analytics: " + e.getMessage());
        e.printStackTrace();
        Map<String, Object> emptyAnalytics = new HashMap<>();
        emptyAnalytics.put("totalOrders", 0);
        emptyAnalytics.put("totalRevenue", BigDecimal.ZERO);
        emptyAnalytics.put("totalCustomers", 0);
        emptyAnalytics.put("averageRating", BigDecimal.ZERO);
        emptyAnalytics.put("statusDistribution", new HashMap<>());
        emptyAnalytics.put("monthlyRevenue", new LinkedHashMap<>());
        emptyAnalytics.put("topProducts", new ArrayList<>());
        emptyAnalytics.put("recentOrders", new ArrayList<>());
        emptyAnalytics.put("chartLabels", new ArrayList<>());
        emptyAnalytics.put("chartData", new ArrayList<>());
        emptyAnalytics.put("statusLabels", new ArrayList<>());
        emptyAnalytics.put("statusData", new ArrayList<>());
        return emptyAnalytics;
    }
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