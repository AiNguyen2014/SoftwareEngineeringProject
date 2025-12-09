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

    // ✅ ĐÚNG: Có imageId riêng
    @Column(name = "image_id")
    private Long imageId;

    // ✅ ĐÚNG: Tên cột là "url" không phải "image_url"
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

    // Backward compatible với code cũ
    public String getImageUrl() {
        return this.url;
    }
}