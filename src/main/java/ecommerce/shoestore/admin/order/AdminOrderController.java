package ecommerce.shoestore.admin.order;

import ecommerce.shoestore.order.OrderHistoryService;
import ecommerce.shoestore.order.OrderStatus;
import ecommerce.shoestore.order.dto.OrderHistoryDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/admin/orders")
@RequiredArgsConstructor
public class AdminOrderController {

    private final OrderHistoryService orderHistoryService;

    // Hiển thị danh sách đơn hàng
    @GetMapping
    public String showOrderList(Model model, 
                                @RequestParam(defaultValue = "0") int page,
                                @RequestParam(defaultValue = "10") int size,
                                @RequestParam(required = false) String status) {
        // 1. Lấy dữ liệu phân trang từ Service
        Page<OrderHistoryDto> orderPage = orderHistoryService.getAllOrders(status,page, size);
        
        // 2. Đưa dữ liệu ra HTML
        model.addAttribute("orders", orderPage.getContent()); // Danh sách đơn
        model.addAttribute("totalPages", orderPage.getTotalPages());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalElements", orderPage.getTotalElements()); // Tổng số đơn
        model.addAttribute("currentStatus", status != null ? status : "ALL");
        model.addAttribute("activeMenu", "orders");
        
        // Trả về file HTML mới (đẹp hơn)
        return "order/admin-order-list";
    }

    // API Cập nhật trạng thái (Giữ nguyên logic cũ)
    @PostMapping("/update-status")
    public String updateStatus(@RequestParam("orderId") Long orderId, 
                               @RequestParam("newStatus") String newStatusStr) { // Nhận String từ form
        try {
            // Logic cập nhật (bạn có thể tùy chỉnh người thay đổi là Admin đang login)
            orderHistoryService.addOrderStatusChange(orderId, "UNKNOWN", newStatusStr, "Admin", "Cập nhật bởi Admin");
        } catch (Exception e) {
            System.err.println("Lỗi cập nhật trạng thái: " + e.getMessage());
        }
        return "redirect:/admin/orders";
    }
}