package savora.com.savora.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "products")
@Data
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Nama produk tidak boleh kosong")
    @Size(max = 100, message = "Nama produk maksimal 100 karakter")
    @Column(nullable = false)
    private String name;

    @NotBlank(message = "Deskripsi produk tidak boleh kosong")
    @Size(max = 1000, message = "Deskripsi produk maksimal 1000 karakter")
    private String description;

    @NotNull(message = "Harga produk tidak boleh kosong")
    @DecimalMin(value = "1000", message = "Harga minimal Rp 1.000")
    @Column(nullable = false)
    private BigDecimal price;

    @NotNull(message = "Stok produk tidak boleh kosong")
    @Min(value = 0, message = "Stok tidak boleh negatif")
    private Integer stockQuantity;

    @Size(max = 50, message = "Satuan maksimal 50 karakter")
    private String unit;

    private String imageUrl;

    @NotNull(message = "Supplier tidak boleh kosong")
    @ManyToOne
    @JoinColumn(name = "supplier_id", nullable = false)
    private User supplier;

    @NotNull(message = "Kategori produk tidak boleh kosong")
    @ManyToOne
    @JoinColumn(name = "category_id")
    private Category category;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    private LocalDateTime updatedAt;

    // Analytics fields
    private Integer stock = 0;
    private Integer salesCount = 0;
    private Double averageRating = 0.0;

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
