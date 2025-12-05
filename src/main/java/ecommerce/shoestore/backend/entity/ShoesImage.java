package ecommerce.shoestore.backend.entity;

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
    private Long id;

    // SỬA TẠI ĐÂY: Đổi tên biến thành imageUrl để Lombok sinh ra getImageUrl()
    @Column(name = "image_url", length = 1000)
    private String imageUrl;

    @Column(name = "is_thumbnail")
    private Boolean isThumbnail;

    @Column(name = "display_order")
    private Integer displayOrder;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shoes_id")
    private Shoes shoes;

    // Helper method: Trả về URL (đề phòng trường hợp bạn dùng getUrl ở chỗ khác)
    public String getUrl() {
        return this.imageUrl;
    }
}