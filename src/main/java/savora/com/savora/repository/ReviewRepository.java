package savora.com.savora.repository;

import savora.com.savora.model.Review;
import savora.com.savora.model.Product;
import savora.com.savora.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    List<Review> findByProductOrderByCreatedAtDesc(Product product);
    List<Review> findByBuyerOrderByCreatedAtDesc(User buyer);
    Optional<Review> findByProductAndBuyer(Product product, User buyer);

    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.product = :product")
    Double findAverageRatingByProduct(@Param("product") Product product);

    @Query("SELECT COUNT(r) FROM Review r WHERE r.product = :product")
    Long countReviewsByProduct(@Param("product") Product product);

    @Query("SELECT r FROM Review r WHERE r.product = :product ORDER BY r.createdAt DESC")
    List<Review> findTop5ByProductOrderByCreatedAtDesc(@Param("product") Product product);
}