package savora.com.savora.service;

import savora.com.savora.model.Order;
import savora.com.savora.model.OrderItem;
import savora.com.savora.model.Product;
import savora.com.savora.model.User;
import savora.com.savora.repository.OrderRepository;
import savora.com.savora.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private NotificationService notificationService;

    public Order createOrder(User buyer, List<OrderItem> orderItems) {
        Order order = new Order();
        order.setBuyer(buyer);
        order.setOrderItems(new java.util.HashSet<>(orderItems));

        // Set supplier from first product (assuming single supplier per order for simplicity)
        if (!orderItems.isEmpty()) {
            order.setSupplier(orderItems.get(0).getProduct().getSupplier());
        }

        // Calculate subtotal
        BigDecimal subtotal = orderItems.stream()
                .map(item -> item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        order.setTotalAmount(subtotal); // This will be updated with shipping cost later

        // Set order reference in items
        orderItems.forEach(item -> item.setOrder(order));

        Order savedOrder = orderRepository.save(order);

        // Send notifications
        notificationService.notifyOrderCreated(buyer, savedOrder.getSupplier(), savedOrder.getId().toString());

        return savedOrder;
    }

    public Order updateOrder(Order order) {
        // Recalculate total including shipping cost
        BigDecimal subtotal = order.getOrderItems().stream()
                .map(item -> item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        order.setTotalAmount(subtotal.add(order.getShippingCost() != null ? order.getShippingCost() : BigDecimal.ZERO));

        return orderRepository.save(order);
    }

    public List<Order> getOrdersByBuyer(User buyer) {
        return orderRepository.findByBuyer(buyer);
    }

    public List<Order> getOrdersBySupplier(User supplier) {
        return orderRepository.findBySupplier(supplier);
    }

    public Optional<Order> getOrderById(Long id) {
        return orderRepository.findById(id);
    }

    public Order updateOrderStatus(Long orderId, Order.Status status) {
        Optional<Order> orderOpt = orderRepository.findById(orderId);
        if (orderOpt.isPresent()) {
            Order order = orderOpt.get();
            Order.Status oldStatus = order.getStatus();
            order.setStatus(status);
            Order updatedOrder = orderRepository.save(order);

            // Send notifications for status change
            notificationService.notifyOrderStatusUpdate(
                order.getBuyer(),
                order.getSupplier(),
                orderId.toString(),
                status.toString()
            );

            return updatedOrder;
        }
        throw new RuntimeException("Order not found");
    }
}