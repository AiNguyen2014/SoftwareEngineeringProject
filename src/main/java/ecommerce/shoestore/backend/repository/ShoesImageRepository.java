package ecommerce.shoestore.backend.repository;

import ecommerce.shoestore.backend.entity.ShoesImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ShoesImageRepository extends JpaRepository<ShoesImage, Long> {

    /**
     * Tìm tất cả hình ảnh của một sản phẩm, sắp xếp theo display_order
     */
    List<ShoesImage> findByShoesIdOrderByDisplayOrderAsc(Long shoesId);
}
