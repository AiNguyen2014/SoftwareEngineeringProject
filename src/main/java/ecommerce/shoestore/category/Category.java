package ecommerce.shoestore.category; // 1. Khai báo package category

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
    private Long id;

    private String name;

    @Column(name = "display_name")
    private String displayName;

    // Lúc này đã hiểu List<Shoes> là gì nhờ dòng import trên
    @OneToMany(mappedBy = "category", fetch = FetchType.LAZY)
    private List<Shoes> shoes;
}