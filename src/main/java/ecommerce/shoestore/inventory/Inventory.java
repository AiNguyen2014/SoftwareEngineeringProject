package ecommerce.shoestore.inventory;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.util.Set;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.LocalDateTime;
@Entity
@Data
@Table(name = "inventory")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class Inventory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column (name = "\"inventoryId\"")
    private Long inventoryId;

    @Column (name = "quantity", nullable = false)
    private Long quantity;

    @Enumerated(EnumType.STRING)
    @Column(name = "type")
    private InventoryTransaction type;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private InventoryStatus status;

    @UpdateTimestamp
    @Column(name = "\"updateAt\"")
    private LocalDateTime updateAt;

    @Column(name = "\"changeAmount\"")
    private Long changeAmount;

    @Column(name = "note", columnDefinition = "TEXT")
    private String note;

    @Column(name = "\"shoeId\"")
    private Long shoeId;
}
