package savora.com.savora.controller;

import savora.com.savora.model.Product;
import savora.com.savora.model.User;
import savora.com.savora.service.ProductService;
import savora.com.savora.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/buyer")
public class BuyerFavoritesController {

    @Autowired
    private UserService userService;

    @Autowired
    private ProductService productService;

    @GetMapping("/favorites")
    public String viewFavorites(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User buyer = userService.findByUsername(userDetails.getUsername()).orElse(null);
        if (buyer == null) {
            return "redirect:/login";
        }

        // For now, show all products as "favorites" - in real implementation, you'd have a favorites relationship
        List<Product> favoriteProducts = productService.getAllProducts().subList(0, Math.min(12, productService.getAllProducts().size()));

        model.addAttribute("favoriteProducts", favoriteProducts);
        model.addAttribute("totalFavorites", favoriteProducts.size());

        return "buyer/favorites";
    }

    @PostMapping("/favorites/add/{productId}")
    public String addToFavorites(@PathVariable Long productId,
                               @AuthenticationPrincipal UserDetails userDetails,
                               RedirectAttributes redirectAttributes) {
        try {
            User buyer = userService.findByUsername(userDetails.getUsername()).orElseThrow();
            Product product = productService.getProductById(productId).orElseThrow();

            // In real implementation, you'd add to favorites relationship
            redirectAttributes.addFlashAttribute("successMessage", "Produk berhasil ditambahkan ke favorit!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Gagal menambahkan ke favorit: " + e.getMessage());
        }

        return "redirect:/products/" + productId;
    }

    @PostMapping("/favorites/remove/{productId}")
    public String removeFromFavorites(@PathVariable Long productId,
                                    @AuthenticationPrincipal UserDetails userDetails,
                                    RedirectAttributes redirectAttributes) {
        try {
            User buyer = userService.findByUsername(userDetails.getUsername()).orElseThrow();
            Product product = productService.getProductById(productId).orElseThrow();

            // In real implementation, you'd remove from favorites relationship
            redirectAttributes.addFlashAttribute("successMessage", "Produk berhasil dihapus dari favorit!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Gagal menghapus dari favorit: " + e.getMessage());
        }

        return "redirect:/buyer/favorites";
    }

    @PostMapping("/favorites/toggle/{productId}")
    public org.springframework.http.ResponseEntity<?> toggleFavorite(@PathVariable Long productId,
                                                                   @AuthenticationPrincipal UserDetails userDetails) {
        try {
            User buyer = userService.findByUsername(userDetails.getUsername()).orElseThrow();
            Product product = productService.getProductById(productId).orElseThrow();

            // In real implementation, you'd toggle favorites relationship
            // For now, just return success
            return org.springframework.http.ResponseEntity.ok(java.util.Map.of("status", "OK", "message", "Favorite toggled"));
        } catch (Exception e) {
            return org.springframework.http.ResponseEntity.badRequest().body(java.util.Map.of("error", e.getMessage()));
        }
    }
}