package savora.com.savora.controller;

import savora.com.savora.model.Review;
import savora.com.savora.model.User;
import savora.com.savora.service.ReviewService;
import savora.com.savora.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/supplier")
public class SupplierReviewsController {

    @Autowired
    private UserService userService;

    @Autowired
    private ReviewService reviewService;

    @GetMapping("/reviews")
    public String viewSupplierReviews(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User supplier = userService.findByUsername(userDetails.getUsername()).orElse(null);
        if (supplier == null || supplier.getRole() != User.Role.SUPPLIER) {
            return "redirect:/login";
        }

        // Get all reviews for products owned by this supplier
        List<Review> supplierReviews = reviewService.getAllReviews().stream()
                .filter(review -> review.getProduct().getSupplier().getId().equals(supplier.getId()))
                .toList();

        // Calculate review statistics
        long totalReviews = supplierReviews.size();
        double averageRating = supplierReviews.stream()
                .mapToDouble(Review::getRating)
                .average()
                .orElse(0.0);

        long fiveStarReviews = supplierReviews.stream().filter(r -> r.getRating() == 5).count();
        long fourStarReviews = supplierReviews.stream().filter(r -> r.getRating() == 4).count();
        long threeStarReviews = supplierReviews.stream().filter(r -> r.getRating() == 3).count();
        long twoStarReviews = supplierReviews.stream().filter(r -> r.getRating() == 2).count();
        long oneStarReviews = supplierReviews.stream().filter(r -> r.getRating() == 1).count();

        model.addAttribute("supplierReviews", supplierReviews);
        model.addAttribute("totalReviews", totalReviews);
        model.addAttribute("averageRating", String.format("%.1f", averageRating));
        model.addAttribute("fiveStarReviews", fiveStarReviews);
        model.addAttribute("fourStarReviews", fourStarReviews);
        model.addAttribute("threeStarReviews", threeStarReviews);
        model.addAttribute("twoStarReviews", twoStarReviews);
        model.addAttribute("oneStarReviews", oneStarReviews);

        return "supplier/reviews";
    }
}