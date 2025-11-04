package savora.com.savora.service;

import savora.com.savora.model.Review;
import savora.com.savora.model.Product;
import savora.com.savora.model.User;
import savora.com.savora.repository.ReviewRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class ReviewService {

    @Autowired
    private ReviewRepository reviewRepository;

    public Review saveReview(Review review) {
        return reviewRepository.save(review);
    }

    public List<Review> getReviewsByProduct(Product product) {
        return reviewRepository.findByProductOrderByCreatedAtDesc(product);
    }

    public List<Review> getReviewsByBuyer(User buyer) {
        return reviewRepository.findByBuyerOrderByCreatedAtDesc(buyer);
    }

    public Optional<Review> getReviewByProductAndBuyer(Product product, User buyer) {
        return reviewRepository.findByProductAndBuyer(product, buyer);
    }

    public Double getAverageRating(Product product) {
        return reviewRepository.findAverageRatingByProduct(product);
    }

    public Long getReviewCount(Product product) {
        return reviewRepository.countReviewsByProduct(product);
    }

    public List<Review> getTopReviews(Product product, int limit) {
        List<Review> reviews = reviewRepository.findTop5ByProductOrderByCreatedAtDesc(product);
        return reviews.size() > limit ? reviews.subList(0, limit) : reviews;
    }

    public boolean canUserReviewProduct(User buyer, Product product) {
        // Check if user has purchased this product and order is delivered
        // This would require checking order history - simplified for now
        return !getReviewByProductAndBuyer(product, buyer).isPresent();
    }

    public Optional<Review> getReviewById(Long id) {
        return reviewRepository.findById(id);
    }

    public void deleteReview(Long id) {
        reviewRepository.deleteById(id);
    }

    public List<Review> getAllReviews() {
        return reviewRepository.findAll();
    }

    public Review createReview(User buyer, Product product, Integer rating, String comment) {
        if (!canUserReviewProduct(buyer, product)) {
            throw new IllegalArgumentException("User cannot review this product");
        }

        Review review = new Review();
        review.setBuyer(buyer);
        review.setProduct(product);
        review.setRating(rating);
        review.setComment(comment);

        return saveReview(review);
    }
}