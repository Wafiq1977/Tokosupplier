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
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/supplier")
public class SupplierInventoryController {

    @Autowired
    private UserService userService;

    @Autowired
    private ProductService productService;

    @GetMapping("/inventory")
    public String viewInventory(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User supplier = userService.findByUsername(userDetails.getUsername()).orElse(null);
        if (supplier == null || supplier.getRole() != User.Role.SUPPLIER) {
            return "redirect:/login";
        }

        List<Product> products = productService.getProductsBySupplier(supplier);

        // Calculate inventory statistics
        long totalProducts = products.size();
        long lowStockProducts = products.stream().filter(p -> p.getStock() != null && p.getStock() <= 10).count();
        long outOfStockProducts = products.stream().filter(p -> p.getStock() != null && p.getStock() == 0).count();
        long normalStockProducts = totalProducts - lowStockProducts - outOfStockProducts;

        model.addAttribute("products", products);
        model.addAttribute("totalProducts", totalProducts);
        model.addAttribute("lowStockProducts", lowStockProducts);
        model.addAttribute("outOfStockProducts", outOfStockProducts);
        model.addAttribute("normalStockProducts", normalStockProducts);

        return "supplier/inventory";
    }

    @PostMapping("/inventory/update/{productId}")
    public String updateStock(@PathVariable Long productId,
                            @RequestParam Integer stock,
                            @AuthenticationPrincipal UserDetails userDetails,
                            RedirectAttributes redirectAttributes) {
        try {
            User supplier = userService.findByUsername(userDetails.getUsername()).orElseThrow();
            Product product = productService.getProductById(productId).orElseThrow();

            // Verify the product belongs to this supplier
            if (!product.getSupplier().getId().equals(supplier.getId())) {
                throw new IllegalArgumentException("Unauthorized access to product");
            }

            product.setStock(stock);
            productService.saveProduct(product);

            redirectAttributes.addFlashAttribute("successMessage", "Stok produk berhasil diperbarui!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Gagal memperbarui stok: " + e.getMessage());
        }

        return "redirect:/supplier/inventory";
    }

    @PostMapping("/inventory/bulk-update")
    public String bulkUpdateStock(@RequestParam List<Long> productIds,
                                @RequestParam List<Integer> stocks,
                                @AuthenticationPrincipal UserDetails userDetails,
                                RedirectAttributes redirectAttributes) {
        try {
            User supplier = userService.findByUsername(userDetails.getUsername()).orElseThrow();

            for (int i = 0; i < productIds.size(); i++) {
                Long productId = productIds.get(i);
                Integer stock = stocks.get(i);

                Product product = productService.getProductById(productId).orElseThrow();

                // Verify the product belongs to this supplier
                if (!product.getSupplier().getId().equals(supplier.getId())) {
                    continue; // Skip unauthorized products
                }

                product.setStock(stock);
                productService.saveProduct(product);
            }

            redirectAttributes.addFlashAttribute("successMessage", "Stok produk berhasil diperbarui secara massal!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Gagal memperbarui stok: " + e.getMessage());
        }

        return "redirect:/supplier/inventory";
    }
}