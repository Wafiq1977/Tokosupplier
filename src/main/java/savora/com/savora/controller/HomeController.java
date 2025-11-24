package savora.com.savora.controller;

import savora.com.savora.model.Product;
import savora.com.savora.model.Category;
import savora.com.savora.service.ProductService;
import savora.com.savora.service.CategoryService;
import savora.com.savora.service.NotificationService;
import savora.com.savora.service.UserService;
import savora.com.savora.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

// DTO for product in search results
class ProductSearchDTO {
    private Long id;
    private String name;
    private String description;
    private BigDecimal price;
    private Integer stock;
    private String imageUrl;
    private Double averageRating;
    private String supplierCompanyName;

    public ProductSearchDTO(Product product) {
        this.id = product.getId();
        this.name = product.getName();
        this.description = product.getDescription();
        this.price = product.getPrice();
        this.stock = product.getStockQuantity();
        this.imageUrl = product.getImageUrl();
        this.averageRating = product.getAverageRating();
        this.supplierCompanyName = product.getSupplier().getCompanyName();
    }

    // Getters
    public Long getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public BigDecimal getPrice() { return price; }
    public Integer getStock() { return stock; }
    public String getImageUrl() { return imageUrl; }
    public Double getAverageRating() { return averageRating; }
    public String getSupplierCompanyName() { return supplierCompanyName; }
}

// Response class for search API
class SearchResponse {
    private List<ProductSearchDTO> products;
    private int currentPage;
    private int totalPages;
    private long totalItems;
    private boolean hasContent;

    // Constructor
    public SearchResponse(List<Product> products, int currentPage, int totalPages, long totalItems, boolean hasContent) {
        this.products = products.stream().map(ProductSearchDTO::new).collect(Collectors.toList());
        this.currentPage = currentPage;
        this.totalPages = totalPages;
        this.totalItems = totalItems;
        this.hasContent = hasContent;
    }

    // Getters
    public List<ProductSearchDTO> getProducts() { return products; }
    public int getCurrentPage() { return currentPage; }
    public int getTotalPages() { return totalPages; }
    public long getTotalItems() { return totalItems; }
    public boolean isHasContent() { return hasContent; }
}

@Controller
public class HomeController {

    @Autowired
    private ProductService productService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private UserService userService;

    @GetMapping("/")
    public String home(@RequestParam(value = "search", required = false) String search,
                        @RequestParam(value = "category", required = false) Long categoryId,
                        @RequestParam(value = "minPrice", required = false) Double minPrice,
                        @RequestParam(value = "maxPrice", required = false) Double maxPrice,
                        @RequestParam(value = "sort", defaultValue = "id") String sortBy,
                        @RequestParam(value = "order", defaultValue = "desc") String sortDir,
                        @RequestParam(value = "page", defaultValue = "0") int page,
                        @RequestParam(value = "size", defaultValue = "20") int size,
                        @AuthenticationPrincipal UserDetails userDetails,
                        Model model) {

        Page<Product> productPage;

        if (search != null && !search.trim().isEmpty()) {
            productPage = productService.searchProductsWithFilters(search, categoryId, minPrice, maxPrice, null, page, size, sortBy, sortDir);
            model.addAttribute("searchTerm", search);
        } else if (categoryId != null) {
            productPage = productService.searchProductsWithFilters(null, categoryId, minPrice, maxPrice, null, page, size, sortBy, sortDir);
            Category category = categoryService.getCategoryById(categoryId).orElse(null);
            if (category != null) {
                model.addAttribute("categoryName", category.getName());
            }
        } else {
            productPage = productService.getAllProductsPaged(page, size, sortBy, sortDir);
        }

        // Add flash sale products (low stock or featured)
        model.addAttribute("flashSaleProducts", productService.getLowStockProducts().subList(0, Math.min(6, productService.getLowStockProducts().size())));

        // Add popular products
        model.addAttribute("popularProducts", productService.getPopularProducts(0, 8));

        // Add new products
        model.addAttribute("newProducts", productService.getNewProducts(0, 8));

        model.addAttribute("products", productPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", productPage.getTotalPages());
        model.addAttribute("totalItems", productPage.getTotalElements());
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("sortDir", sortDir);
        model.addAttribute("search", search);
        model.addAttribute("category", categoryId);
        model.addAttribute("minPrice", minPrice);
        model.addAttribute("maxPrice", maxPrice);

        // Always add categories for navigation
        model.addAttribute("categories", categoryService.getAllCategories());

        // Add unread notification count for authenticated users
        if (userDetails != null) {
            User user = userService.findByUsername(userDetails.getUsername()).orElse(null);
            if (user != null) {
                model.addAttribute("unreadNotificationCount", notificationService.countUnreadNotifications(user));
            }
        }

        return "home";
    }

    @GetMapping("/api/search-products")
    @ResponseBody
    public ResponseEntity<SearchResponse> searchProductsApi(@RequestParam(value = "search", required = false) String search,
                                                           @RequestParam(value = "category", required = false) Long categoryId,
                                                           @RequestParam(value = "page", defaultValue = "0") int page,
                                                           @RequestParam(value = "size", defaultValue = "12") int size) {

        Page<Product> productPage = productService.searchProductsWithFilters(search, categoryId, null, null, null, page, size, "id", "desc");

        SearchResponse response = new SearchResponse(
            productPage.getContent(),
            page,
            productPage.getTotalPages(),
            productPage.getTotalElements(),
            !productPage.getContent().isEmpty()
        );

        return ResponseEntity.ok(response);
    }

    @GetMapping("/api/search-suggestions")
    @ResponseBody
    public ResponseEntity<List<String>> searchSuggestionsApi(@RequestParam(value = "q", required = false) String query,
                                                            @RequestParam(value = "limit", defaultValue = "10") int limit) {
        if (query == null || query.trim().length() < 1) {
            return ResponseEntity.ok(List.of());
        }

        List<String> suggestions = productService.getProductNameSuggestions(query.trim(), limit);
        return ResponseEntity.ok(suggestions);
    }

    // Login and register mappings moved to AuthController
}