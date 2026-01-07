package ecommerce.shoestore.cart;

import ecommerce.shoestore.auth.user.User;
import ecommerce.shoestore.auth.user.UserRepository;
import lombok.RequiredArgsConstructor;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.security.core.Authentication;
import ecommerce.shoestore.cart.dto.CartSummaryView;

import jakarta.servlet.http.HttpServletRequest;

@Controller
@RequiredArgsConstructor
@RequestMapping("/cart")
public class CartController {

    private final CartService cartService;
    private final UserRepository userRepository;

    // ================== VIEW CART ==================
    @GetMapping
    public String viewCart(HttpSession session, Model model) {

        Long userId = (Long) session.getAttribute("USER_ID");
        if (userId == null) {
            return "redirect:/auth/login";
        }

        User customer = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        CartSummaryView cartView = cartService.getCartSummaryForView(customer);

        model.addAttribute("cartItems", cartView.items());
        model.addAttribute("cartSubtotal", cartView.subtotal());
        model.addAttribute("cartShipping", cartView.shipping());
        model.addAttribute("cartTotal", cartView.total());

        return "cart";
    }

    // ================== ADD ITEM ==================
    @PostMapping("/add")
    public String addToCart(
            HttpSession session,
            HttpServletRequest request,
            @RequestParam(required = false) Long variantId,
            @RequestParam int quantity,
            Authentication authentication,
            RedirectAttributes redirectAttributes
    ) {
        Long userId = (Long) session.getAttribute("USER_ID");
        if (userId == null) {
            return "redirect:/auth/login";
        }

        String referer = request.getHeader("Referer");

        if (variantId == null) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    "Vui lòng chọn kích thước trước khi thêm vào giỏ hàng"
            );
            return "redirect:" + referer;
        }

        User customer = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        cartService.addItem(customer, variantId, quantity);

        redirectAttributes.addFlashAttribute(
                "successMessage",
                "Đã thêm sản phẩm vào giỏ hàng thành công!"
        );

        return "redirect:" + referer;
    }

    // ================== UPDATE QUANTITY ==================
    @PostMapping("/update")
    public String updateQuantity(
            HttpSession session,
            @RequestParam Long cartItemId,
            @RequestParam String action,
            RedirectAttributes redirectAttributes
    ) {
        //Check login
        Long userId = (Long) session.getAttribute("USER_ID");
        if (userId == null) {
            return "redirect:/auth/login";
        }

        User customer = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        //Handle action increase/decrease
        if ("increase".equals(action)) {
            boolean increased = cartService.increaseQuantity(customer, cartItemId);
            if (!increased) {
                redirectAttributes.addFlashAttribute(
                        "errorMessage",
                        "Số lượng vượt quá tồn kho"
                );
            }

        } else if ("decrease".equals(action)) {
            cartService.decreaseQuantity(customer, cartItemId);
        }

        return "redirect:/cart";
    }
}
