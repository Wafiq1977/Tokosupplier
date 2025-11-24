package savora.com.savora.service;

import savora.com.savora.model.Notification;
import savora.com.savora.model.User;
import savora.com.savora.repository.NotificationRepository;
import savora.com.savora.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class NotificationService {

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private EmailService emailService;

    public Notification createNotification(User user, String title, String message, Notification.Type type) {
        Notification notification = new Notification();
        notification.setUser(user);
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setType(type);
        return notificationRepository.save(notification);
    }

    public List<Notification> getUserNotifications(User user) {
        if (user == null) return new java.util.ArrayList<>();
        return notificationRepository.findByUserOrderByCreatedAtDesc(user);
    }

    public List<Notification> getUnreadNotifications(User user) {
        if (user == null) return new java.util.ArrayList<>();
        return notificationRepository.findByUserAndIsReadOrderByCreatedAtDesc(user, false);
    }

    public Long countUnreadNotifications(User user) {
        if (user == null) return 0L;
        return notificationRepository.countByUserAndIsRead(user, false);
    }

    @Transactional
    public void markAllAsRead(User user) {
        if (user != null) {
            notificationRepository.markAllAsReadByUser(user);
        }
    }

    public void markAsRead(Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId).orElse(null);
        if (notification != null) {
            notification.setIsRead(true);
            notificationRepository.save(notification);
        }
    }

    // Helper methods for common notifications
    public void notifyOrderCreated(User buyer, User supplier, String orderId, String productName, Integer quantity) {
        // Notify buyer
        Notification buyerNotification = new Notification();
        buyerNotification.setUser(buyer);
        buyerNotification.setTitle("Pesanan Berhasil Dibuat");
        buyerNotification.setMessage("Pesanan #" + orderId + " telah berhasil dibuat. Supplier akan segera memproses pesanan Anda.");
        buyerNotification.setType(Notification.Type.SUCCESS);
        buyerNotification.setOrderId(orderId);
        buyerNotification.setProductName(productName);
        buyerNotification.setQuantity(quantity);
        notificationRepository.save(buyerNotification);

        // Notify supplier
        Notification supplierNotification = new Notification();
        supplierNotification.setUser(supplier);
        supplierNotification.setTitle("Pesanan Baru Masuk");
        supplierNotification.setMessage("Anda menerima pesanan baru #" + orderId + " dari " + buyer.getCompanyName() + ". Produk: " + productName + " (Qty: " + quantity + ")");
        supplierNotification.setType(Notification.Type.ORDER_UPDATE);
        supplierNotification.setOrderId(orderId);
        supplierNotification.setProductName(productName);
        supplierNotification.setQuantity(quantity);
        notificationRepository.save(supplierNotification);

        // Send email notifications
        emailService.sendNewOrderNotification(supplier, buyer, orderId, productName, quantity);
    }

    public void notifyPaymentConfirmed(User buyer, User supplier, String orderId, Double amount, String paymentMethod) {
        // Notify buyer
        Notification buyerNotification = new Notification();
        buyerNotification.setUser(buyer);
        buyerNotification.setTitle("Pembayaran Dikonfirmasi");
        buyerNotification.setMessage("Pembayaran sebesar Rp " + String.format("%,.0f", amount) + " untuk pesanan #" + orderId + " telah dikonfirmasi.");
        buyerNotification.setType(Notification.Type.PAYMENT_CONFIRMED);
        buyerNotification.setOrderId(orderId);
        notificationRepository.save(buyerNotification);

        // Notify supplier
        Notification supplierNotification = new Notification();
        supplierNotification.setUser(supplier);
        supplierNotification.setTitle("Pembayaran Masuk");
        supplierNotification.setMessage("Pembayaran sebesar Rp " + String.format("%,.0f", amount) + " untuk pesanan #" + orderId + " telah diterima via " + paymentMethod + ".");
        supplierNotification.setType(Notification.Type.PAYMENT_CONFIRMED);
        supplierNotification.setOrderId(orderId);
        notificationRepository.save(supplierNotification);
    }

    public void notifyOrderShipped(User buyer, String orderId, String trackingNumber, String estimatedDelivery) {
        Notification notification = new Notification();
        notification.setUser(buyer);
        notification.setTitle("Pesanan Dikirim");
        notification.setMessage("Pesanan #" + orderId + " telah dikirim. Nomor tracking: " + trackingNumber + ". Estimasi tiba: " + estimatedDelivery);
        notification.setType(Notification.Type.SHIPPING_UPDATE);
        notification.setOrderId(orderId);
        notificationRepository.save(notification);
    }

    public void notifyOrderConfirmed(User buyer, String orderId, String estimatedShipping) {
        Notification notification = new Notification();
        notification.setUser(buyer);
        notification.setTitle("Pesanan Dikonfirmasi");
        notification.setMessage("Pesanan #" + orderId + " telah dikonfirmasi oleh supplier. Estimasi pengiriman: " + estimatedShipping);
        notification.setType(Notification.Type.ORDER_UPDATE);
        notification.setOrderId(orderId);
        notificationRepository.save(notification);
    }

    public void notifyPaymentApproved(User buyer, String orderId, Double amount) {
        Notification notification = new Notification();
        notification.setUser(buyer);
        notification.setTitle("Pembayaran Disetujui");
        notification.setMessage("Pembayaran sebesar Rp " + String.format("%,.0f", amount) + " untuk pesanan #" + orderId + " telah disetujui.");
        notification.setType(Notification.Type.PAYMENT_CONFIRMED);
        notification.setOrderId(orderId);
        notificationRepository.save(notification);
    }

    public void notifyOrderStatusUpdate(User buyer, User supplier, String orderId, String newStatus) {
        // Notify buyer
        createNotification(buyer,
            "Status Pesanan Diperbarui",
            "Status pesanan #" + orderId + " telah diperbarui menjadi: " + newStatus,
            Notification.Type.ORDER_UPDATE);

        // Notify supplier
        createNotification(supplier,
            "Status Pesanan Diperbarui",
            "Status pesanan #" + orderId + " telah diperbarui menjadi: " + newStatus,
            Notification.Type.INFO);
    }

    public void notifyProductAdded(User supplier, String productName) {
        createNotification(supplier,
            "Produk Baru Ditambahkan",
            "Produk '" + productName + "' telah berhasil ditambahkan ke katalog Anda.",
            Notification.Type.PRODUCT_UPDATE);
    }

    public void deleteNotification(Long notificationId) {
        notificationRepository.deleteById(notificationId);
    }
}