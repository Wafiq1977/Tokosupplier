package savora.com.savora.service;

import savora.com.savora.model.Order;
import savora.com.savora.model.OrderItem;
import savora.com.savora.model.User;
import savora.com.savora.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;

    public List<Order> getOrdersByBuyer(User buyer) {
        return orderRepository.findByBuyer(buyer);
    }

    public List<Order> getOrdersBySupplier(User supplier) {
        return orderRepository.findBySupplier(supplier);
    }

    public Order saveOrder(Order order) {
        return orderRepository.save(order);
    }

    public Optional<Order> getOrderById(Long id) {
        return orderRepository.findById(id);
    }

    @Transactional
    public Order createOrder(User buyer, List<OrderItem> items) {
        // Calculate total amount
        java.math.BigDecimal totalAmount = items.stream()
                .map(item -> item.getPrice().multiply(java.math.BigDecimal.valueOf(item.getQuantity())))
                .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);

        Order order = new Order();
        order.setBuyer(buyer);
        order.setSupplier(items.get(0).getProduct().getSupplier()); // Assuming all items from same supplier
        order.setTotalAmount(totalAmount);
        order.setOrderItems(new java.util.HashSet<>(items));

        // Set order reference in items
        items.forEach(item -> item.setOrder(order));

        return orderRepository.save(order);
    }

    @Transactional
    public Order updateOrder(Order order) {
        return orderRepository.save(order);
    }

    @Transactional
    public void updateOrderStatus(Long orderId, Order.Status status) {
        Order order = orderRepository.findById(orderId).orElseThrow();
        order.setStatus(status);
        orderRepository.save(order);
    }

    public List<Order> getOrdersByBuyerAndStatus(User buyer, Order.Status status) {
        return orderRepository.findByBuyerAndStatus(buyer, status);
    }

    public List<Order> getRecentOrdersByBuyer(User buyer, int limit) {
        List<Order> allOrders = orderRepository.findByBuyer(buyer);
        return allOrders.stream()
                .sorted((o1, o2) -> o2.getCreatedAt().compareTo(o1.getCreatedAt()))
                .limit(limit)
                .toList();
    }
}