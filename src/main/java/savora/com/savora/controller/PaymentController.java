package savora.com.savora.controller;

import savora.com.savora.model.Cart;
import savora.com.savora.model.Order;
import savora.com.savora.model.OrderItem;
import savora.com.savora.model.User;
import savora.com.savora.service.CartService;
import savora.com.savora.service.OrderService;
import savora.com.savora.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/payment")
public class PaymentController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private UserService userService;

    @Autowired
    private CartService cartService;



    @PostMapping("/create-order")
    public String createOrder(@RequestParam String paymentMethod,
                             @RequestParam String shippingMethod,
                             @RequestParam String shippingAddress,
                             @RequestParam String shippingCity,
                             @RequestParam String shippingProvince,
                             @RequestParam String shippingPostalCode,
                             @RequestParam String shippingPhone,
                             @RequestParam(required = false) String voucherCode,
                             @RequestParam(required = false) String bankSelection,
                             @AuthenticationPrincipal UserDetails userDetails,
                             RedirectAttributes redirectAttributes) {
        try {
            User buyer = userService.findByUsername(userDetails.getUsername()).orElseThrow();

            // Get cart items
            List<Cart> cartItems = cartService.getCartItems(buyer);
            if (cartItems.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Keranjang Anda kosong!");
                return "redirect:/cart";
            }

            // Create order items from cart
            List<OrderItem> orderItems = new java.util.ArrayList<>();
            for (Cart cart : cartItems) {
                OrderItem item = new OrderItem();
                item.setProduct(cart.getProduct());
                item.setQuantity(cart.getQuantity());
                item.setPrice(cart.getUnitPrice());
                orderItems.add(item);
            }

            // Create order
            Order order = orderService.createOrder(buyer, orderItems);

            // Update order with payment and shipping details
            order.setPaymentMethod(Order.PaymentMethod.valueOf(paymentMethod));
            order.setShippingMethod(Order.ShippingMethod.valueOf(shippingMethod));
            order.setShippingAddress(shippingAddress);
            order.setShippingCity(shippingCity);
            order.setShippingProvince(shippingProvince);
            order.setShippingPostalCode(shippingPostalCode);
            order.setShippingPhone(shippingPhone);

            // Calculate shipping cost based on method
            Order.ShippingMethod shippingMethodEnum = Order.ShippingMethod.valueOf(shippingMethod);
            switch (shippingMethodEnum) {
                case INSTANT:
                    order.setShippingCost(java.math.BigDecimal.valueOf(25000));
                    break;
                case SAME_DAY:
                    order.setShippingCost(java.math.BigDecimal.valueOf(15000));
                    break;
                case EXPRESS:
                    order.setShippingCost(java.math.BigDecimal.valueOf(10000));
                    break;
                case REGULAR:
                default:
                    order.setShippingCost(java.math.BigDecimal.valueOf(5000));
                    break;
            }

            // Update total amount including shipping
            order.setTotalAmount(order.getSubtotalAmount().add(order.getShippingCost()));

            // Apply voucher discount if provided (placeholder for future implementation)
            if (voucherCode != null && !voucherCode.trim().isEmpty()) {
                // TODO: Implement voucher validation and discount calculation
                redirectAttributes.addFlashAttribute("info", "Kode voucher '" + voucherCode + "' akan divalidasi setelah pembayaran.");
            }

            orderService.updateOrder(order);

            // Clear cart after successful order creation
            cartService.clearCart(buyer);

            redirectAttributes.addFlashAttribute("successMessage", "Pesanan berhasil dibuat! Silakan lakukan pembayaran.");
            redirectAttributes.addAttribute("highlight", order.getId());
            redirectAttributes.addAttribute("showDetail", order.getId());

            // Redirect to buyer orders page with success message and show order details
            return "redirect:/buyer/orders";

        } catch (Exception e) {
            e.printStackTrace(); // Add this to see the actual error
            String errorMsg = e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName();
            System.err.println("Error creating order: " + errorMsg);
            e.printStackTrace(System.err);
            redirectAttributes.addFlashAttribute("errorMessage", "Gagal membuat pesanan: " + errorMsg);
            return "redirect:/buyer/orders";
        }
    }
}