package savora.com.savora.repository;

import savora.com.savora.model.Product;
import savora.com.savora.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findBySupplier(User supplier);
    List<Product> findByCategoryId(Long categoryId);

    @Query("SELECT p FROM Product p WHERE p.name LIKE %:keyword% OR p.description LIKE %:keyword%")
    List<Product> searchByKeyword(String keyword);

    // Advanced search with filters
    @Query("SELECT p FROM Product p WHERE " +
           "(:keyword IS NULL OR p.name LIKE %:keyword% OR p.description LIKE %:keyword%) AND " +
           "(:categoryId IS NULL OR p.category.id = :categoryId) AND " +
           "(:minPrice IS NULL OR p.price >= :minPrice) AND " +
           "(:maxPrice IS NULL OR p.price <= :maxPrice) AND " +
           "(:supplierId IS NULL OR p.supplier.id = :supplierId)")
    Page<Product> findProductsWithFilters(String keyword, Long categoryId, Double minPrice, Double maxPrice, Long supplierId, Pageable pageable);

    // Popular products (based on orders - simplified)
    @Query("SELECT p FROM Product p ORDER BY p.price DESC")
    Page<Product> findPopularProducts(Pageable pageable);

    // New products
    @Query("SELECT p FROM Product p ORDER BY p.id DESC")
    Page<Product> findNewProducts(Pageable pageable);

    // Products by price range
    @Query("SELECT p FROM Product p WHERE p.price BETWEEN :minPrice AND :maxPrice ORDER BY p.price ASC")
    List<Product> findByPriceRange(Double minPrice, Double maxPrice);

    // Products with low stock
    @Query("SELECT p FROM Product p WHERE p.stockQuantity < 10 ORDER BY p.stockQuantity ASC")
    List<Product> findLowStockProducts();
}