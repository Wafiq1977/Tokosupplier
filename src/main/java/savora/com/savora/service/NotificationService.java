package savora.com.savora.service;

import savora.com.savora.model.Notification;
import savora.com.savora.model.User;
import savora.com.savora.repository.NotificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class NotificationService {

    @Autowired
    private NotificationRepository notificationRepository;

    public Notification createNotification(User user, String title, String message, Notification.Type type) {
        Notification notification = new Notification();
        notification.setUser(user);
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setType(type);
        return notificationRepository.save(notification);
    }

    public List<Notification> getUserNotifications(User user) {
        return notificationRepository.findByUserOrderByCreatedAtDesc(user);
    }

    public List<Notification> getUnreadNotifications(User user) {
        return notificationRepository.findByUserAndIsReadOrderByCreatedAtDesc(user, false);
    }

    public Long countUnreadNotifications(User user) {
        return notificationRepository.countUnreadByUser(user);
    }

    @Transactional
    public void markAllAsRead(User user) {
        notificationRepository.markAllAsReadByUser(user);
    }

    public void markAsRead(Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId).orElse(null);
        if (notification != null) {
            notification.setIsRead(true);
            notificationRepository.save(notification);
        }
    }

    // Helper methods for common notifications
    public void notifyOrderCreated(User buyer, User supplier, String orderId) {
        // Notify buyer
        createNotification(buyer,
            "Pesanan Berhasil Dibuat",
            "Pesanan #" + orderId + " telah berhasil dibuat. Supplier akan segera memproses pesanan Anda.",
            Notification.Type.SUCCESS);

        // Notify supplier
        createNotification(supplier,
            "Pesanan Baru Masuk",
            "Anda menerima pesanan baru #" + orderId + " dari " + buyer.getCompanyName(),
            Notification.Type.ORDER_UPDATE);
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
}