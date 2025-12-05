package ecommerce.shoestore.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.List;

@Entity
@Table(name = "category")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Category {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;        // MEN, WOMEN

    @Column(name = "display_name")
    private String displayName; // Nam, Nữ

    @OneToMany(mappedBy = "category", fetch = FetchType.LAZY)
    private List<Shoes> shoes;
}