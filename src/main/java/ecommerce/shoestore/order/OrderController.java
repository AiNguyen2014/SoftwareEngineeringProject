package ecommerce.shoestore.order;

import ecommerce.shoestore.auth.user.User;
import ecommerce.shoestore.auth.user.UserRepository;
import ecommerce.shoestore.cart.Cart;
import ecommerce.shoestore.cart.CartRepository;
import ecommerce.shoestore.shoesvariant.ShoesVariant;
import ecommerce.shoestore.shoesvariant.ShoesVariantRepository;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.List;

@Controller
@RequestMapping("/order")
@RequiredArgsConstructor
public class OrderController {
    
    private final OrderService orderService;
    private final UserRepository userRepository;
    private final CartRepository cartRepository;
    private final ShoesVariantRepository shoesVariantRepository;
    
    /**
     * Hiển thị trang checkout
     * GET /order/checkout?type=CART hoặc /order/checkout?type=BUY_NOW&variantId=1&quantity=2
     */
    @GetMapping("/checkout")
    public String showCheckoutPage(
            @RequestParam String type,
            @RequestParam(required = false) Long variantId,
            @RequestParam(required = false) Integer quantity,
            HttpSession session,
            Model model,
            RedirectAttributes redirectAttributes) {
        
        System.out.println("\n===== ORDER CHECKOUT DEBUG START =====");
        System.out.println("Request received at /order/checkout");
        System.out.println("Type parameter: " + type);
        System.out.println("VariantId: " + variantId);
        System.out.println("Quantity: " + quantity);
        System.out.println("Session ID: " + session.getId());
        
        // Kiểm tra đăng nhập qua session
        Long userId = (Long) session.getAttribute("USER_ID");
        System.out.println("Checking session attributes:");
        System.out.println("  - USER_ID: " + userId);
        System.out.println("  - FULLNAME: " + session.getAttribute("FULLNAME"));
        System.out.println("  - ROLE: " + session.getAttribute("ROLE"));
        
        if (userId == null) {
            System.out.println("❌ USER_ID is null - user not logged in");
            
            // Nếu là BUY_NOW, lưu redirect URL vào session để quay lại sau khi login
            if ("BUY_NOW".equals(type) && variantId != null && quantity != null) {
                String redirectUrl = String.format("/order/checkout?type=BUY_NOW&variantId=%d&quantity=%d", 
                        variantId, quantity);
                session.setAttribute("REDIRECT_AFTER_LOGIN", redirectUrl);
                System.out.println("💾 Saved redirect URL to session: " + redirectUrl);
                System.out.println("Verify saved: " + session.getAttribute("REDIRECT_AFTER_LOGIN"));
            }
            
            System.out.println("Redirecting to /auth/login");
            System.out.println("===== ORDER CHECKOUT DEBUG END =====");
            redirectAttributes.addFlashAttribute("message", "Vui lòng đăng nhập để tiếp tục");
            return "redirect:/auth/login";
        }
        
        System.out.println("✅ USER_ID found: " + userId + " - user is logged in");
        
        System.out.println("USER_ID found: " + userId + " - proceeding with checkout");
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy thông tin người dùng"));
        
        System.out.println("User found: " + user.getEmail());
        
        // Lấy thông tin để hiển thị trên form
        model.addAttribute("user", user);
        model.addAttribute("type", type);
        
        if ("CART".equals(type)) {
            System.out.println("Processing CART checkout");
            // Đặt hàng từ giỏ
            Cart cart = cartRepository.findCartWithItems(user).orElse(null);
            
            if (cart == null || cart.getItems().isEmpty()) {
                System.out.println("Cart is empty - redirecting to cart page");
                redirectAttributes.addFlashAttribute("error", "Giỏ hàng trống!");
                return "redirect:/cart";
            }
            
            System.out.println("Cart has " + cart.getItems().size() + " items");
            
            // Tính tổng tiền - sử dụng unitPrice đã lưu trong CartItem
            BigDecimal subtotal = cart.getItems().stream()
                    .map(item -> item.getUnitPrice()
                            .multiply(BigDecimal.valueOf(item.getQuantity())))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            System.out.println("Subtotal calculated: " + subtotal);
            
            model.addAttribute("cartItems", cart.getItems());
            model.addAttribute("subtotal", subtotal);
            model.addAttribute("shipping", new BigDecimal("30000"));
            model.addAttribute("total", subtotal.add(new BigDecimal("30000")));
            
        } else if ("BUY_NOW".equals(type)) {
            System.out.println("Processing BUY_NOW checkout");
            
            // Validate input
            if (variantId == null || quantity == null) {
                System.out.println("Invalid BUY_NOW parameters");
                redirectAttributes.addFlashAttribute("error", "Thông tin sản phẩm không hợp lệ!");
                return "redirect:/";
            }
            
            // Validate quantity
            if (quantity <= 0) {
                System.out.println("Invalid quantity: " + quantity);
                redirectAttributes.addFlashAttribute("error", "Số lượng phải lớn hơn 0!");
                return "redirect:/";
            }
            
            // Lấy thông tin variant với eager fetch Shoes entity
            ShoesVariant variant = shoesVariantRepository.findByIdWithShoes(variantId)
                    .orElseThrow(() -> new RuntimeException("Sản phẩm không tồn tại"));
            
            System.out.println("Variant found: " + variant.getShoes().getName() + " - Size: " + variant.getSize());
            
            BigDecimal subtotal = variant.getShoes().getBasePrice()
                    .multiply(BigDecimal.valueOf(quantity));
            
            System.out.println("BUY_NOW - Quantity: " + quantity + ", Subtotal: " + subtotal);
            
            model.addAttribute("variant", variant);
            model.addAttribute("quantity", quantity);
            model.addAttribute("subtotal", subtotal);
            model.addAttribute("shipping", new BigDecimal("30000"));
            model.addAttribute("total", subtotal.add(new BigDecimal("30000")));
            model.addAttribute("variantId", variantId);
        }
        
        System.out.println("Returning checkout template");
        System.out.println("===== ORDER CHECKOUT DEBUG END =====");
        return "checkout";
    }
    
