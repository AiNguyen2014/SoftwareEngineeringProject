package ecommerce.shoestore.inventory.dto;
import java.time.LocalDateTime;
import ecommerce.shoestore.inventory.InventoryStatus;
import ecommerce.shoestore.inventory.InventoryTransaction;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class InventoryResponseDto {
    private Long inventoryId;
    private Long shoeId;
    private String shoeName;
    private Long quantity;
    private InventoryStatus status;
    private InventoryTransaction type;
    private Long changeAmount;
    private LocalDateTime updateAt;
    private String note;
}
