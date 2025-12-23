package ecommerce.shoestore.inventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
@Repository
public interface InventoryRepository extends JpaRepository<Inventory, Long> {
    Optional<Inventory> findByShoeId(Long shoeId);
    @Query(value = """
            SELECT i.* FROM inventory i
            LEFT JOIN shoes s ON i."shoeId" = s."shoeId"
            WHERE (:keyword IS NULL OR LOWER(s.name) LIKE LOWER(CONCAT('%', :keyword, '%')))
            AND (:status IS NULL OR CAST(i.status AS text) = :status)
            ORDER BY i."updateAt" DESC
            """, nativeQuery = true)
    List<Inventory> searchInventory(
        @Param("keyword") String keyword,
        @Param("status") String status
    );
}
