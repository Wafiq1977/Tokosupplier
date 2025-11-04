package savora.com.savora.repository;

import savora.com.savora.model.Cart;
import savora.com.savora.model.User;
import savora.com.savora.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;
import java.util.Optional;

public interface CartRepository extends JpaRepository<Cart, Long> {
    List<Cart> findByUser(User user);

    Optional<Cart> findByUserAndProduct(User user, Product product);

    @Query("SELECT COUNT(c) FROM Cart c WHERE c.user = :user")
    Long countByUser(User user);

    @Query("SELECT SUM(c.quantity) FROM Cart c WHERE c.user = :user")
    Long getTotalQuantityByUser(User user);

    @Query("SELECT SUM(c.unitPrice * c.quantity) FROM Cart c WHERE c.user = :user")
    java.math.BigDecimal getTotalPriceByUser(User user);

    void deleteByUser(User user);
}