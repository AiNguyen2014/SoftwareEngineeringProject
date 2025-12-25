package ecommerce.shoestore.inventory.dto;
import ecommerce.shoestore.inventory.InventoryTransaction;
import ecommerce.shoestore.inventory.InventoryStatus;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
@Data
public class InventoryUpdateDto {
    private Long shoeId;
    private Long amount;
    private InventoryTransaction type;
    private String note;
}
