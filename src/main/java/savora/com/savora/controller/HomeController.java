package savora.com.savora.controller;

import savora.com.savora.model.Product;
import savora.com.savora.model.Category;
import savora.com.savora.service.ProductService;
import savora.com.savora.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class HomeController {

    @Autowired
    private ProductService productService;

    @Autowired
    private CategoryService categoryService;

    @GetMapping("/")
    public String home(@RequestParam(value = "search", required = false) String search,
                       @RequestParam(value = "category", required = false) Long categoryId,
                       @RequestParam(value = "minPrice", required = false) Double minPrice,
                       @RequestParam(value = "maxPrice", required = false) Double maxPrice,
                       @RequestParam(value = "sort", defaultValue = "id") String sortBy,
                       @RequestParam(value = "order", defaultValue = "desc") String sortDir,
                       @RequestParam(value = "page", defaultValue = "0") int page,
                       @RequestParam(value = "size", defaultValue = "20") int size,
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
        return "home";
    }

    // Login and register mappings moved to AuthController
}