package savora.com.savora.service;

import savora.com.savora.model.Order;
import savora.com.savora.model.OrderItem;
import savora.com.savora.model.Product;
import savora.com.savora.model.User;
import savora.com.savora.repository.OrderRepository;
import savora.com.savora.service.NotificationService;
import savora.com.savora.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private ProductService productService;

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
        if (items == null || items.isEmpty()) {
            throw new RuntimeException("Tidak ada item dalam pesanan");
        }

        // Validate all items have the same supplier
        User supplier = items.get(0).getProduct().getSupplier();
        if (supplier == null) {
            throw new RuntimeException("Supplier tidak ditemukan untuk produk");
        }

        for (OrderItem item : items) {
            if (!supplier.equals(item.getProduct().getSupplier())) {
                throw new RuntimeException("Semua produk harus dari supplier yang sama");
            }
        }

        // Validate stock availability and reserve stock
        for (OrderItem item : items) {
            Product product = item.getProduct();
            Integer currentStock = product.getStockQuantity();
            if (currentStock == null || currentStock < item.getQuantity()) {
                throw new RuntimeException("Stok produk '" + product.getName() + "' tidak mencukupi. Stok tersedia: " +
                    (currentStock != null ? currentStock : 0) + ", diminta: " + item.getQuantity());
            }
        }

        // Calculate total amount
        java.math.BigDecimal totalAmount = java.math.BigDecimal.ZERO;
        for (OrderItem item : items) {
            totalAmount = totalAmount.add(item.getPrice().multiply(java.math.BigDecimal.valueOf(item.getQuantity())));
        }

        Order order = new Order();
        order.setBuyer(buyer);
        order.setSupplier(supplier);
        order.setSubtotalAmount(totalAmount);
        order.setTotalAmount(totalAmount); // Will be updated with shipping cost later
        order.setOrderItems(new java.util.ArrayList<>(items));

        // Set order reference in items
        for (OrderItem item : items) {
            item.setOrder(order);
        }

        Order savedOrder = orderRepository.save(order);

        // Reduce stock for each product after successful order creation
        for (OrderItem item : items) {
            Product product = item.getProduct();
            Integer currentStock = product.getStockQuantity();
            if (currentStock != null) {
                int newStock = currentStock - item.getQuantity();
                product.setStockQuantity(newStock);
                productService.saveProduct(product);
                System.out.println("Stock reduced for product " + product.getName() + ": " + currentStock + " -> " + newStock);
            }
        }

        // Send notification for new order
        if (!items.isEmpty()) {
            String productName = items.get(0).getProduct().getName();
            int quantity = items.get(0).getQuantity();
            notificationService.notifyOrderCreated(buyer, supplier, savedOrder.getId().toString(), productName, quantity);
        }

        return savedOrder;
    }

    @Transactional
    public Order updateOrder(Order order) {
        return orderRepository.save(order);
    }

    @Transactional
    public void updateOrderStatus(Long orderId, Order.Status status) {
        Order order = orderRepository.findById(orderId).orElseThrow();
        Order.Status oldStatus = order.getStatus();
        order.setStatus(status);
        Order savedOrder = orderRepository.save(order);

        // Send notifications based on status change
        if (status == Order.Status.CONFIRMED && oldStatus != Order.Status.CONFIRMED) {
            // Notify buyer that order is confirmed
            notificationService.notifyOrderConfirmed(savedOrder.getBuyer(), savedOrder.getId().toString(), "2-3 hari kerja");
        } else if (status == Order.Status.SHIPPED && oldStatus != Order.Status.SHIPPED) {
            // Notify buyer that order is shipped
            notificationService.notifyOrderShipped(savedOrder.getBuyer(), savedOrder.getId().toString(),
                savedOrder.getTrackingNumber() != null ? savedOrder.getTrackingNumber() : "TBA",
                "3-5 hari kerja");
        }
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

    @Transactional
    public void deleteOrder(Long orderId, User buyer) {
        Order order = orderRepository.findById(orderId).orElseThrow(() -> new RuntimeException("Order not found"));

        // Verify that the buyer owns this order
        if (!order.getBuyer().getId().equals(buyer.getId())) {
            throw new RuntimeException("Unauthorized access to order");
        }

        // Only allow deletion of completed or cancelled orders
        if (order.getStatus() != Order.Status.DELIVERED && order.getStatus() != Order.Status.CANCELLED) {
            throw new RuntimeException("Hanya pesanan yang sudah selesai atau dibatalkan yang dapat dihapus");
        }

        orderRepository.delete(order);
    }

    // Get sequential order ID for buyer (1, 2, 3, etc. based on their orders)
    public Integer getSequentialOrderIdForBuyer(Order order, User buyer) {
        List<Order> buyerOrders = orderRepository.findByBuyer(buyer);
        buyerOrders.sort((o1, o2) -> o1.getCreatedAt().compareTo(o2.getCreatedAt()));

        for (int i = 0; i < buyerOrders.size(); i++) {
            if (buyerOrders.get(i).getId().equals(order.getId())) {
                return i + 1; // Start from 1
            }
        }
        return null;
    }

    // Get sequential order ID for supplier (1, 2, 3, etc. based on orders received)
    public Integer getSequentialOrderIdForSupplier(Order order, User supplier) {
        List<Order> supplierOrders = orderRepository.findBySupplier(supplier);
        supplierOrders.sort((o1, o2) -> o1.getCreatedAt().compareTo(o2.getCreatedAt()));

        for (int i = 0; i < supplierOrders.size(); i++) {
            if (supplierOrders.get(i).getId().equals(order.getId())) {
                return i + 1; // Start from 1
            }
        }
        return null;
    }
}