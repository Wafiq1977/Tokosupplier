package savora.com.savora.controller;

import savora.com.savora.model.Order;
import savora.com.savora.model.Product;
import savora.com.savora.model.Review;
import savora.com.savora.model.User;
import savora.com.savora.service.OrderService;
import savora.com.savora.service.ProductService;
import savora.com.savora.service.ReviewService;
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
@RequestMapping("/buyer")
public class BuyerDashboardController {

    @Autowired
    private UserService userService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private ProductService productService;

    @Autowired
    private ReviewService reviewService;

    @GetMapping("/dashboard")
    public String dashboard(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User buyer = userService.findByUsername(userDetails.getUsername()).orElse(null);
        if (buyer == null) {
            return "redirect:/login";
        }

        // Get orders statistics
        List<Order> orders = orderService.getOrdersByBuyer(buyer);
        long totalOrders = orders.size();
        long completedOrders = orders.stream().filter(o -> o.getStatus() == Order.Status.DELIVERED).count();
        long pendingOrders = orders.stream().filter(o -> o.getStatus() == Order.Status.PENDING || o.getStatus() == Order.Status.PROCESSING).count();

        BigDecimal totalSpent = orders.stream()
                .filter(o -> o.getStatus() == Order.Status.DELIVERED)
                .map(Order::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Get recent orders (last 5)
        List<Order> recentOrders = orders.stream()
                .sorted((o1, o2) -> o2.getCreatedAt().compareTo(o1.getCreatedAt()))
                .limit(5)
                .toList();

        // Get recent reviews (last 5)
        List<Review> recentReviews = reviewService.getReviewsByBuyer(buyer).stream()
                .sorted((r1, r2) -> r2.getCreatedAt().compareTo(r1.getCreatedAt()))
                .limit(5)
                .toList();

        model.addAttribute("totalOrders", totalOrders);
        model.addAttribute("completedOrders", completedOrders);
        model.addAttribute("pendingOrders", pendingOrders);
        model.addAttribute("totalSpent", totalSpent);
        model.addAttribute("recentOrders", recentOrders);
        model.addAttribute("recentReviews", recentReviews);
        model.addAttribute("recentActivities", List.of()); // TODO: Implement activity tracking

        return "buyer/dashboard";
    }

    @GetMapping("/orders")
    public String orders(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User buyer = userService.findByUsername(userDetails.getUsername()).orElse(null);
        if (buyer == null) {
            return "redirect:/login";
        }

        List<Order> orders = orderService.getOrdersByBuyer(buyer);
        model.addAttribute("orders", orders);

        return "buyer/orders";
    }

}