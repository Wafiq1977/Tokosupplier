package savora.com.savora.model;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "orders")
@Data
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "buyer_id", nullable = false)
    private User buyer;

    @ManyToOne
    @JoinColumn(name = "supplier_id", nullable = false)
    private User supplier;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL)
    private List<OrderItem> orderItems;

    @Column(nullable = false)
    private BigDecimal subtotalAmount;

    @Column(nullable = false)
    private BigDecimal totalAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status = Status.PENDING;

    // Payment information
    @Enumerated(EnumType.STRING)
    private PaymentMethod paymentMethod;

    @Enumerated(EnumType.STRING)
    private PaymentStatus paymentStatus = PaymentStatus.PENDING;

    // Shipping information
    private String shippingAddress;
    private String shippingCity;
    private String shippingProvince;
    private String shippingPostalCode;
    private String shippingPhone;

    @Enumerated(EnumType.STRING)
    private ShippingMethod shippingMethod;

    private BigDecimal shippingCost = BigDecimal.ZERO;

    private String trackingNumber;
    private String courierName;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    private LocalDateTime updatedAt;

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public enum Status {
        PENDING, CONFIRMED, PROCESSING, SHIPPED, DELIVERED, CANCELLED
    }

    public enum PaymentMethod {
        BANK_TRANSFER, E_WALLET, CREDIT_CARD, CASH_ON_DELIVERY
    }

    public enum PaymentStatus {
        PENDING, PAID, FAILED, REFUNDED
    }

    public enum ShippingMethod {
        REGULAR, EXPRESS, SAME_DAY, INSTANT
    }
}