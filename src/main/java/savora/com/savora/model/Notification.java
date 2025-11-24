package savora.com.savora.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
@Data
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Type type = Type.INFO;

    @Column(nullable = false)
    private Boolean isRead = false;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    // Additional fields for order details
    private String orderId;
    private String productName;
    private Integer quantity;
    private String actionUrl; // URL to order detail page
    private String actionText; // Text for action button

    public enum Type {
        INFO, SUCCESS, WARNING, ERROR, ORDER_UPDATE, PRODUCT_UPDATE, PAYMENT_CONFIRMED, SHIPPING_UPDATE
    }
}