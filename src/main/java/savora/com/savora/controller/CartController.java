package savora.com.savora.controller;

import savora.com.savora.model.Cart;
import savora.com.savora.model.User;
import savora.com.savora.service.CartService;
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
@RequestMapping("/cart")
public class CartController {

    @Autowired
    private CartService cartService;

    @Autowired
    private UserService userService;

    @GetMapping
    public String viewCart(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        if (userDetails == null) {
            return "redirect:/login";
        }

        User user = userService.findByUsername(userDetails.getUsername()).orElse(null);
        if (user == null) {
            return "redirect:/login";
        }

        List<Cart> cartItems = cartService.getCartItems(user);
        Long totalQuantity = cartService.getTotalQuantity(user);
        java.math.BigDecimal totalPrice = cartService.getTotalPrice(user);

        model.addAttribute("cartItems", cartItems);
        model.addAttribute("totalQuantity", totalQuantity);
        model.addAttribute("totalPrice", totalPrice);

        return "cart";
    }

    @PostMapping("/add")
    public String addToCart(@RequestParam Long productId,
                           @RequestParam(defaultValue = "1") Integer quantity,
                           @AuthenticationPrincipal UserDetails userDetails,
                           RedirectAttributes redirectAttributes) {
        if (userDetails == null) {
            redirectAttributes.addFlashAttribute("error", "Silakan login terlebih dahulu");
            return "redirect:/login";
        }

        try {
            User user = userService.findByUsername(userDetails.getUsername()).orElse(null);
            if (user == null) {
                redirectAttributes.addFlashAttribute("error", "User tidak ditemukan");
                return "redirect:/login";
            }
            
            // Check if user is a buyer
            if (user.getRole() != User.Role.BUYER) {
                redirectAttributes.addFlashAttribute("error", "Hanya pembeli yang dapat menambahkan produk ke keranjang");
                return "redirect:/";
            }

            cartService.addToCart(user, productId, quantity);
            redirectAttributes.addFlashAttribute("success", "Produk berhasil ditambahkan ke keranjang");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/cart";
    }

    // REST endpoint for fetch() from homepage (no redirect)
    @PostMapping(value = "/add", params = {"productId", "quantity"}, headers = "Content-Type=application/x-www-form-urlencoded")
    @ResponseBody
    public org.springframework.http.ResponseEntity<?> addToCartApi(@RequestParam Long productId,
                                                                   @RequestParam(defaultValue = "1") Integer quantity,
                                                                   @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return org.springframework.http.ResponseEntity.status(401).body(java.util.Map.of("error", "Unauthorized"));
        }
        User user = userService.findByUsername(userDetails.getUsername()).orElse(null);
        if (user == null) {
            return org.springframework.http.ResponseEntity.status(401).body(java.util.Map.of("error", "Unauthorized"));
        }
        
        // Check if user is a buyer
        if (user.getRole() != User.Role.BUYER) {
            return org.springframework.http.ResponseEntity.status(403).body(java.util.Map.of("error", "Only buyers can add to cart"));
        }
        
        try {
            cartService.addToCart(user, productId, quantity);
            return org.springframework.http.ResponseEntity.ok(java.util.Map.of("status", "OK", "message", "Product added to cart"));
        } catch (Exception e) {
            return org.springframework.http.ResponseEntity.badRequest().body(java.util.Map.of("error", e.getMessage()));
        }
    }

    // Buy now endpoint - adds to cart and returns cart URL
    @PostMapping("/buy-now")
    @ResponseBody
    public org.springframework.http.ResponseEntity<?> buyNow(@RequestParam Long productId,
                                                             @RequestParam(defaultValue = "1") Integer quantity,
                                                             @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return org.springframework.http.ResponseEntity.status(401).body(java.util.Map.of("error", "Please login first", "redirect", "/login"));
        }
        
