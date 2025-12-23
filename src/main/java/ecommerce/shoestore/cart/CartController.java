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
import ecommerce.shoestore.shoes.Shoes;
import ecommerce.shoestore.shoes.ShoesRepository;

import org.springframework.security.core.annotation.AuthenticationPrincipal;

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
            @RequestParam Long variantId,
            @RequestParam int quantity,
            Authentication authentication,
            RedirectAttributes redirectAttributes
    ) {
        Long userId = (Long) session.getAttribute("USER_ID");
        if (userId == null) {
            return "redirect:/auth/login";
        }

        User customer = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        cartService.addItem(customer, variantId, quantity);

        redirectAttributes.addFlashAttribute(
                "successMessage",
                "Đã thêm sản phẩm vào giỏ hàng thành công!"
        );
        return "redirect:/cart";
    }

    // ================== REMOVE ITEM ==================
    @PostMapping("/remove")
    public String removeItem(
            HttpSession session,
            @RequestParam Long cartItemId
    ) {
        Long userId = (Long) session.getAttribute("USER_ID");
        if (userId == null) {
            return "redirect:/auth/login";
        }

        User customer = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        cartService.removeItem(customer, cartItemId);

        return "redirect:/cart";
    }

    // ================== UPDATE QUANTITY ==================
    @PostMapping("/update")
    public String updateQuantity(
            HttpSession session,
            @RequestParam Long cartItemId,
            @RequestParam String action
    ) {
        Long userId = (Long) session.getAttribute("USER_ID");
        if (userId == null) {
            return "redirect:/auth/login";
        }

        User customer = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if ("increase".equals(action)) {
            cartService.increaseQuantity(customer, cartItemId);
        } else if ("decrease".equals(action)) {
            cartService.decreaseQuantity(customer, cartItemId);
        }
        return "redirect:/cart";
    }
}
