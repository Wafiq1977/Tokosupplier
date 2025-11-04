package savora.com.savora.controller;

import savora.com.savora.model.Order;
import savora.com.savora.model.OrderItem;
import savora.com.savora.model.Product;
import savora.com.savora.model.User;
import savora.com.savora.service.OrderService;
import savora.com.savora.service.ProductService;
import savora.com.savora.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/orders")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private ProductService productService;

    @Autowired
    private UserService userService;

    @PostMapping("/create")
    public String createOrder(@RequestParam Long productId,
                             @RequestParam int quantity,
                             @RequestParam Order.PaymentMethod paymentMethod,
                             @RequestParam Order.ShippingMethod shippingMethod,
                             @RequestParam String shippingAddress,
                             @RequestParam String shippingCity,
                             @RequestParam String shippingProvince,
                             @RequestParam String shippingPostalCode,
                             @RequestParam String shippingPhone,
                             @AuthenticationPrincipal UserDetails userDetails,
                             RedirectAttributes redirectAttributes) {
        try {
            User buyer = userService.findByUsername(userDetails.getUsername()).orElseThrow();
            Product product = productService.getProductById(productId).orElseThrow();

            OrderItem item = new OrderItem();
            item.setProduct(product);
            item.setQuantity(quantity);
            item.setPrice(product.getPrice());

            List<OrderItem> items = new ArrayList<>();
            items.add(item);

            Order order = orderService.createOrder(buyer, items);

            // Update order with payment and shipping details
            order.setPaymentMethod(paymentMethod);
            order.setShippingMethod(shippingMethod);
            order.setShippingAddress(shippingAddress);
            order.setShippingCity(shippingCity);
            order.setShippingProvince(shippingProvince);
            order.setShippingPostalCode(shippingPostalCode);
            order.setShippingPhone(shippingPhone);

            // Calculate shipping cost based on method
            switch (shippingMethod) {
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

            orderService.updateOrder(order);

            redirectAttributes.addFlashAttribute("successMessage", "Pesanan berhasil dibuat! Silakan lakukan pembayaran.");
            return "redirect:/buyer/orders";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Gagal membuat pesanan: " + e.getMessage());
            return "redirect:/products/" + productId;
        }
    }

    @GetMapping("/buyer")
    public String buyerOrders(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User buyer = userService.findByUsername(userDetails.getUsername()).orElse(null);
        if (buyer != null) {
            model.addAttribute("orders", orderService.getOrdersByBuyer(buyer));
        }
        return "buyer/orders";
    }

    @GetMapping("/supplier")
    public String supplierOrders(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User supplier = userService.findByUsername(userDetails.getUsername()).orElse(null);
        if (supplier != null) {
            model.addAttribute("orders", orderService.getOrdersBySupplier(supplier));
        }
        return "supplier/orders";
    }

    @PostMapping("/{id}/status")
    public String updateOrderStatus(@PathVariable Long id, @RequestParam Order.Status status, RedirectAttributes redirectAttributes) {
        try {
            orderService.updateOrderStatus(id, status);
            redirectAttributes.addFlashAttribute("successMessage", "Status pesanan berhasil diperbarui!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Gagal memperbarui status: " + e.getMessage());
        }
        return "redirect:/orders/supplier";
    }

    @PostMapping("/{id}/payment")
    public String updatePaymentStatus(@PathVariable Long id,
                                     @RequestParam Order.PaymentStatus paymentStatus,
                                     RedirectAttributes redirectAttributes) {
        try {
            Order order = orderService.getOrderById(id).orElseThrow();
            order.setPaymentStatus(paymentStatus);
            orderService.updateOrder(order);
            redirectAttributes.addFlashAttribute("successMessage", "Status pembayaran berhasil diperbarui!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Gagal memperbarui status pembayaran: " + e.getMessage());
        }
        return "redirect:/orders/supplier";
    }

    @PostMapping("/{id}/shipping")
    public String updateShippingInfo(@PathVariable Long id,
                                    @RequestParam String trackingNumber,
                                    @RequestParam String courierName,
                                    RedirectAttributes redirectAttributes) {
        try {
            Order order = orderService.getOrderById(id).orElseThrow();
            order.setTrackingNumber(trackingNumber);
            order.setCourierName(courierName);
            order.setStatus(Order.Status.SHIPPED);
            orderService.updateOrder(order);
            redirectAttributes.addFlashAttribute("successMessage", "Informasi pengiriman berhasil diperbarui!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Gagal memperbarui informasi pengiriman: " + e.getMessage());
        }
        return "redirect:/orders/supplier";
    }
}