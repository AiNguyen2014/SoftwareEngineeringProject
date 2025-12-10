package ecommerce.shoestore.shoes;

import jakarta.persistence.*;
import lombok.*;

/**
 * Entity ShoesImage - ĐÚNG THEO CLASS DIAGRAM
 */
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
    private Long id;

    @Column(name = "image_id")
    private Long imageId;

    @Column(name = "url", length = 1000, nullable = false)
    private String url;

    @Column(name = "is_thumbnail")
    private Boolean isThumbnail;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shoes_id", nullable = false)
    private Shoes shoes;

    @PrePersist
    protected void onCreate() {
        if (imageId == null) {
            imageId = id;
        }
    }

    public String getUrl() {
        return this.url;
    }

    public String getImageUrl() {
        return this.url;
    }
}