        User user = userService.findByUsername(userDetails.getUsername()).orElse(null);
        if (user == null) {
            return org.springframework.http.ResponseEntity.status(401).body(java.util.Map.of("error", "User not found", "redirect", "/login"));
        }
        
        // Check if user is a buyer
        if (user.getRole() != User.Role.BUYER) {
            return org.springframework.http.ResponseEntity.status(403).body(java.util.Map.of("error", "Only buyers can purchase products"));
        }
        
        try {
            cartService.addToCart(user, productId, quantity);
            return org.springframework.http.ResponseEntity.ok(java.util.Map.of("status", "OK", "redirect", "/cart"));
        } catch (Exception e) {
            return org.springframework.http.ResponseEntity.badRequest().body(java.util.Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/update")
    public String updateQuantity(@RequestParam Long productId,
                                 @RequestParam Integer quantity,
                                 @AuthenticationPrincipal UserDetails userDetails,
                                 RedirectAttributes redirectAttributes) {
        if (userDetails == null) {
            return "redirect:/login";
        }

        try {
            User user = userService.findByUsername(userDetails.getUsername()).orElse(null);
            if (user == null) {
                return "redirect:/login";
            }

            cartService.updateQuantity(user, productId, quantity);
            redirectAttributes.addFlashAttribute("success", "Jumlah produk berhasil diperbarui");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/cart";
    }

    @PostMapping("/update-ajax")
    @ResponseBody
    public org.springframework.http.ResponseEntity<?> updateQuantityAjax(@RequestParam Long productId,
                                                                        @RequestParam Integer quantity,
                                                                        @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return org.springframework.http.ResponseEntity.status(401).body(java.util.Map.of("error", "Unauthorized"));
        }

        try {
            User user = userService.findByUsername(userDetails.getUsername()).orElse(null);
            if (user == null) {
                return org.springframework.http.ResponseEntity.status(401).body(java.util.Map.of("error", "User not found"));
            }

            cartService.updateQuantity(user, productId, quantity);

            // Get updated cart totals
            Long totalQuantity = cartService.getTotalQuantity(user);
            java.math.BigDecimal totalPrice = cartService.getTotalPrice(user);

            return org.springframework.http.ResponseEntity.ok(java.util.Map.of(
                "status", "OK",
                "message", "Quantity updated successfully",
                "totalQuantity", totalQuantity,
                "totalPrice", totalPrice
            ));
        } catch (Exception e) {
            return org.springframework.http.ResponseEntity.badRequest().body(java.util.Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/remove")
    public String removeFromCart(@RequestParam Long productId,
                                @AuthenticationPrincipal UserDetails userDetails,
                                RedirectAttributes redirectAttributes) {
        if (userDetails == null) {
            return "redirect:/login";
        }

        try {
            User user = userService.findByUsername(userDetails.getUsername()).orElse(null);
            if (user == null) {
                return "redirect:/login";
            }

            cartService.removeFromCart(user, productId);
            redirectAttributes.addFlashAttribute("success", "Produk berhasil dihapus dari keranjang");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/cart";
    }


    @PostMapping("/clear")
    public String clearCart(@AuthenticationPrincipal UserDetails userDetails,
                           RedirectAttributes redirectAttributes) {
        if (userDetails == null) {
            return "redirect:/login";
        }

        try {
            User user = userService.findByUsername(userDetails.getUsername()).orElse(null);
            if (user == null) {
                return "redirect:/login";
            }

            cartService.clearCart(user);
            redirectAttributes.addFlashAttribute("success", "Keranjang berhasil dikosongkan");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/cart";
    }

    @GetMapping("/count")
    @ResponseBody
    public java.util.Map<String, Long> getCartCount(@AuthenticationPrincipal UserDetails userDetails) {
        long count = 0L;
        if (userDetails != null) {
            User user = userService.findByUsername(userDetails.getUsername()).orElse(null);
            if (user != null) {
                count = cartService.getCartItemCount(user);
            }
        }
        return java.util.Map.of("count", count);
    }

}
