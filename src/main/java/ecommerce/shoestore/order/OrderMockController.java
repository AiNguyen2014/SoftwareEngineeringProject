package ecommerce.shoestore.order;

import ecommerce.shoestore.auth.account.UserRole;
import ecommerce.shoestore.order.dto.MockOrderDto;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Mock Controller để test giao diện Order History với dữ liệu giả
 * Route: /order/mock/...
 * KHÔNG tương tác với database, chỉ sử dụng mock data
 */
@Controller
@RequestMapping("/order/mock")
public class OrderMockController {
    
    @Autowired
    private OrderMockService mockService;
    
    /**
     * Test endpoint
     */
    @GetMapping("/test")
    @ResponseBody
    public String test() {
        return "Mock Controller working! Database disabled for UI testing.";
    }
    
    /**
     * Test root
     */
    @GetMapping("/")
    @ResponseBody
    public String root() {
        return "Order Mock System Ready - No Database Dependencies";
    }
    
    /**
     * [MOCK] Hiển thị lịch sử đơn hàng của customer với mock data
     */
    @GetMapping("/history")
    public String showMockOrderHistory(@RequestParam(defaultValue = "0") int page,
                                      @RequestParam(defaultValue = "5") int size,
                                      HttpSession session, Model model) {
        
        // Giả lập user đã login
        if (session.getAttribute("USER_ID") == null) {
            session.setAttribute("USER_ID", 1L);
            session.setAttribute("FULLNAME", "Nguyễn Văn A");
            session.setAttribute("EMAIL", "nguyenvana@email.com");
            session.setAttribute("ROLE", UserRole.CUSTOMER);
        }
        
        Long userId = (Long) session.getAttribute("USER_ID");
        
        try {
            // Lấy mock data - KHÔNG query database
            List<MockOrderDto> allOrders = mockService.getMockCustomerOrderHistory(userId);
            List<MockOrderDto> ordersForPage = mockService.getOrdersForPage(allOrders, page, size);
            int totalPages = mockService.calculateTotalPages(allOrders.size(), size);
            
            model.addAttribute("orders", ordersForPage);
            model.addAttribute("currentPage", page);
            model.addAttribute("totalPages", totalPages);
            model.addAttribute("totalElements", allOrders.size());
            model.addAttribute("hasNext", (page + 1) < totalPages);
            model.addAttribute("hasPrevious", page > 0);
            
            return "order/order-history";
            
        } catch (Exception e) {
            model.addAttribute("error", "Lỗi khi tải mock data: " + e.getMessage());
            return "order/order-history";
        }
    }
    
    /**
     * [MOCK] Xem chi tiết tracking log với mock data
     */
    @GetMapping("/tracking/{orderId}")
    public String showMockOrderTracking(@PathVariable Long orderId,
                                       HttpSession session, Model model) {
        
        // Giả lập user đã login
        if (session.getAttribute("USER_ID") == null) {
            session.setAttribute("USER_ID", 1L);
            session.setAttribute("FULLNAME", "Nguyễn Văn A");
            session.setAttribute("EMAIL", "nguyenvana@email.com");
            session.setAttribute("ROLE", UserRole.CUSTOMER);
        }
        
        try {
            MockOrderDto order = mockService.getMockOrderWithTrackingLogs(orderId);
            model.addAttribute("order", order);
            return "order/order-tracking";
            
        } catch (Exception e) {
            model.addAttribute("error", "Lỗi khi tải mock tracking data: " + e.getMessage());
            return "order/order-history";
        }
    }
    
    /**
     * [MOCK] Admin: Xem tất cả đơn hàng với mock data
     */
    @GetMapping("/admin/all")
    public String showMockAllOrdersForAdmin(@RequestParam(defaultValue = "0") int page,
                                           @RequestParam(defaultValue = "10") int size,
                                           HttpSession session, Model model) {
        
        // Giả lập admin login
        session.setAttribute("USER_ID", 99L);
        session.setAttribute("FULLNAME", "Admin Test");
        session.setAttribute("EMAIL", "admin@webshoe.com");
        session.setAttribute("ROLE", UserRole.ADMIN);
        
        try {
            // Lấy mock data - KHÔNG query database
            List<MockOrderDto> allOrders = mockService.getMockAllOrders();
            List<MockOrderDto> ordersForPage = mockService.getOrdersForPage(allOrders, page, size);
            int totalPages = mockService.calculateTotalPages(allOrders.size(), size);
            
            model.addAttribute("orders", ordersForPage);
            model.addAttribute("currentPage", page);
            model.addAttribute("totalPages", totalPages);
            model.addAttribute("totalElements", allOrders.size());
            model.addAttribute("hasNext", (page + 1) < totalPages);
            model.addAttribute("hasPrevious", page > 0);
            
            return "order/admin-order-list";
            
        } catch (Exception e) {
            model.addAttribute("error", "Lỗi khi tải mock data: " + e.getMessage());
            return "order/admin-order-list";
        }
    }
}