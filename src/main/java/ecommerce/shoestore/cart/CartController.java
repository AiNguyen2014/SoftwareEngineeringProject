package ecommerce.shoestore.cart;

import ecommerce.shoestore.auth.user.User;
import ecommerce.shoestore.shoes.Shoes;
import ecommerce.shoestore.shoes.ShoesRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.ui.Model;

@Controller
@RequiredArgsConstructor
@RequestMapping("/cart")

public class CartController {
    
    private final CartService cartService;
    private final ShoesRepository shoesRepository;

    // Xem giỏ hàng
    @GetMapping
    public String viewCart(
            @AuthenticationPrincipal User customer,
            Model model
    ) {
        if (customer == null) {
        return "redirect:/login";
        }

        Cart cart = cartService.getOrCreateCart(customer);
        model.addAttribute("cart", cart);
        return "cart";
    }

    // Thêm sản phẩm vào giỏ
    @PostMapping("/add")
    public String addToCart(
            @AuthenticationPrincipal User customer,
            @RequestParam Long shoeId,
            @RequestParam int quantity
    ) {
        Shoes shoes = shoesRepository.findById(shoeId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm phù hợp"));

        cartService.addItem(customer, shoes, quantity);
        return "redirect:/cart";
    }

    // Xóa 1 item khỏi giỏ
    @PostMapping("/remove")
    public String removeItem(
            @AuthenticationPrincipal User customer,
            @RequestParam Long shoeId
    ) {
        cartService.removeItem(customer, shoeId);
        return "redirect:/cart";
    }
}
