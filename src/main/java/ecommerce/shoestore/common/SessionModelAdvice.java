package ecommerce.shoestore.common;

import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.ui.Model;

/**
 * ControllerAdvice để tự động thêm thông tin session vào tất cả các view
 * Không cần lặp lại code xử lý login ở mỗi controller
 */
@ControllerAdvice
public class SessionModelAdvice {

    @ModelAttribute
    public void addSessionAttributes(HttpSession session, Model model) {
        String fullname = (String) session.getAttribute("FULLNAME");
        
        if (fullname != null) {
            model.addAttribute("isLoggedIn", true);
            model.addAttribute("fullname", fullname);
            model.addAttribute("role", session.getAttribute("ROLE"));
            model.addAttribute("avatar", session.getAttribute("AVATAR"));
        } else {
            model.addAttribute("isLoggedIn", false);
        }
    }
}
