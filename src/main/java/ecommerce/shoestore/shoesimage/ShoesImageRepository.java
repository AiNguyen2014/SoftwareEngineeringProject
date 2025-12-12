package ecommerce.shoestore.shoesimage;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ShoesImageRepository extends JpaRepository<ShoesImage, Long> {
}