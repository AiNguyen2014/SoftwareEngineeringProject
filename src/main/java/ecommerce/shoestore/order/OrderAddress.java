package ecommerce.shoestore.order;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "order_address")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderAddress {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "\"addressId\"")
    private Long addressId;
    
    @Column(name = "\"userId\"", nullable = false)
    private Long userId;
    
    @Column(name = "\"recipientName\"", nullable = false)
    private String recipientName;
    
    @Column(name = "\"recipientPhone\"", nullable = false)
    private String recipientPhone;
    
    @Column(name = "province", nullable = false)
    private String province;
    
    @Column(name = "district", nullable = false)
    private String district;
    
    @Column(name = "commune", nullable = false)
    private String commune;
    
    @Column(name = "\"streetDetail\"", columnDefinition = "TEXT")
    private String streetDetail;
    
    @Column(name = "\"isDefault\"", nullable = false)
    private Boolean isDefault = false;
    
    @Column(name = "\"createdAt\"", nullable = false)
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (isDefault == null) {
            isDefault = false;
        }
    }
    
    // Helper method to get full address
    public String getFullAddress() {
        return streetDetail + ", " + commune + ", " + district + ", " + province;
    }
}
