package ecommerce.shoestore.inventory;
import ecommerce.shoestore.inventory.dto.InventoryResponseDto;
import ecommerce.shoestore.inventory.dto.InventoryUpdateDto;
import ecommerce.shoestore.inventory.InventoryTransaction;
import ecommerce.shoestore.shoes.ShoesRepository;
import ecommerce.shoestore.shoesvariant.ShoesVariant;
import ecommerce.shoestore.shoesvariant.ShoesVariantRepository;
import ecommerce.shoestore.shoes.Shoes;
import ecommerce.shoestore.order.OrderItem;
import io.micrometer.common.util.StringUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
@Service
@RequiredArgsConstructor
public class InventoryService {
    private final InventoryRepository inventoryRepository;
    private final ShoesRepository shoesRepository;
    private final ShoesVariantRepository shoesVariantRepository;

    public Page<InventoryResponseDto> getAllInventory(String keyword, InventoryStatus status, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        String statusStr = (status != null) ? status.name() : null;
        String keywordStr = (StringUtils.isNotBlank(keyword)) ? keyword : null;
        Page<Inventory> inventoryPage;
        if (keywordStr == null && statusStr == null) {
            inventoryPage = inventoryRepository.findAll(pageable);
        } else {
            inventoryPage = inventoryRepository.searchInventory(keywordStr, statusStr, pageable);
        }
        Set<Long> shoeIds = inventoryPage.getContent().stream()
                .map(Inventory::getShoeId)
                .collect(Collectors.toSet());
        Map<Long, String> shoeNamesMap = shoesRepository.findAllById(shoeIds).stream()
                .collect(Collectors.toMap(Shoes::getShoeId, Shoes::getName));
        return inventoryPage.map(inv -> {
            // Lấy tên từ Map (Siêu nhanh vì nằm trong RAM)
            String shoeName = shoeNamesMap.getOrDefault(inv.getShoeId(), "Sản phẩm không tồn tại");

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
        });
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
        Page<InventoryResponseDto> page = getAllInventory(null, null, 0, 1000);
        return page.getContent().stream()
                .filter(item -> item.getStatus() != InventoryStatus.IN_STOCK)
                .collect(Collectors.toList());
    }
    //Cập nhật tồn kho (Nhập / Xuất)
    @Transactional
    public void updateStock(InventoryUpdateDto dto){
        Inventory inventory = inventoryRepository.findByShoeId(dto.getShoeId()).orElseThrow(() -> new RuntimeException("Không tìm thấy kho cho giày ID: " + dto.getShoeId()));
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
    @Transactional
    public void restoreStock(List<OrderItem> items){
        if (items == null || items.isEmpty()) return;
        for (OrderItem item : items){
            Inventory inventory = inventoryRepository.findByShoeId(item.getShoeId()).orElse(null);
            if (inventory != null){
                long currentQty = inventory.getQuantity() != null ? inventory.getQuantity() : 0;
                long newQty = currentQty + item.getQuantity();
                inventory.setQuantity(newQty);
                inventory.setType(InventoryTransaction.RETURN);
                inventory.setNote("Hoàn kho do huỷ đơn hàng");
                inventory.setUpdateAt(LocalDateTime.now());
                if (newQty == 0){
                    inventory.setStatus(InventoryStatus.OUT_OF_STOCK);
                }else if (newQty <= 5){
                    inventory.setStatus(InventoryStatus.ALMOST_OUT_OF_STOCK);
                }else{
                    inventory.setStatus(InventoryStatus.IN_STOCK);
                }
                inventoryRepository.save(inventory);
            }
        }
    }
    public List<Map<String, Object>> getVariantsForDropdown(Long shoeId) {
        return shoesVariantRepository.findByShoes_ShoeId(shoeId).stream()
            .map(v -> {
                Map<String, Object> map = new HashMap<>();
                map.put("variantId", v.getVariantId());
                // Hiển thị: Size 40 - Màu Trắng (Tồn: 10)
                String label = String.format("Size %s - %s (Tồn: %d)", 
                        v.getSize().name(), v.getColor().name(), v.getStock());
                map.put("label", label);
                return map;
            })
            .collect(Collectors.toList());
    }
    @Transactional
    public void updateVariantStock(Long variantId, Long amount, String typeStr, String note){
        ShoesVariant variant = shoesVariantRepository.findById(variantId)
            .orElseThrow(() -> new RuntimeException("Không tìm thấy biến thể giày với ID: " + variantId));
        InventoryTransaction type = InventoryTransaction.valueOf(typeStr);
        int currentStock = variant.getStock() != null ? variant.getStock() : 0;
        int changeAmount = amount.intValue();
        if (type == InventoryTransaction.IMPORT || type == InventoryTransaction.RETURN) {
            variant.setStock(currentStock + changeAmount);
        } else if (type == InventoryTransaction.EXPORT) {
            if (currentStock < changeAmount) throw new RuntimeException("Không đủ hàng để xuất");
            variant.setStock(currentStock - changeAmount);
        }
        shoesVariantRepository.save(variant);
        syncInventoryFromVariant(variant.getShoes().getShoeId());
    }
    private void syncInventoryFromVariant(Long shoeId) {
        // Gọi sang hàm chính với type và note là null
        syncInventoryFromVariant(shoeId, null, null);
    }
    private void syncInventoryFromVariant(Long shoeId, InventoryTransaction type, String note) {
        Inventory inventory = inventoryRepository.findByShoeId(shoeId).orElse(null);
        if (inventory != null) {
            Integer total = shoesVariantRepository.getTotalStockByShoeId(shoeId);
            inventory.setQuantity(total != null ? total.longValue() : 0L);
            inventory.setUpdateAt(LocalDateTime.now());
            if (type != null) inventory.setType(type);
            if (note != null && !note.isEmpty()) inventory.setNote(note);
            // Cập nhật trạng thái
            if (total <= 0) inventory.setStatus(InventoryStatus.OUT_OF_STOCK);
            else if (total <= 5) inventory.setStatus(InventoryStatus.ALMOST_OUT_OF_STOCK);
            else inventory.setStatus(InventoryStatus.IN_STOCK);
            
            inventoryRepository.save(inventory);
        }
    }
}
