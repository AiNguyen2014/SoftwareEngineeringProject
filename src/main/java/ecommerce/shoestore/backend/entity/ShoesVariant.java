package ecommerce.shoestore.backend.entity;

import lombok.*;
import jakarta.persistence.*;

@Entity
@Table(name = "shoes_variant")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShoesVariant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 20)
    private String size;

    @Column(length = 100)
    private String color;

    @Column
    private Integer stock;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shoes_id", nullable = false)
    private Shoes shoes;
}