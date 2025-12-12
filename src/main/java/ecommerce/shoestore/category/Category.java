package ecommerce.shoestore.category;

import jakarta.persistence.*;
import lombok.*;
import java.util.List;
import ecommerce.shoestore.shoes.Shoes;

@Entity
@Table(name = "category")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "\"categoryId\"")
    private Long categoryId;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(name = "\"displayName\"")
    private String displayName;

    @OneToMany(mappedBy = "category", fetch = FetchType.LAZY)
    private List<Shoes> shoes;
}