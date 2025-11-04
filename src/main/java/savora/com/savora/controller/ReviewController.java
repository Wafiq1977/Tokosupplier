package savora.com.savora.controller;

import savora.com.savora.model.Review;
import savora.com.savora.model.User;
import savora.com.savora.model.Product;
import savora.com.savora.service.ReviewService;
import savora.com.savora.service.UserService;
import savora.com.savora.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.util.List;

@Controller
@RequestMapping("/reviews")
public class ReviewController {

    @Autowired
    private ReviewService reviewService;

    @Autowired
    private UserService userService;

    @Autowired
    private ProductService productService;

    @PostMapping("/add")
    public String addReview(@RequestParam Long productId,
                           @RequestParam int rating,
                           @RequestParam String comment,
                           @AuthenticationPrincipal UserDetails userDetails,
                           RedirectAttributes redirectAttributes) {
        try {
            User user = userService.findByUsername(userDetails.getUsername()).orElseThrow();
            Product product = productService.getProductById(productId).orElseThrow();

            Review review = new Review();
            review.setBuyer(user);
            review.setProduct(product);
            review.setRating(rating);
            review.setComment(comment);

            reviewService.saveReview(review);
            redirectAttributes.addFlashAttribute("successMessage", "Review berhasil ditambahkan!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Gagal menambahkan review: " + e.getMessage());
        }
        return "redirect:/products/" + productId;
    }

    @GetMapping("/my-reviews")
    public String myReviews(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User user = userService.findByUsername(userDetails.getUsername()).orElse(null);
        if (user != null) {
            List<Review> reviews = reviewService.getReviewsByBuyer(user);
            model.addAttribute("reviews", reviews);
        }
        return "my-reviews";
    }

    @GetMapping("/product/{productId}")
    public String productReviews(@PathVariable Long productId, Model model) {
        Product product = productService.getProductById(productId).orElse(null);
        if (product != null) {
            List<Review> reviews = reviewService.getReviewsByProduct(product);
            double averageRating = reviewService.getAverageRating(product);
            model.addAttribute("product", product);
            model.addAttribute("reviews", reviews);
            model.addAttribute("averageRating", averageRating);
        }
        return "product-reviews";
    }

    @PostMapping("/delete/{id}")
    public String deleteReview(@PathVariable Long id,
                              @AuthenticationPrincipal UserDetails userDetails,
                              RedirectAttributes redirectAttributes) {
        try {
            User user = userService.findByUsername(userDetails.getUsername()).orElseThrow();
            Review review = reviewService.getReviewById(id).orElseThrow();

            // Check if user owns the review
            if (!review.getBuyer().getId().equals(user.getId())) {
                redirectAttributes.addFlashAttribute("errorMessage", "Anda tidak memiliki akses untuk menghapus review ini.");
                return "redirect:/reviews/my-reviews";
            }

            reviewService.deleteReview(id);
            redirectAttributes.addFlashAttribute("successMessage", "Review berhasil dihapus!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Gagal menghapus review: " + e.getMessage());
        }
        return "redirect:/reviews/my-reviews";
    }
}