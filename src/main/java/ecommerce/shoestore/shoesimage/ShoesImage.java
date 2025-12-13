package ecommerce.shoestore.shoesimage;

import ecommerce.shoestore.shoes.Shoes;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "shoes_image")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShoesImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "\"imageId\"")
    private Long imageId;

    @Column(nullable = false, length = 1000)
    private String url;

    @Column(name = "\"isThumbnail\"")
    private boolean isThumbnail;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "\"shoeId\"", nullable = false)
    private Shoes shoes;
}