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
            return "redirect:/login";
        }

        try {
            User user = userService.findByUsername(userDetails.getUsername()).orElse(null);
            if (user == null) {
                return "redirect:/login";
            }

            cartService.addToCart(user, productId, quantity);
            redirectAttributes.addFlashAttribute("success", "Produk berhasil ditambahkan ke keranjang");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/cart";
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
    public Long getCartCount(@AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return 0L;
        }

        User user = userService.findByUsername(userDetails.getUsername()).orElse(null);
        if (user == null) {
            return 0L;
        }

        return cartService.getCartItemCount(user);
    }
}