package savora.com.savora.controller;

import savora.com.savora.model.Product;
import savora.com.savora.model.User;
import savora.com.savora.service.ProductService;
import savora.com.savora.service.FileUploadService;
import savora.com.savora.service.UserService;
import savora.com.savora.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import jakarta.validation.Valid;
import java.util.List;
import java.io.IOException;

@Controller
@RequestMapping("/products")
public class ProductController {

    @Autowired
    private ProductService productService;

    @Autowired
    private FileUploadService fileUploadService;

    @Autowired
    private UserService userService;

    @Autowired
    private CategoryService categoryService;

    @GetMapping("/{id}")
    public String viewProduct(@PathVariable Long id, Model model) {
        Product product = productService.getProductById(id).orElse(null);
        if (product == null) {
            return "redirect:/";
        }
        model.addAttribute("product", product);
        return "product-detail";
    }

    @GetMapping("/api/{id}")
    public ResponseEntity<Product> getProductApi(@PathVariable Long id) {
        Product product = productService.getProductById(id).orElse(null);
        if (product == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(product);
    }

    @GetMapping("/supplier")
    public String supplierProducts(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User supplier = userService.findByUsername(userDetails.getUsername()).orElse(null);
        if (supplier != null && supplier.getRole() == User.Role.SUPPLIER) {
            model.addAttribute("products", productService.getProductsBySupplier(supplier));
        } else {
            model.addAttribute("products", List.of()); // Empty list if not supplier
        }
        return "supplier/products";
    }

    @GetMapping("/add")
    public String addProductForm(Model model) {
        model.addAttribute("product", new Product());
        model.addAttribute("categories", categoryService.getAllCategories());
        return "supplier/add-product";
    }

    @PostMapping("/delete/{id}")
    public String deleteProduct(@PathVariable Long id,
                                @AuthenticationPrincipal UserDetails userDetails,
                                RedirectAttributes redirectAttributes) {
        try {
            User supplier = userService.findByUsername(userDetails.getUsername()).orElseThrow();
            Product product = productService.getProductById(id).orElseThrow();

            // Verify the product belongs to this supplier
            if (!product.getSupplier().getId().equals(supplier.getId())) {
                redirectAttributes.addFlashAttribute("errorMessage", "Anda tidak memiliki akses untuk menghapus produk ini.");
                return "redirect:/products/supplier";
            }

            productService.deleteProduct(id);
            redirectAttributes.addFlashAttribute("successMessage", "Produk berhasil dihapus!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Gagal menghapus produk: " + e.getMessage());
        }

        return "redirect:/products/supplier";
    }

    @GetMapping("/edit/{id}")
    public String editProductForm(@PathVariable Long id,
                                  @AuthenticationPrincipal UserDetails userDetails,
                                  Model model,
                                  RedirectAttributes redirectAttributes) {
        try {
            User supplier = userService.findByUsername(userDetails.getUsername()).orElseThrow();
            Product product = productService.getProductById(id).orElseThrow();

            // Verify the product belongs to this supplier
            if (!product.getSupplier().getId().equals(supplier.getId())) {
                redirectAttributes.addFlashAttribute("errorMessage", "Anda tidak memiliki akses untuk mengedit produk ini.");
                return "redirect:/products/supplier";
            }

            model.addAttribute("product", product);
            model.addAttribute("categories", categoryService.getAllCategories());
            return "supplier/edit-product";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Produk tidak ditemukan.");
            return "redirect:/products/supplier";
        }
    }

    @PostMapping("/edit/{id}")
    public String editProduct(@PathVariable Long id,
                              @ModelAttribute Product product,
                              BindingResult bindingResult,
                              @RequestParam(value = "price") String priceStr,
                              @RequestParam(value = "imageFile", required = false) MultipartFile imageFile,
                              @AuthenticationPrincipal UserDetails userDetails,
                              RedirectAttributes redirectAttributes) {
        try {
            User supplier = userService.findByUsername(userDetails.getUsername()).orElseThrow();
            Product existingProduct = productService.getProductById(id).orElseThrow();

            // Verify the product belongs to this supplier
            if (!existingProduct.getSupplier().getId().equals(supplier.getId())) {
                redirectAttributes.addFlashAttribute("errorMessage", "Anda tidak memiliki akses untuk mengedit produk ini.");
                return "redirect:/products/supplier";
            }

            // Set supplier and ID
            product.setSupplier(supplier);
            product.setId(id);

            // Convert price string to BigDecimal
            if (priceStr != null && !priceStr.trim().isEmpty()) {
                try {
                    // Remove any non-numeric characters except decimal point and comma
                    String cleanPrice = priceStr.replaceAll("[^\\d.,]", "").replace(".", "").replace(",", ".");
                    if (cleanPrice.isEmpty()) {
                        redirectAttributes.addFlashAttribute("errorMessage", "Harga produk tidak boleh kosong.");
                        return "redirect:/products/edit/" + id;
                    }
                    java.math.BigDecimal priceValue = new java.math.BigDecimal(cleanPrice);
                    if (priceValue.compareTo(new java.math.BigDecimal("1000")) < 0) {
                        redirectAttributes.addFlashAttribute("errorMessage", "Harga produk minimal Rp 1.000. Harga yang dimasukkan: " + priceValue);
                        return "redirect:/products/edit/" + id;
                    }
                    product.setPrice(priceValue);
                } catch (NumberFormatException e) {
                    redirectAttributes.addFlashAttribute("errorMessage", "Format harga tidak valid.");
                    return "redirect:/products/edit/" + id;
                }
            } else {
                redirectAttributes.addFlashAttribute("errorMessage", "Harga produk tidak boleh kosong.");
                return "redirect:/products/edit/" + id;
            }

            // Set stock quantity from stock field
            if (product.getStock() != null) {
                product.setStockQuantity(product.getStock());
            }

            // Handle file upload - only if a new file is provided
            if (imageFile != null && !imageFile.isEmpty()) {
                if (fileUploadService.isValidImageFile(imageFile)) {
                    String imageUrl = fileUploadService.uploadProductImage(imageFile);
                    product.setImageUrl(imageUrl);
                } else {
                    redirectAttributes.addFlashAttribute("errorMessage", "Format file gambar tidak valid. Gunakan JPG, PNG, GIF, atau WebP.");
                    return "redirect:/products/edit/" + id;
                }
            } else {
                // Keep existing image if no new file uploaded
                product.setImageUrl(existingProduct.getImageUrl());
            }

            productService.saveProduct(product);
            redirectAttributes.addFlashAttribute("successMessage", "Produk berhasil diperbarui!");
        } catch (IOException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Gagal mengupload gambar: " + e.getMessage());
            return "redirect:/products/edit/" + id;
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Gagal memperbarui produk: " + e.getMessage());
            return "redirect:/products/edit/" + id;
        }
        return "redirect:/products/supplier";
    }

    @PostMapping("/add")
    public String addProduct(@ModelAttribute Product product,
                             BindingResult bindingResult,
                             @RequestParam(value = "price") String priceStr,
                             @RequestParam(value = "imageFile", required = false) MultipartFile imageFile,
                             @AuthenticationPrincipal UserDetails userDetails,
                             RedirectAttributes redirectAttributes) {
        try {
            // Set supplier from authenticated user
            User supplier = userService.findByUsername(userDetails.getUsername()).orElseThrow();
            product.setSupplier(supplier);

            // Convert price string to BigDecimal
            if (priceStr != null && !priceStr.trim().isEmpty()) {
                try {
                    // Remove any non-numeric characters except decimal point and comma
                    String cleanPrice = priceStr.replaceAll("[^\\d.,]", "").replace(".", "").replace(",", ".");
                    if (cleanPrice.isEmpty()) {
                        redirectAttributes.addFlashAttribute("errorMessage", "Harga produk tidak boleh kosong.");
                        return "redirect:/products/add";
                    }
                    java.math.BigDecimal priceValue = new java.math.BigDecimal(cleanPrice);
                    // Debug: print the price value
                    System.out.println("Price value: " + priceValue + ", compared to 1000: " + priceValue.compareTo(new java.math.BigDecimal("1000")));
                    if (priceValue.compareTo(new java.math.BigDecimal("1000")) < 0) {
                        redirectAttributes.addFlashAttribute("errorMessage", "Harga produk minimal Rp 1.000. Harga yang dimasukkan: " + priceValue);
                        return "redirect:/products/add";
                    }
                    product.setPrice(priceValue);
                } catch (NumberFormatException e) {
                    redirectAttributes.addFlashAttribute("errorMessage", "Format harga tidak valid.");
                    return "redirect:/products/add";
                }
            } else {
                redirectAttributes.addFlashAttribute("errorMessage", "Harga produk tidak boleh kosong.");
                return "redirect:/products/add";
            }

            // Set stock quantity from stock field
            if (product.getStock() != null) {
                product.setStockQuantity(product.getStock());
            }

            // Handle file upload
            if (imageFile != null && !imageFile.isEmpty()) {
                if (fileUploadService.isValidImageFile(imageFile)) {
                    String imageUrl = fileUploadService.uploadProductImage(imageFile);
                    product.setImageUrl(imageUrl);
                } else {
                    redirectAttributes.addFlashAttribute("errorMessage", "Format file gambar tidak valid. Gunakan JPG, PNG, GIF, atau WebP.");
                    return "redirect:/products/add";
                }
            }

            productService.saveProduct(product);
            redirectAttributes.addFlashAttribute("successMessage", "Produk berhasil ditambahkan!");
        } catch (IOException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Gagal mengupload gambar: " + e.getMessage());
            return "redirect:/products/add";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Gagal menambahkan produk: " + e.getMessage());
            return "redirect:/products/add";
        }
        return "redirect:/products/supplier";
    }
}