package ecommerce.shoestore.admin.promotion;

import ecommerce.shoestore.category.CategoryRepository;
import ecommerce.shoestore.promotion.*;
import ecommerce.shoestore.promotion.dto.CampaignForm;
import ecommerce.shoestore.promotion.dto.VoucherForm;
import ecommerce.shoestore.shoes.ShoesRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin/promotions")
@RequiredArgsConstructor
@Slf4j
public class PromotionAdminController {

    private final PromotionService promotionService;
    private final ShoesRepository shoesRepository;
    private final CategoryRepository categoryRepository;
    private final PromotionTargetRepository promotionTargetRepository;

    /* ===== Campaign ===== */
    @GetMapping("/campaigns")
    public String listCampaigns(@RequestParam(required = false) String keyword,
                                @RequestParam(required = false) String discountType,
                                @RequestParam(required = false) String status,
                                @RequestParam(required = false) Boolean enabled,
                                Model model) {
        model.addAttribute("activeMenu", "promotions");
        model.addAttribute("pageTitle", "Chiến dịch khuyến mãi");
        model.addAttribute("tab", "campaigns");
        model.addAttribute("keyword", keyword);
        model.addAttribute("discountType", discountType);
        model.addAttribute("status", status);
        model.addAttribute("enabled", enabled);
        model.addAttribute("campaigns", promotionService.searchCampaigns(keyword, discountType, status, enabled));
        return "admin/promotion/campaign-list";
    }

    @GetMapping("/campaigns/{id}")
    public String viewCampaign(@PathVariable Long id, Model model) {
        model.addAttribute("activeMenu", "promotions");
        model.addAttribute("campaign", promotionService.getCampaign(id));
        return "admin/promotion/campaign-detail";
    }

    @GetMapping({"/campaigns/create", "/campaigns/{id}/edit"})
    public String campaignForm(@PathVariable(name = "id", required = false) Long id, Model model) {
        CampaignForm form = new CampaignForm();
        if (id != null) {
            PromotionCampaign c = promotionService.getCampaign(id);
            form.setCampaignId(c.getCampaignId());
            form.setName(c.getName());
            form.setDescription(c.getDescription());
            form.setStartDate(c.getStartDate());
            form.setEndDate(c.getEndDate());
            form.setDiscountType(c.getDiscountType());
            form.setDiscountValue(c.getDiscountValue());
            form.setMaxDiscountAmount(c.getMaxDiscountAmount());
            form.setMinOrderValue(c.getMinOrderValue());
            form.setStatus(c.getStatus());
            form.setEnabled(c.getEnabled());
            
            // Load existing targets from campaign (already fetched)
            List<PromotionTarget> targets = c.getTargets();
            if (targets != null && !targets.isEmpty()) {
                ProductTargetType targetType = targets.get(0).getTargetType();
                form.setTargetType(targetType);
                
                if (targetType == ProductTargetType.PRODUCT) {
                    List<Long> shoeIds = targets.stream()
                            .filter(t -> t.getShoe() != null)
                            .map(t -> t.getShoe().getShoeId())
                            .collect(Collectors.toList());
                    form.setShoeIds(shoeIds);
                    model.addAttribute("selectedShoeIds", shoeIds);
                } else if (targetType == ProductTargetType.CATEGORY) {
                    List<Long> categoryIds = targets.stream()
                            .filter(t -> t.getCategory() != null)
                            .map(t -> t.getCategory().getCategoryId())
                            .collect(Collectors.toList());
                    form.setCategoryIds(categoryIds);
                    model.addAttribute("selectedCategoryIds", categoryIds);
                }
            }
        }
        
        model.addAttribute("activeMenu", "promotions");
        model.addAttribute("pageTitle", id == null ? "Tạo chiến dịch" : "Sửa chiến dịch");
        model.addAttribute("campaign", form);
        model.addAttribute("discountTypes", VoucherDiscountType.values());
        model.addAttribute("statuses", PromotionCampaignStatus.values());
        model.addAttribute("targetTypes", ProductTargetType.values());
        model.addAttribute("allShoes", shoesRepository.findAll());
        model.addAttribute("allCategories", categoryRepository.findAll());
        return "admin/promotion/campaign-form";
    }

