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
    @Column(name = "\"logId\"")
    private Long logId;
    
    @Column(name = "\"orderId\"", nullable = false)
    private Long orderId;
    
    @Column(name = "\"oldStatus\"", length = 50)
    private String oldStatus;
    
    @Column(name = "\"newStatus\"", length = 50, nullable = false)
    private String newStatus;
    
    @Column(name = "\"changedAt\"", nullable = false)
    private LocalDateTime changedAt;
    
    @Column(name = "\"changedBy\"", nullable = false, length = 255)
    private String changedBy;
    
    @Column(name = "comment", columnDefinition = "TEXT")
    private String comment;
    
    @PrePersist
    protected void onCreate() {
        this.changedAt = LocalDateTime.now();
    }
}