package savora.com.savora.service;

import savora.com.savora.model.Cart;
import savora.com.savora.model.User;
import savora.com.savora.model.Product;
import savora.com.savora.repository.CartRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
public class CartService {

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private ProductService productService;

    public List<Cart> getCartItems(User user) {
        return cartRepository.findByUser(user);
    }

    public Long getCartItemCount(User user) {
        return cartRepository.countByUser(user);
    }

    public Long getTotalQuantity(User user) {
        Long total = cartRepository.getTotalQuantityByUser(user);
        return total != null ? total : 0L;
    }

    public BigDecimal getTotalPrice(User user) {
        BigDecimal total = cartRepository.getTotalPriceByUser(user);
        return total != null ? total : BigDecimal.ZERO;
    }

    @Transactional
    public Cart addToCart(User user, Long productId, Integer quantity) {
        Product product = productService.getProductById(productId)
            .orElseThrow(() -> new RuntimeException("Product not found"));

        if (product.getStockQuantity() < quantity) {
            throw new RuntimeException("Insufficient stock");
        }

        Optional<Cart> existingCartItem = cartRepository.findByUserAndProduct(user, product);

        if (existingCartItem.isPresent()) {
            Cart cartItem = existingCartItem.get();
            cartItem.setQuantity(cartItem.getQuantity() + quantity);
            return cartRepository.save(cartItem);
        } else {
            Cart cartItem = new Cart();
            cartItem.setUser(user);
            cartItem.setProduct(product);
            cartItem.setQuantity(quantity);
            cartItem.setUnitPrice(product.getPrice());
            return cartRepository.save(cartItem);
        }
    }

    @Transactional
    public Cart updateQuantity(User user, Long productId, Integer quantity) {
        Product product = productService.getProductById(productId)
            .orElseThrow(() -> new RuntimeException("Product not found"));

        if (product.getStockQuantity() < quantity) {
            throw new RuntimeException("Insufficient stock");
        }

        Optional<Cart> existingCartItem = cartRepository.findByUserAndProduct(user, product);

        if (existingCartItem.isPresent()) {
            Cart cartItem = existingCartItem.get();
            cartItem.setQuantity(quantity);
            return cartRepository.save(cartItem);
        } else {
            throw new RuntimeException("Cart item not found");
        }
    }

    @Transactional
    public void removeFromCart(User user, Long productId) {
        Product product = productService.getProductById(productId)
            .orElseThrow(() -> new RuntimeException("Product not found"));

        cartRepository.findByUserAndProduct(user, product)
            .ifPresent(cartRepository::delete);
    }

    @Transactional
    public void clearCart(User user) {
        cartRepository.deleteByUser(user);
    }

    public Optional<Cart> getCartItem(User user, Long productId) {
        Product product = productService.getProductById(productId)
            .orElseThrow(() -> new RuntimeException("Product not found"));

        return cartRepository.findByUserAndProduct(user, product);
    }
}