    @PostMapping({"/campaigns/create", "/campaigns/{id}/edit"})
    public String saveCampaign(@PathVariable(name = "id", required = false) Long id,
                               @Valid @ModelAttribute("campaign") CampaignForm form,
                               @RequestParam(required = false) List<Long> shoeIds,
                               @RequestParam(required = false) List<Long> categoryIds,
                               BindingResult bindingResult,
                               Model model,
                               RedirectAttributes redirectAttributes) {
        if (id != null) {
            form.setCampaignId(id);
        }
        
        // Set target fields from request params
        form.setShoeIds(shoeIds);
        form.setCategoryIds(categoryIds);
        
        if (bindingResult.hasErrors()) {
            model.addAttribute("activeMenu", "promotions");
            model.addAttribute("pageTitle", id == null ? "Tạo chiến dịch" : "Sửa chiến dịch");
            model.addAttribute("discountTypes", VoucherDiscountType.values());
            model.addAttribute("statuses", PromotionCampaignStatus.values());
            model.addAttribute("targetTypes", ProductTargetType.values());
            model.addAttribute("allShoes", shoesRepository.findAll());
            model.addAttribute("allCategories", categoryRepository.findAll());
            return "admin/promotion/campaign-form";
        }
        try {
            promotionService.saveCampaign(form);
            redirectAttributes.addFlashAttribute("successMessage", "Lưu chiến dịch thành công");
            return "redirect:/admin/promotions/campaigns";
        } catch (Exception e) {
            log.error("Lỗi lưu chiến dịch", e);
            bindingResult.rejectValue(null, "error", e.getMessage());
            model.addAttribute("activeMenu", "promotions");
            model.addAttribute("pageTitle", id == null ? "Tạo chiến dịch" : "Sửa chiến dịch");
            model.addAttribute("discountTypes", VoucherDiscountType.values());
            model.addAttribute("statuses", PromotionCampaignStatus.values());
            model.addAttribute("targetTypes", ProductTargetType.values());
            model.addAttribute("allShoes", shoesRepository.findAll());
            model.addAttribute("allCategories", categoryRepository.findAll());
            model.addAttribute("error", e.getMessage());
            return "admin/promotion/campaign-form";
        }
    }

    @PostMapping("/campaigns/{id}/toggle")
    public String toggleCampaign(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            promotionService.toggleCampaignEnabled(id);
            redirectAttributes.addFlashAttribute("successMessage", "Đã cập nhật trạng thái chiến dịch");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/promotions/campaigns";
    }

