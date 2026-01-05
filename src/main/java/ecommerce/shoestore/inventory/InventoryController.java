package ecommerce.shoestore.inventory;
import ecommerce.shoestore.inventory.dto.InventoryResponseDto;
import ecommerce.shoestore.inventory.dto.InventoryUpdateDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ModelAttribute;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/admin/inventory")
@RequiredArgsConstructor
public class InventoryController {
    private final InventoryService inventoryService;
    @GetMapping
    public String showInventoryPage(Model model,
        @RequestParam(name = "keyword", required = false) String keyword,
        @RequestParam(name = "status", required = false) InventoryStatus status,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size
    ) {
        Page<InventoryResponseDto> pageResult = inventoryService.getAllInventory(keyword, status, page, size);
        model.addAttribute("inventories", pageResult.getContent()); 
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", pageResult.getTotalPages());
        model.addAttribute("totalItems", pageResult.getTotalElements());
        model.addAttribute("currentKeyword", keyword);
        model.addAttribute("currentStatus", status);
        model.addAttribute("activeMenu", "inventory");

        List<InventoryResponseDto> notifications = inventoryService.getAlertInventory();
        model.addAttribute("notifications", notifications);
        return "admin/inventory/inventory-main";
    }

    /* Nhập hàng */
    @GetMapping("/import")
    public String showImportForm(Model model) {
        model.addAttribute("inventoryUpdate", new InventoryUpdateDto());
        model.addAttribute("activeMenu", "inventory");
        model.addAttribute("content", "admin/inventory/inventory-import :: import-form");
        model.addAttribute("pageTitle", "Nhập hàng mới");
        return "admin/layout";
    }

    @PostMapping("/update")
    public String updateInventory(@ModelAttribute InventoryUpdateDto inventoryUpdateDto) {
        inventoryService.updateStock(inventoryUpdateDto);
        return "redirect:/admin/inventory";
    }

    @GetMapping("/detail/{id}")
    public String shoeInventoryDetail(@PathVariable("id") Long inventoryId, Model model){
        InventoryResponseDto detail = inventoryService.getInventoryById(inventoryId);
        model.addAttribute("detail", detail);
        model.addAttribute("activeMenu", "inventory");
        return "admin/inventory/inventory-detail";
    }

    @PostMapping("/update-note")
    public String updateInventoryNote(
        @RequestParam("inventoryId") Long inventoryId,
        @RequestParam("note") String note
    ) {
        inventoryService.updateNoteOnly(inventoryId, note);
        return "redirect:/admin/inventory/detail/" + inventoryId;
    }

    public InventoryService getInventoryService() {
        return inventoryService;
    }

    @GetMapping("/api/variants/{shoeId}")
    @ResponseBody
    public ResponseEntity<List<Map<String, Object>>> getVariantsByShoeId(@PathVariable Long shoeId) {
        List<Map<String, Object>> variants = inventoryService.getVariantsForDropdown(shoeId);
        return ResponseEntity.ok(variants);
    }
    @PostMapping("/quick-update")
    public String quickUpdateStock(@RequestParam Long variantId,
                                  @RequestParam Long amount,
                                  @RequestParam String type,
                                  @RequestParam(required = false) String note) {
        inventoryService.updateVariantStock(variantId, amount, type, note);
        return "redirect:/admin/inventory";
    }
}