    /**
     * Xử lý tạo đơn hàng
     * POST /order/create
     */
    @PostMapping("/create")
    public String createOrder(
            @RequestParam String type,
            @RequestParam(required = false) Long variantId,
            @RequestParam(required = false) Integer quantity,
            @RequestParam String recipientName,
            @RequestParam String recipientPhone,
            @RequestParam(required = false) String recipientEmail,
            @RequestParam String recipientAddress,
            @RequestParam String paymentMethod,
            @RequestParam(required = false) String note,
            HttpSession session,
            RedirectAttributes redirectAttributes) {
        
        System.out.println("\n===== CREATE ORDER REQUEST RECEIVED =====");
        System.out.println("Type: " + type);
        System.out.println("VariantId: " + variantId);
        System.out.println("Quantity: " + quantity);
        System.out.println("RecipientName: " + recipientName);
        System.out.println("RecipientPhone: " + recipientPhone);
        System.out.println("PaymentMethod: " + paymentMethod);
        
        try {
            Long userId = (Long) session.getAttribute("USER_ID");
            System.out.println("UserId from session: " + userId);
            
            if (userId == null) {
                return "redirect:/auth/login";
            }
            
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy thông tin người dùng"));
            
            Order order;
            
            if ("CART".equals(type)) {
                // Tạo đơn từ giỏ hàng
                Cart cart = cartRepository.findCartWithItems(user)
                        .orElseThrow(() -> new RuntimeException("Giỏ hàng trống"));
                
                order = orderService.createOrderFromCart(
                        user.getUserId(),
                        recipientName, recipientPhone, recipientEmail, recipientAddress,
                        paymentMethod, note, cart
                );
                
            } else if ("BUY_NOW".equals(type)) {
                // Tạo đơn từ mua ngay
                order = orderService.createOrderBuyNow(
                        user.getUserId(),
                        recipientName, recipientPhone, recipientEmail, recipientAddress,
                        paymentMethod, note,
                        variantId, quantity
                );
                
            } else {
                throw new RuntimeException("Loại đơn hàng không hợp lệ");
            }
            
            redirectAttributes.addFlashAttribute("message", "Đặt hàng thành công!");
            return "redirect:/order/confirmation/" + order.getOrderId();
            
        } catch (Exception e) {
            System.out.println("===== ORDER CREATION ERROR =====");
            System.out.println("Error message: " + e.getMessage());
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Đặt hàng thất bại: " + e.getMessage());
            return "redirect:/cart";
        }
    }
    
    /**
     * Hiển thị trang xác nhận đơn hàng
     * GET /order/confirmation/{orderId}
     */
    @GetMapping("/confirmation/{orderId}")
    public String showConfirmationPage(@PathVariable Long orderId, Model model, HttpSession session) {
        
        Long userId = (Long) session.getAttribute("USER_ID");
        if (userId == null) {
            return "redirect:/auth/login";
        }
        
        Order order = orderService.getOrderById(orderId);
        List<OrderItem> orderItems = orderService.getOrderItems(orderId);
        
        model.addAttribute("order", order);
        model.addAttribute("orderItems", orderItems);
        
        return "order-confirmation";
    }
}
