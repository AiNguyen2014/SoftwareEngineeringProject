package ecommerce.shoestore.common;

import ecommerce.shoestore.auth.user.User;
import ecommerce.shoestore.cart.CartRepository;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.ui.Model;

/**
 * ControllerAdvice để tự động thêm thông tin session vào tất cả các view
 * Không cần lặp lại code xử lý login ở mỗi controller
 */
@ControllerAdvice
@RequiredArgsConstructor
public class SessionModelAdvice {

    private final CartRepository cartRepository;

    @ModelAttribute
    public void addSessionAttributes(HttpSession session, Model model) {
        String fullname = (String) session.getAttribute("FULLNAME");
        
        if (fullname != null) {
            model.addAttribute("isLoggedIn", true);
            model.addAttribute("fullname", fullname);
            model.addAttribute("role", session.getAttribute("ROLE"));
            model.addAttribute("avatar", session.getAttribute("AVATAR"));
            
            // Thêm số lượng sản phẩm trong giỏ hàng
            Long userId = (Long) session.getAttribute("USER_ID");
            if (userId != null) {
                User user = new User();
                user.setUserId(userId);
                int cartItemCount = cartRepository.countItemsByUser(user);
                model.addAttribute("cartItemCount", cartItemCount);
            }
        } else {
            model.addAttribute("isLoggedIn", false);
            model.addAttribute("cartItemCount", 0);
        }
    }
}
