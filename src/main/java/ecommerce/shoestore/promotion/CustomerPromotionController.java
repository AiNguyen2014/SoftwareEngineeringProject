package ecommerce.shoestore.promotion;

import ecommerce.shoestore.promotion.dto.VoucherValidationResult;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Controller cho customer xem và sử dụng vouchers
 */
@Controller
@RequestMapping("/vouchers")
@RequiredArgsConstructor
@Slf4j
public class CustomerPromotionController {

    private final CustomerPromotionService customerPromotionService;

    /**
     * Hiển thị trang danh sách vouchers khả dụng cho customer
     */
    @GetMapping
    public String listVouchers(HttpSession session, Model model) {
        Long userId = (Long) session.getAttribute("USER_ID");
        
        // Lấy tất cả vouchers đang hoạt động (không filter theo minOrderValue)
        // để customer có thể xem tất cả vouchers hiện có
        List<Voucher> allVouchers = customerPromotionService.getAllActiveVouchers();
        
        model.addAttribute("vouchers", allVouchers);
        model.addAttribute("pageTitle", "Mã giảm giá");
        
        return "user/voucher-list";
    }
    
    /**
     * API lấy danh sách vouchers khả dụng cho giá trị đơn hàng cụ thể
     */
    @GetMapping("/api/available")
    @ResponseBody
    public ResponseEntity<List<Map<String, Object>>> getAvailableVouchers(
            @RequestParam(required = false, defaultValue = "0") BigDecimal orderSubTotal,
            HttpSession session) {
        
        Long userId = (Long) session.getAttribute("USER_ID");
        List<Voucher> vouchers = customerPromotionService.getAvailableVouchers(userId, orderSubTotal);
        
        List<Map<String, Object>> voucherList = vouchers.stream()
                .map(v -> {
                    Map<String, Object> voucherMap = new HashMap<>();
                    voucherMap.put("voucherId", v.getVoucherId());
                    voucherMap.put("code", v.getCode());
                    voucherMap.put("title", v.getTitle());
                    voucherMap.put("description", v.getDescription());
                    voucherMap.put("discountType", v.getDiscountType().name());
                    voucherMap.put("discountValue", v.getDiscountValue());
                    voucherMap.put("maxDiscountValue", v.getMaxDiscountValue());
                    voucherMap.put("minOrderValue", v.getMinOrderValue());
                    voucherMap.put("startDate", v.getStartDate().toString());
                    voucherMap.put("endDate", v.getEndDate().toString());
                    
                    // Tính số tiền giảm dự kiến
                    BigDecimal estimatedDiscount = customerPromotionService.calculateDiscount(v, orderSubTotal);
                    voucherMap.put("estimatedDiscount", estimatedDiscount);
                    
                    // Check xem voucher có đủ điều kiện áp dụng không
                    boolean applicable = v.getMinOrderValue() == null || 
                            orderSubTotal.compareTo(v.getMinOrderValue()) >= 0;
                    voucherMap.put("applicable", applicable);
                    
                    return voucherMap;
                })
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(voucherList);
    }
    
    /**
     * API validate và tính toán giảm giá cho voucher code
     */
    @PostMapping("/api/validate")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> validateVoucherCode(
            @RequestParam String voucherCode,
            @RequestParam BigDecimal orderSubTotal,
            HttpSession session) {
        
        log.info("=== Validating voucher code: {} for orderSubTotal: {} ===", voucherCode, orderSubTotal);
        
        Long userId = (Long) session.getAttribute("USER_ID");
        log.info("User ID from session: {}", userId);
        
        Map<String, Object> response = new HashMap<>();
        
        if (userId == null) {
            log.warn("User not logged in");
            response.put("valid", false);
            response.put("message", "Vui lòng đăng nhập để sử dụng mã giảm giá");
            return ResponseEntity.ok(response);
        }
        
        try {
            VoucherValidationResult result = customerPromotionService.validateVoucher(
                    voucherCode, userId, orderSubTotal);
            
            log.info("Validation result - valid: {}", result.isValid());
            
            response.put("valid", result.isValid());
            
            if (result.isValid()) {
                response.put("discountAmount", result.getDiscountAmount());
                response.put("voucherCode", result.getVoucher().getCode());
                response.put("voucherTitle", result.getVoucher().getTitle());
                response.put("discountType", result.getVoucher().getDiscountType().name());
                response.put("discountValue", result.getVoucher().getDiscountValue());
                response.put("message", String.format("Áp dụng thành công! Giảm %,.0f₫", result.getDiscountAmount()));
                log.info("Voucher applied successfully. Discount: {}", result.getDiscountAmount());
            } else {
                response.put("message", result.getErrorMessage());
                log.warn("Voucher validation failed: {}", result.getErrorMessage());
            }
        } catch (Exception e) {
            log.error("Error validating voucher: ", e);
            response.put("valid", false);
            response.put("message", "Lỗi hệ thống: " + e.getMessage());
        }
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * API xóa voucher đã chọn (reset)
     */
    @PostMapping("/api/remove")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> removeVoucher(HttpSession session) {
        session.removeAttribute("APPLIED_VOUCHER_CODE");
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Đã xóa mã giảm giá");
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * API kiểm tra số lần sử dụng voucher của user
     */
    @GetMapping("/api/usage/{voucherId}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getVoucherUsage(
            @PathVariable Long voucherId,
            HttpSession session) {
        
        Long userId = (Long) session.getAttribute("USER_ID");
        
        Map<String, Object> response = new HashMap<>();
        
        if (userId == null) {
            response.put("usageCount", 0);
            response.put("loggedIn", false);
        } else {
            long usageCount = customerPromotionService.countVoucherUsage(voucherId, userId);
            response.put("usageCount", usageCount);
            response.put("loggedIn", true);
        }
        
        return ResponseEntity.ok(response);
    }
}
