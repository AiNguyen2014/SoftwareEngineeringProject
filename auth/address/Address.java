package ecommerce.shoestore.auth.address;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "address")
@Data @NoArgsConstructor @AllArgsConstructor
public class Address {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long addressId;
    
    private String province;
    private String district;
    private String commune;
    
    @Column(columnDefinition = "TEXT")
    private String streetDetail;
}