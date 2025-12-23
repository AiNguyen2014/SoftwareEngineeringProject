package ecommerce.shoestore.order;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "ordertrackinglog")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderTrackingLog {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "logid")
    private Long logId;
    
    @Column(name = "orderid", nullable = false)
    private Long orderId;
    
    @Column(name = "oldstatus", length = 50)
    private String oldStatus;
    
    @Column(name = "newstatus", length = 50, nullable = false)
    private String newStatus;
    
    @Column(name = "changeat", nullable = false)
    private LocalDateTime changeAt;
    
    @Column(name = "changedby", nullable = false, length = 255)
    private String changedBy;
    
    @Column(name = "comment", columnDefinition = "TEXT")
    private String comment;
    
    @PrePersist
    protected void onCreate() {
        this.changeAt = LocalDateTime.now();
    }
}