    @PostMapping("/campaigns/{id}/delete")
    public String deleteCampaign(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            promotionService.deleteCampaign(id);
            redirectAttributes.addFlashAttribute("successMessage", "Đã xóa chiến dịch thành công");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi: " + e.getMessage());
        }
        return "redirect:/admin/promotions/campaigns";
    }

    /* ===== Voucher ===== */
    @GetMapping("/vouchers")
    public String listVouchers(@RequestParam(required = false) String keyword,
                               @RequestParam(required = false) Long campaignId,
                               @RequestParam(required = false) String discountType,
                               @RequestParam(required = false) Boolean enabled,
                               Model model) {
        model.addAttribute("activeMenu", "promotions");
        model.addAttribute("pageTitle", "Voucher");
        model.addAttribute("tab", "vouchers");
        model.addAttribute("keyword", keyword);
        model.addAttribute("campaignId", campaignId);
        model.addAttribute("discountType", discountType);
        model.addAttribute("enabled", enabled);
        model.addAttribute("campaigns", promotionService.listCampaigns());
        model.addAttribute("vouchers", promotionService.searchVouchers(keyword, campaignId, discountType, enabled));
        return "admin/promotion/voucher-list";
    }

    @GetMapping("/vouchers/{id}")
    public String viewVoucher(@PathVariable Long id, Model model) {
        model.addAttribute("activeMenu", "promotions");
        model.addAttribute("voucher", promotionService.getVoucher(id));
        return "admin/promotion/voucher-detail";
    }

    @GetMapping({"/vouchers/create", "/vouchers/{id}/edit"})
    public String voucherForm(@PathVariable(name = "id", required = false) Long id, Model model) {
        VoucherForm form = new VoucherForm();
        if (id != null) {
            Voucher v = promotionService.getVoucher(id);
            form.setVoucherId(v.getVoucherId());
            form.setCode(v.getCode());
            form.setTitle(v.getTitle());
            form.setDescription(v.getDescription());
            form.setDiscountType(v.getDiscountType());
            form.setDiscountValue(v.getDiscountValue());
            form.setMaxDiscountValue(v.getMaxDiscountValue());
            form.setMinOrderValue(v.getMinOrderValue());
            form.setStartDate(v.getStartDate());
            form.setEndDate(v.getEndDate());
            form.setMaxRedeemPerCustomer(v.getMaxRedeemPerCustomer());
            form.setEnabled(v.getEnabled());
            form.setCampaignId(v.getCampaign().getCampaignId());
        }
        model.addAttribute("activeMenu", "promotions");
        model.addAttribute("pageTitle", id == null ? "Tạo voucher" : "Sửa voucher");
        model.addAttribute("voucher", form);
        model.addAttribute("discountTypes", VoucherDiscountType.values());
        model.addAttribute("campaigns", promotionService.listCampaigns());
        return "admin/promotion/voucher-form";
    }

    @PostMapping({"/vouchers/create", "/vouchers/{id}/edit"})
    public String saveVoucher(@PathVariable(name = "id", required = false) Long id,
                              @Valid @ModelAttribute("voucher") VoucherForm form,
                              BindingResult bindingResult,
                              Model model,
                              RedirectAttributes redirectAttributes) {
        if (id != null) form.setVoucherId(id);
        if (bindingResult.hasErrors()) {
            model.addAttribute("activeMenu", "promotions");
            model.addAttribute("pageTitle", id == null ? "Tạo voucher" : "Sửa voucher");
            model.addAttribute("discountTypes", VoucherDiscountType.values());
            model.addAttribute("campaigns", promotionService.listCampaigns());
            return "admin/promotion/voucher-form";
        }
        try {
            promotionService.saveVoucher(form);
            redirectAttributes.addFlashAttribute("successMessage", "Lưu voucher thành công");
            return "redirect:/admin/promotions/vouchers";
        } catch (Exception e) {
            log.error("Lỗi lưu voucher", e);
            bindingResult.rejectValue(null, "error", e.getMessage());
            model.addAttribute("activeMenu", "promotions");
            model.addAttribute("pageTitle", id == null ? "Tạo voucher" : "Sửa voucher");
            model.addAttribute("discountTypes", VoucherDiscountType.values());
            model.addAttribute("campaigns", promotionService.listCampaigns());
            model.addAttribute("error", e.getMessage());
            return "admin/promotion/voucher-form";
        }
    }

    @PostMapping("/vouchers/{id}/toggle")
    public String toggleVoucher(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            promotionService.toggleVoucherEnabled(id);
            redirectAttributes.addFlashAttribute("successMessage", "Đã cập nhật trạng thái voucher");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/promotions/vouchers";
    }

    @PostMapping("/vouchers/{id}/delete")
    public String deleteVoucher(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            promotionService.deleteVoucher(id);
            redirectAttributes.addFlashAttribute("successMessage", "Đã xóa voucher");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/promotions/vouchers";
    }
}
