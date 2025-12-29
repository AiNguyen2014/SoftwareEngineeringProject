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

    @GetMapping
    public String showOrderList(Model model, 
                                @RequestParam(defaultValue = "0") int page,
                                @RequestParam(defaultValue = "10") int size,
                                @RequestParam(required = false) String status,
                                @RequestParam(required = false) String searchType,
                                @RequestParam(required = false) String searchKeyword) {
        
        Page<OrderHistoryDto> orderPage;
        
        // Nếu có search thì dùng search, không thì dùng filter status thông thường
        if (searchKeyword != null && !searchKeyword.trim().isEmpty() && 
            searchType != null && !searchType.trim().isEmpty()) {
            orderPage = orderHistoryService.searchOrders(searchType, searchKeyword, page, size);
        } else {
            orderPage = orderHistoryService.getAllOrders(status, page, size);
        }
        
        if (page > orderPage.getTotalPages() && orderPage.getTotalPages() > 0) {
            StringBuilder redirectUrl = new StringBuilder("redirect:/admin/orders?page=" + (orderPage.getTotalPages() - 1));
            if (status != null && !status.trim().isEmpty()) {
                redirectUrl.append("&status=").append(status);
            }
            if (searchKeyword != null && !searchKeyword.trim().isEmpty()) {
                redirectUrl.append("&searchType=").append(searchType)
                          .append("&searchKeyword=").append(searchKeyword);
            }
            return redirectUrl.toString();
        }
        
        model.addAttribute("orders", orderPage.getContent());
        model.addAttribute("totalPages", orderPage.getTotalPages());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalElements", orderPage.getTotalElements());
        model.addAttribute("currentStatus", status != null ? status : "ALL");
        model.addAttribute("searchType", searchType);
        model.addAttribute("searchKeyword", searchKeyword);
        model.addAttribute("activeMenu", "orders");
        
        long pendingCount = orderHistoryService.countOrdersByStatus("PENDING");
        long completedCount = orderHistoryService.countOrdersByStatus("COMPLETED");
        long cancelledCount = orderHistoryService.countOrdersByStatus("CANCELLED");
        
        model.addAttribute("pendingCount", pendingCount);
        model.addAttribute("completedCount", completedCount);
        model.addAttribute("cancelledCount", cancelledCount);
        
        return "order/admin-order-list";
    }

    @PostMapping("/update-status")
    public String updateStatus(@RequestParam("orderId") Long orderId, 
                               @RequestParam("newStatus") String newStatusStr) {
        String currentStatus = orderHistoryService.getCurrentOrderStatus(orderId);
        orderHistoryService.addOrderStatusChange(orderId, currentStatus, newStatusStr, "Chủ cửa hàng", "Cập nhật bởi chủ cửa hàng");
        return "redirect:/admin/orders";
    }
}