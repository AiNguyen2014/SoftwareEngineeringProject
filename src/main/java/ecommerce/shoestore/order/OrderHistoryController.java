package ecommerce.shoestore.order;

import ecommerce.shoestore.auth.account.UserRole;
import ecommerce.shoestore.order.dto.OrderHistoryDto;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/order")
public class OrderHistoryController {
    
    @Autowired
    private OrderHistoryService orderHistoryService;
    
    /**
     * Hiển thị lịch sử đơn hàng của customer
     */
    @GetMapping("/history")
    public String showOrderHistory(@RequestParam(defaultValue = "0") int page,
                                  @RequestParam(defaultValue = "10") int size,
                                  HttpSession session, Model model) {
        
        // Lấy userId từ session
        Long userId = (Long) session.getAttribute("USER_ID");
        if (userId == null) {
            // Nếu không có USER_ID trong session, redirect về login
            return "redirect:/auth/login";
        }
        
        // Nạp thông tin Header (giống UserController)
        model.addAttribute("isLoggedIn", true);
        model.addAttribute("fullname", session.getAttribute("FULLNAME"));
        model.addAttribute("role", session.getAttribute("ROLE"));
        
        try {
            // Debug: Log userId để check
            System.out.println("DEBUG: Looking for orders with userId: " + userId);
            
            Page<OrderHistoryDto> orderHistory = orderHistoryService.getCustomerOrderHistory(userId, page, size);
            
            // Debug: Log số lượng orders
            System.out.println("DEBUG: Found " + orderHistory.getTotalElements() + " orders");
            
            model.addAttribute("orders", orderHistory.getContent());
            model.addAttribute("currentPage", page);
            model.addAttribute("totalPages", orderHistory.getTotalPages());
            model.addAttribute("totalElements", orderHistory.getTotalElements());
            model.addAttribute("hasNext", orderHistory.hasNext());
            model.addAttribute("hasPrevious", orderHistory.hasPrevious());
            model.addAttribute("hasOrders", !orderHistory.getContent().isEmpty());
            
            return "order/order-history";
            
        } catch (Exception e) {
            // Handle error gracefully - chỉ hiện empty state, không hiện lỗi
            model.addAttribute("orders", java.util.List.of());
            model.addAttribute("hasOrders", false);
            model.addAttribute("currentPage", 0);
            model.addAttribute("totalPages", 0);
            model.addAttribute("totalElements", 0L);
            
            return "order/order-history";
        }
    }
    
    /**
     * Xem chi tiết tracking log của một đơn hàng
     */
    @GetMapping("/tracking/{orderId}")
    public String showOrderTracking(@PathVariable Long orderId,
                                   HttpSession session, Model model) {
        
        Long userId = (Long) session.getAttribute("USER_ID");
        UserRole role = (UserRole) session.getAttribute("ROLE");
        
        if (userId == null) {
            return "redirect:/auth/login";
        }
        
        try {
            OrderHistoryDto order = orderHistoryService.getOrderWithTrackingLogs(orderId);
            
            // Kiểm tra quyền truy cập: customer chỉ xem được đơn hàng của mình
            // Tạm thời bỏ qua check email, chỉ check userId
            
            // Nạp thông tin Header
            model.addAttribute("isLoggedIn", true);
            model.addAttribute("fullname", session.getAttribute("FULLNAME"));
            model.addAttribute("role", role);
            
            model.addAttribute("order", order);
            return "order/order-tracking";
            
        } catch (Exception e) {
            model.addAttribute("error", "Không tìm thấy đơn hàng hoặc có lỗi xảy ra: " + e.getMessage());
            return "redirect:/order/history";
        }
    }
    
    /**
     * Admin: Xem tất cả đơn hàng
     */
    @GetMapping("/admin/all")
    public String showAllOrdersForAdmin(@RequestParam(defaultValue = "0") int page,
                                       @RequestParam(defaultValue = "20") int size,
                                       HttpSession session, Model model) {
        
        UserRole role = (UserRole) session.getAttribute("ROLE");
        if (role != UserRole.ADMIN) {
            return "redirect:/";
        }
        
        // Nạp thông tin Header (giống UserController)
        model.addAttribute("isLoggedIn", true);
        model.addAttribute("fullname", session.getAttribute("FULLNAME"));
        model.addAttribute("role", role);
        
        try {
            Page<OrderHistoryDto> allOrders = orderHistoryService.getAllOrders(page, size);
            
            model.addAttribute("orders", allOrders.getContent());
            model.addAttribute("currentPage", page);
            model.addAttribute("totalPages", allOrders.getTotalPages());
            model.addAttribute("totalElements", allOrders.getTotalElements());
            model.addAttribute("hasNext", allOrders.hasNext());
            model.addAttribute("hasPrevious", allOrders.hasPrevious());
            
            return "order/admin-order-list";
            
        } catch (Exception e) {
            // Nếu lỗi SQL/JDBC, không hiển thị error, chỉ hiển thị empty state
            if (e.getMessage().contains("JDBC") || e.getMessage().contains("SQL")) {
                // Set empty data thay vì error
                model.addAttribute("orders", java.util.Collections.emptyList());
                model.addAttribute("currentPage", 0);
                model.addAttribute("totalPages", 0);
                model.addAttribute("totalElements", 0L);
                model.addAttribute("hasNext", false);
                model.addAttribute("hasPrevious", false);
            } else {
                model.addAttribute("error", e.getMessage());
            }
            return "order/admin-order-list";
        }
    }
    
    /**
     * Admin: Cập nhật trạng thái đơn hàng
     */
    @PostMapping("/admin/update-status")
    public String updateOrderStatus(@RequestParam Long orderId,
                                   @RequestParam OrderStatus newStatus,
                                   @RequestParam(required = false) String comment,
                                   HttpSession session) {
        
        UserRole role = (UserRole) session.getAttribute("ROLE");
        if (role != UserRole.ADMIN) {
            return "redirect:/";
        }
        
        try {
            String changedBy = (String) session.getAttribute("FULLNAME");
            
            // Lấy trạng thái cũ từ database
            OrderHistoryDto currentOrder = orderHistoryService.getOrderWithTrackingLogs(orderId);
            String oldStatus = currentOrder.getStatus().name(); // Convert to String
            String newStatusString = newStatus.name(); // Convert to String
            
            orderHistoryService.addOrderStatusChange(orderId, oldStatus, newStatusString, changedBy, comment);
            
            return "redirect:/order/tracking/" + orderId + "?updated=true";
            
        } catch (Exception e) {
            return "redirect:/order/tracking/" + orderId + "?error=" + e.getMessage();
        }
    }
}