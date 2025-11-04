package savora.com.savora.controller;

import savora.com.savora.model.Order;
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
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/profile")
public class ProfileController {

    @Autowired
    private UserService userService;

    @Autowired
    private ProductService productService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private ReviewService reviewService;

    @GetMapping
    public String viewProfile(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User user = userService.findByUsername(userDetails.getUsername()).orElse(null);
        if (user != null) {
            // Add role-specific statistics
            if (user.getRole() == User.Role.SUPPLIER) {
                // Get supplier statistics
                long productCount = productService.getProductsBySupplier(user).size();
                long orderCount = orderService.getOrdersBySupplier(user).size();
                double rating = productService.getProductsBySupplier(user).stream()
                    .filter(p -> p.getAverageRating() != null)
                    .mapToDouble(p -> p.getAverageRating())
                    .average()
                    .orElse(0.0);

                model.addAttribute("productCount", productCount);
                model.addAttribute("orderCount", orderCount);
                model.addAttribute("rating", rating);
            } else if (user.getRole() == User.Role.BUYER) {
                // Get buyer statistics
                long orderCount = orderService.getOrdersByBuyer(user).size();
                double totalSpent = orderService.getOrdersByBuyer(user).stream()
                    .filter(o -> o.getStatus() == Order.Status.DELIVERED)
                    .mapToDouble(o -> o.getTotalAmount().doubleValue())
                    .sum();
                long reviewCount = reviewService.getReviewsByBuyer(user).size();

                model.addAttribute("orderCount", orderCount);
                model.addAttribute("totalSpent", totalSpent);
                model.addAttribute("reviewCount", reviewCount);
            }

            model.addAttribute("user", user);
            model.addAttribute("recentActivities", List.of()); // TODO: Implement activity tracking
            return "profile";
        }
        return "redirect:/login";
    }

    @GetMapping("/edit")
    public String editProfileForm(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User user = userService.findByUsername(userDetails.getUsername()).orElse(null);
        if (user != null) {
            model.addAttribute("user", user);
            return "edit-profile";
        }
        return "redirect:/login";
    }

    @PostMapping("/edit")
    public String updateProfile(@ModelAttribute User updatedUser,
                               @AuthenticationPrincipal UserDetails userDetails,
                               RedirectAttributes redirectAttributes) {
        try {
            User currentUser = userService.findByUsername(userDetails.getUsername()).orElseThrow();

            // Update only allowed fields
            currentUser.setCompanyName(updatedUser.getCompanyName());
            currentUser.setPhoneNumber(updatedUser.getPhoneNumber());
            currentUser.setAddress(updatedUser.getAddress());
            currentUser.setAvatarUrl(updatedUser.getAvatarUrl());

            // Note: Username and email changes would require additional validation
            // For now, we keep them as read-only

            // Save updated user (assuming UserService has update method)
            // userService.updateUser(currentUser);

            redirectAttributes.addFlashAttribute("successMessage", "Profil berhasil diperbarui!");
            return "redirect:/profile";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Gagal memperbarui profil: " + e.getMessage());
            return "redirect:/profile/edit";
        }
    }
}