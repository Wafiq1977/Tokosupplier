package savora.com.savora.service;

import savora.com.savora.model.Product;
import savora.com.savora.model.User;
import savora.com.savora.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class ProductService {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private NotificationService notificationService;

    public Product saveProduct(Product product) {
        Product savedProduct = productRepository.save(product);

        // Send notification to supplier about new product
        if (product.getId() == null) { // New product
            notificationService.notifyProductAdded(product.getSupplier(), product.getName());
        }

        return savedProduct;
    }

    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    public Page<Product> getAllProductsPaged(int page, int size, String sortBy, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        return productRepository.findAll(pageable);
    }

    public List<Product> getProductsBySupplier(User supplier) {
        return productRepository.findBySupplier(supplier);
    }

    // Get popular products as list
    public List<Product> getPopularProducts(int offset, int limit) {
        Pageable pageable = PageRequest.of(offset / limit, limit);
        return productRepository.findPopularProducts(pageable).getContent();
    }

    // Get new products as list
    public List<Product> getNewProducts(int offset, int limit) {
        Pageable pageable = PageRequest.of(offset / limit, limit);
        return productRepository.findNewProducts(pageable).getContent();
    }

    public List<Product> getProductsByCategory(Long categoryId) {
        return productRepository.findByCategoryId(categoryId);
    }

    public List<Product> searchProducts(String keyword) {
        return productRepository.searchByKeyword(keyword);
    }

    // Advanced search with filters
    public Page<Product> searchProductsWithFilters(String keyword, Long categoryId, Double minPrice,
                                                  Double maxPrice, Long supplierId, int page, int size,
                                                  String sortBy, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        return productRepository.findProductsWithFilters(keyword, categoryId, minPrice, maxPrice, supplierId, pageable);
    }

    // Get popular products as Page
    public Page<Product> getPopularProductsPaged(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return productRepository.findPopularProducts(pageable);
    }

    // Get new products as Page
    public Page<Product> getNewProductsPaged(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return productRepository.findNewProducts(pageable);
    }

    // Get products by price range
    public List<Product> getProductsByPriceRange(Double minPrice, Double maxPrice) {
        return productRepository.findByPriceRange(minPrice, maxPrice);
    }

    // Get low stock products
    public List<Product> getLowStockProducts() {
        return productRepository.findLowStockProducts();
    }

    public Optional<Product> getProductById(Long id) {
        return productRepository.findById(id);
    }

    public void deleteProduct(Long id) {
        productRepository.deleteById(id);
    }

    // Get product name suggestions for autocomplete
    public List<String> getProductNameSuggestions(String query, int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        return productRepository.findProductNameSuggestionsLimited(query, pageable);
    }
}