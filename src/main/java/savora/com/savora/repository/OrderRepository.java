package savora.com.savora.repository;

import savora.com.savora.model.Order;
import savora.com.savora.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByBuyer(User buyer);
    List<Order> findBySupplier(User supplier);
    List<Order> findByBuyerAndStatus(User buyer, Order.Status status);
    List<Order> findBySupplierAndStatus(User supplier, Order.Status status);
}