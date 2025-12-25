package ecommerce.shoestore.order;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Entity
@Table(name = "orderitem")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderItem {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "\"orderItemId\"")
    private Long orderItemId;
    
    @Column(name = "\"orderId\"", nullable = false)
    private Long orderId;
    
    @Column(name = "\"shoeId\"", nullable = false)
    private Long shoeId;
    
    @Column(name = "quantity", nullable = false)
    private Long quantity;
    
    @Column(name = "\"productName\"")
    private String productName;
    
    @Column(name = "\"variantInfo\"")
    private String variantInfo;
    
    @Column(name = "\"unitPrice\"", columnDefinition = "numeric")
    private BigDecimal unitPrice;
    
    @Column(name = "\"shopDiscount\"", columnDefinition = "numeric")
    private BigDecimal shopDiscount;
    
    @Column(name = "\"itemTotal\"", columnDefinition = "numeric")
    private BigDecimal itemTotal;
}
