package savora.com.savora.controller;

import savora.com.savora.model.Order;
import savora.com.savora.model.Product;
import savora.com.savora.model.Review;
import savora.com.savora.model.User;
import savora.com.savora.service.OrderService;
import savora.com.savora.service.ProductService;
import savora.com.savora.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.math.BigDecimal;
import java.util.List;

@Controller
@RequestMapping("/supplier")
public class SupplierDashboardController {

    @Autowired
    private UserService userService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private ProductService productService;

    @GetMapping("/dashboard")
    public String dashboard(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User supplier = userService.findByUsername(userDetails.getUsername()).orElse(null);
        if (supplier == null) {
            return "redirect:/login";
        }

        // Get products statistics
        List<Product> products = productService.getProductsBySupplier(supplier);
        long totalProducts = products.size();

        // Get orders statistics
        List<Order> orders = orderService.getOrdersBySupplier(supplier);
        long totalOrders = orders.size();

        BigDecimal totalRevenue = orders.stream()
                .filter(o -> o.getStatus() == Order.Status.DELIVERED)
                .map(Order::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        long pendingOrdersCount = orders.stream()
                .filter(o -> o.getStatus() == Order.Status.PENDING || o.getStatus() == Order.Status.PROCESSING)
                .count();

        // Calculate average rating
        double averageRating = products.stream()
                .filter(p -> p.getAverageRating() != null)
                .mapToDouble(Product::getAverageRating)
                .average()
                .orElse(0.0);

        // Get top products (by sales count)
        List<Product> topProducts = products.stream()
                .filter(p -> p.getSalesCount() != null && p.getSalesCount() > 0)
                .sorted((p1, p2) -> Integer.compare(p2.getSalesCount(), p1.getSalesCount()))
                .limit(5)
                .toList();

        // Get low stock products
        List<Product> lowStockProducts = products.stream()
                .filter(p -> p.getStock() != null && p.getStock() <= 10)
                .limit(5)
                .toList();

        // Get recent orders (last 5)
        List<Order> recentOrders = orders.stream()
                .sorted((o1, o2) -> o2.getCreatedAt().compareTo(o1.getCreatedAt()))
                .limit(5)
                .toList();

        model.addAttribute("totalProducts", totalProducts);
        model.addAttribute("totalOrders", totalOrders);
        model.addAttribute("totalRevenue", totalRevenue);
        model.addAttribute("averageRating", String.format("%.1f", averageRating));
        model.addAttribute("pendingOrdersCount", (int) pendingOrdersCount);
        model.addAttribute("topProducts", topProducts);
        model.addAttribute("lowStockProducts", lowStockProducts);
        model.addAttribute("recentOrders", recentOrders);

        return "supplier/dashboard";
    }


}