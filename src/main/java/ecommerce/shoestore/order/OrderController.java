package ecommerce.shoestore.order;

import ecommerce.shoestore.auth.user.User;
import ecommerce.shoestore.auth.user.UserRepository;
import ecommerce.shoestore.cart.Cart;
import ecommerce.shoestore.cart.CartRepository;
import ecommerce.shoestore.promotion.CustomerPromotionService;
import ecommerce.shoestore.promotion.Voucher;
import ecommerce.shoestore.promotion.dto.VoucherValidationResult;
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
    private final OrderAddressService orderAddressService;
    private final UserRepository userRepository;
    private final CartRepository cartRepository;
    private final ShoesVariantRepository shoesVariantRepository;
    private final CustomerPromotionService customerPromotionService;
    
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
        
        // Lấy danh sách địa chỉ đã lưu của user
        List<OrderAddress> savedAddresses = orderAddressService.getUserAddresses(userId);
        model.addAttribute("savedAddresses", savedAddresses);
        
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
        return "shipping-info";
    }

    /**
     * Xử lý thông tin giao hàng và chuyển sang trang thanh toán
     * POST /order/shipping
     */
    @PostMapping("/shipping")
    public String submitShippingInfo(
            @RequestParam String type,
            @RequestParam(required = false) Long variantId,
            @RequestParam(required = false) Integer quantity,
            @RequestParam(required = false) Long savedAddressId,
            @RequestParam(required = false) String recipientName,
            @RequestParam(required = false) String recipientPhone,
            @RequestParam(required = false) String recipientEmail,
            @RequestParam(required = false) String province,
            @RequestParam(required = false) String district,
            @RequestParam(required = false) String commune,
            @RequestParam(required = false) String streetDetail,
            @RequestParam(required = false) String note,
            @RequestParam(required = false) boolean saveAddress,
            @RequestParam(required = false) boolean setAsDefault,
            HttpSession session,
            RedirectAttributes redirectAttributes) {
        
        Long userId = (Long) session.getAttribute("USER_ID");
        if (userId == null) {
            return "redirect:/auth/login";
        }
        
        Long addressId;
        
        // Nếu chọn địa chỉ có sẵn
        if (savedAddressId != null) {
            addressId = savedAddressId;
        } 
        // Nếu nhập địa chỉ mới
        else if (recipientName != null && !recipientName.isBlank()) {
            OrderAddress newAddress = new OrderAddress();
            newAddress.setUserId(userId);
            newAddress.setRecipientName(recipientName);
            newAddress.setRecipientPhone(recipientPhone);
            newAddress.setProvince(province);
            newAddress.setDistrict(district);
            newAddress.setCommune(commune);
            newAddress.setStreetDetail(streetDetail);
            // Luôn set default nếu user chọn
            newAddress.setIsDefault(setAsDefault);
            
            // Luôn lưu địa chỉ vào database để có thể hiển thị trong order confirmation
            // Chỉ khác là có set làm default hay không
            newAddress = orderAddressService.saveAddress(newAddress);
            
            addressId = newAddress.getAddressId();
        } else {
            redirectAttributes.addFlashAttribute("error", "Vui lòng chọn hoặc nhập địa chỉ giao hàng");
            return "redirect:/order/checkout?type=" + type + 
                   (variantId != null ? "&variantId=" + variantId + "&quantity=" + quantity : "");
        }
        
        // Lưu thông tin vào session
        session.setAttribute("SHIPPING_TYPE", type);
        session.setAttribute("SHIPPING_ADDRESS_ID", addressId);
        session.setAttribute("SHIPPING_RECIPIENT_EMAIL", recipientEmail);
        session.setAttribute("SHIPPING_NOTE", note);
        
        if ("BUY_NOW".equals(type)) {
            session.setAttribute("SHIPPING_VARIANT_ID", variantId);
            session.setAttribute("SHIPPING_QUANTITY", quantity);
        }
        
        // Redirect sang trang thanh toán
        return "redirect:/order/payment";
    }
    
    /**
     * API endpoint để thêm địa chỉ mới (AJAX)
     */
    @PostMapping("/address/add")
    @ResponseBody
    public OrderAddress addAddress(
            @RequestParam String recipientName,
            @RequestParam String recipientPhone,
            @RequestParam String province,
            @RequestParam String district,
            @RequestParam String commune,
            @RequestParam String streetDetail,
            @RequestParam(required = false) boolean setAsDefault,
            HttpSession session) {
        
        Long userId = (Long) session.getAttribute("USER_ID");
        if (userId == null) {
            throw new RuntimeException("User not logged in");
        }
        
        OrderAddress address = new OrderAddress();
        address.setUserId(userId);
        address.setRecipientName(recipientName);
        address.setRecipientPhone(recipientPhone);
        address.setProvince(province);
        address.setDistrict(district);
        address.setCommune(commune);
        address.setStreetDetail(streetDetail);
        address.setIsDefault(setAsDefault);
        
        return orderAddressService.saveAddress(address);
    }
    
    /**
     * API endpoint để xóa địa chỉ (AJAX)
     */
    @DeleteMapping("/address/{addressId}")
    @ResponseBody
    public void deleteAddress(
            @PathVariable Long addressId,
            HttpSession session) {
        
        Long userId = (Long) session.getAttribute("USER_ID");
        if (userId == null) {
            throw new RuntimeException("User not logged in");
        }
        
        orderAddressService.deleteAddress(addressId, userId);
    }
    
    /**
     * API endpoint để đặt địa chỉ mặc định (AJAX)
     */
    @PostMapping("/address/{addressId}/set-default")
    @ResponseBody
    public void setDefaultAddress(
            @PathVariable Long addressId,
            HttpSession session) {
        
        Long userId = (Long) session.getAttribute("USER_ID");
        if (userId == null) {
            throw new RuntimeException("User not logged in");
        }
        
        orderAddressService.setDefaultAddress(addressId, userId);
    }

    /**
     * API endpoint để validate voucher (AJAX)
     */
    @PostMapping("/voucher/validate")
    @ResponseBody
    public java.util.Map<String, Object> validateVoucher(
            @RequestParam String voucherCode,
            @RequestParam BigDecimal orderSubTotal,
            HttpSession session) {
        
        Long userId = (Long) session.getAttribute("USER_ID");
        if (userId == null) {
            throw new RuntimeException("User not logged in");
        }
        
        VoucherValidationResult result = customerPromotionService.validateVoucher(
                voucherCode, userId, orderSubTotal);
        
        java.util.Map<String, Object> response = new java.util.HashMap<>();
        response.put("valid", result.isValid());
        
        if (result.isValid()) {
            response.put("discountAmount", result.getDiscountAmount());
            response.put("message", String.format("Áp dụng thành công! Giảm %,.0f₫", result.getDiscountAmount()));
        } else {
            response.put("message", result.getErrorMessage());
        }
        
        return response;
    }
    
    /**
     * Hiển thị trang thanh toán và voucher
     * GET /order/payment
     */
    @GetMapping("/payment")
    public String showPaymentPage(HttpSession session, Model model, RedirectAttributes redirectAttributes) {
        
        Long userId = (Long) session.getAttribute("USER_ID");
        if (userId == null) {
            return "redirect:/auth/login";
        }
        
        // Kiểm tra có thông tin shipping không
        String type = (String) session.getAttribute("SHIPPING_TYPE");
        Long addressId = (Long) session.getAttribute("SHIPPING_ADDRESS_ID");
        
        if (type == null || addressId == null) {
            redirectAttributes.addFlashAttribute("error", "Vui lòng nhập thông tin giao hàng trước");
            return "redirect:/order/checkout?type=CART";
        }
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy thông tin người dùng"));
        
        // Lấy thông tin địa chỉ
        OrderAddress address = orderAddressService.getAddressById(addressId);
        if (address != null) {
            model.addAttribute("recipientName", address.getRecipientName());
            model.addAttribute("recipientPhone", address.getRecipientPhone());
            model.addAttribute("recipientAddress", address.getFullAddress());
        } else {
            // Fallback nếu không tìm thấy địa chỉ
            model.addAttribute("recipientName", "Không có thông tin");
            model.addAttribute("recipientPhone", "");
            model.addAttribute("recipientAddress", "Không có thông tin");
        }
        model.addAttribute("type", type);
        
        // Tính tổng tiền
        BigDecimal subtotal;
        BigDecimal shipping = new BigDecimal("30000");
        
        if ("CART".equals(type)) {
            Cart cart = cartRepository.findCartWithItems(user).orElse(null);
            if (cart == null || cart.getItems().isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Giỏ hàng trống!");
                return "redirect:/cart";
            }
            
            subtotal = cart.getItems().stream()
                    .map(item -> item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            model.addAttribute("cartItems", cart.getItems());
            model.addAttribute("variant", null);
            model.addAttribute("quantity", 0);
        } else {
            Long variantId = (Long) session.getAttribute("SHIPPING_VARIANT_ID");
            Integer quantity = (Integer) session.getAttribute("SHIPPING_QUANTITY");
            
            ShoesVariant variant = shoesVariantRepository.findByIdWithShoes(variantId)
                    .orElseThrow(() -> new RuntimeException("Sản phẩm không tồn tại"));
            
            subtotal = variant.getShoes().getBasePrice().multiply(BigDecimal.valueOf(quantity));
            
            model.addAttribute("variant", variant);
            model.addAttribute("quantity", quantity);
            model.addAttribute("cartItems", List.of());
        }
        
        model.addAttribute("subtotal", subtotal);
        model.addAttribute("shipping", shipping);
        model.addAttribute("total", subtotal.add(shipping));
        
        // Lấy danh sách voucher để hiển thị (bao gồm cả voucher không đủ điều kiện - màu xám)
        var vouchersForDisplay = customerPromotionService.getVouchersForDisplay(userId, subtotal);
        
        model.addAttribute("vouchers", vouchersForDisplay != null ? vouchersForDisplay : List.of());
        model.addAttribute("recipientEmail", session.getAttribute("SHIPPING_RECIPIENT_EMAIL") != null ? session.getAttribute("SHIPPING_RECIPIENT_EMAIL") : "");
        model.addAttribute("note", session.getAttribute("SHIPPING_NOTE") != null ? session.getAttribute("SHIPPING_NOTE") : "");
        
        return "payment";
    }
    
    /**
     * Xử lý tạo đơn hàng
     * POST /order/create
     */
    @PostMapping("/create")
    public String createOrder(
            @RequestParam String paymentMethod,
            @RequestParam(required = false) String voucherCode,
            HttpSession session,
            RedirectAttributes redirectAttributes) {
        
        System.out.println("\n===== CREATE ORDER REQUEST RECEIVED =====");
        
        try {
            Long userId = (Long) session.getAttribute("USER_ID");
            System.out.println("UserId from session: " + userId);
            
            if (userId == null) {
                return "redirect:/auth/login";
            }
            
            // Lấy thông tin từ session
            String type = (String) session.getAttribute("SHIPPING_TYPE");
            Long addressId = (Long) session.getAttribute("SHIPPING_ADDRESS_ID");
            String recipientEmail = (String) session.getAttribute("SHIPPING_RECIPIENT_EMAIL");
            String note = (String) session.getAttribute("SHIPPING_NOTE");
            
            System.out.println("Type: " + type);
            System.out.println("AddressId: " + addressId);
            System.out.println("PaymentMethod: " + paymentMethod);
            System.out.println("VoucherCode: " + voucherCode);
            
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy thông tin người dùng"));
            
            Order order;
            
            if ("CART".equals(type)) {
                // Tạo đơn từ giỏ hàng
                Cart cart = cartRepository.findCartWithItems(user)
                        .orElseThrow(() -> new RuntimeException("Giỏ hàng trống"));
                
                order = orderService.createOrderFromCart(
                        user.getUserId(), addressId, recipientEmail,
                        paymentMethod, note, cart, voucherCode
                );
                
            } else if ("BUY_NOW".equals(type)) {
                // Tạo đơn từ mua ngay
                Long variantId = (Long) session.getAttribute("SHIPPING_VARIANT_ID");
                Integer quantity = (Integer) session.getAttribute("SHIPPING_QUANTITY");
                
                order = orderService.createOrderBuyNow(
                        user.getUserId(), addressId, recipientEmail,
                        paymentMethod, note, variantId, quantity, voucherCode
                );
                
            } else {
                throw new RuntimeException("Loại đơn hàng không hợp lệ");
            }
            
            // Xóa session shipping data
            session.removeAttribute("SHIPPING_TYPE");
            session.removeAttribute("SHIPPING_ADDRESS_ID");
            session.removeAttribute("SHIPPING_RECIPIENT_EMAIL");
            session.removeAttribute("SHIPPING_NOTE");
            session.removeAttribute("SHIPPING_VARIANT_ID");
            session.removeAttribute("SHIPPING_QUANTITY");
            
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
        
        // Lấy thông tin địa chỉ
        if (order.getOrderAddressId() != null) {
            OrderAddress address = orderAddressService.getAddressById(order.getOrderAddressId());
            model.addAttribute("orderAddress", address);
        }
        
        model.addAttribute("order", order);
        model.addAttribute("orderItems", orderItems);
        
        return "order-confirmation";
    }
}
