package ecommerce.shoestore.inventory;
import ecommerce.shoestore.inventory.dto.InventoryResponseDto;
import ecommerce.shoestore.inventory.dto.InventoryUpdateDto;
import ecommerce.shoestore.inventory.InventoryTransaction;
import ecommerce.shoestore.shoes.ShoesRepository;
import ecommerce.shoestore.shoes.Shoes;
import io.micrometer.common.util.StringUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
@Service
@RequiredArgsConstructor
public class InventoryService {
    private final InventoryRepository inventoryRepository;
    private final ShoesRepository shoesRepository; //Lấy tên giày
    //Lấy danh sách hiển thị
    public List<InventoryResponseDto> getAllInventory(String keyword, InventoryStatus status) {
        List<Inventory> inventoryList;
        String statusStr = (status != null) ? status.name() : null;
        String keywordStr = (StringUtils.isNotBlank(keyword)) ? keyword : null;

        if (keywordStr == null && statusStr == null){
            inventoryList = inventoryRepository.findAll();
        } else {
            inventoryList = inventoryRepository.searchInventory(keywordStr, statusStr);
        }
        return inventoryList.stream().map((inv) -> {
            String shoeName = shoesRepository.findById(inv.getShoeId())
                    .map(s -> s.getName())
                    .orElse("Sản phẩm không tồn tại");
           return InventoryResponseDto.builder()
           .inventoryId(inv.getInventoryId())
           .shoeId(inv.getShoeId())
           .shoeName(shoeName)
           .quantity(inv.getQuantity())
           .status(inv.getStatus())
           .type(inv.getType())
           .updateAt(inv.getUpdateAt())
           .note(inv.getNote())
           .build();
        }).collect(Collectors.toList());
    }

    public void updateNoteOnly(Long inventoryId, String newNote){
        Inventory inventory = inventoryRepository.findById(inventoryId)
            .orElseThrow(() -> new RuntimeException("Không tìm thấy kho ID: " + inventoryId));
        inventory.setNote(newNote);
        inventory.setUpdateAt(LocalDateTime.now());
        inventoryRepository.save(inventory);
    }

    /*Chi tiết kho */
    public InventoryResponseDto getInventoryById(Long inventoryId) {
        Inventory inv = inventoryRepository.findById(inventoryId)
            .orElseThrow(() -> new RuntimeException("Không tìm thấy dữ liệu kho với ID: " + inventoryId));
        String shoeName = shoesRepository.findById(inv.getShoeId()).map(s -> s.getName()).orElse("Unknown Product");
        return InventoryResponseDto.builder().inventoryId(inv.getInventoryId())
        .shoeId(inv.getShoeId()).shoeName(shoeName).quantity(inv.getQuantity())
        .status(inv.getStatus()).type(inv.getType()).note(inv.getNote())
        .updateAt(inv.getUpdateAt()).build();
        }

    /*Thông báo */
    public List<InventoryResponseDto> getAlertInventory() {
        List<InventoryResponseDto> all = getAllInventory(null, null);
        return all.stream().filter(item -> item.getStatus() != InventoryStatus.IN_STOCK)
        .collect(Collectors.toList());
    }
    //Cập nhật tồn kho (Nhập / Xuất)
    @Transactional
    public void updateStock(InventoryUpdateDto dto){
        Inventory inventory = inventoryRepository.findByShoeId(dto.getShoeId())
        .orElseThrow(() -> new RuntimeException("Không tìm thấy kho cho giày ID: " + dto.getShoeId()));
        long currentQty = inventory.getQuantity() != null ? inventory.getQuantity() : 0;
        long changeAmount = dto.getAmount();
        if (dto.getType() == InventoryTransaction.IMPORT || dto.getType() == InventoryTransaction.RETURN) {
            currentQty += changeAmount;
        } else if (dto.getType() == InventoryTransaction.EXPORT) {
            if (currentQty < changeAmount) {
                throw new RuntimeException("Lỗi: Không đủ hàng để xuất kho!");
            }
            currentQty -= changeAmount;
        }
        /*Cập nhật lại các Entity */
        inventory.setQuantity(currentQty);
        inventory.setType(dto.getType());
        inventory.setNote(dto.getNote());
        inventory.setUpdateAt(LocalDateTime.now());
        
        if (currentQty == 0) {
            inventory.setStatus(InventoryStatus.OUT_OF_STOCK);
        } else if (currentQty < 5) {
            inventory.setStatus(InventoryStatus.ALMOST_OUT_OF_STOCK);
        } else {
            inventory.setStatus(InventoryStatus.IN_STOCK);
        }
        inventoryRepository.save(inventory);
    }
}
