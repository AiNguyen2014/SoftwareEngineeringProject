package ecommerce.shoestore.auth.user;

import ecommerce.shoestore.auth.dto.UserProfileDto;
import ecommerce.shoestore.auth.user.UserService;
import jakarta.servlet.http.HttpSession;

import java.util.Enumeration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    // Hiển thị trang hồ sơ
    @GetMapping("/profile")
    public String showProfile(HttpSession session, Model model) {
        Long userId = (Long) session.getAttribute("USER_ID");
        
        System.out.println("SESSION ATTRIBUTES:");
        Enumeration<String> attrs = session.getAttributeNames();
        while(attrs.hasMoreElements()) {
            String name = attrs.nextElement();
            System.out.println(" - " + name + " = " + session.getAttribute(name));
        }
        System.out.println("DEBUG PROFILE:");
        System.out.println(" - Đọc USER_ID từ Session: " + userId);;
    // --------------------------------

    // Check login
        if (userId == null) {
            System.out.println(">>> LỖI: Không tìm thấy ID -> Đá về Login");
            return "redirect:/auth/login";
        }

        // Check login
        // if (userId == null) {
        //     return "redirect:/auth/login";
        // }

        // Lấy thông tin user đưa vào model
        UserProfileDto profileDto = userService.getUserProfile(userId);
        
        // Cần truyền lại các thông tin Header (isLoggedIn...)
        model.addAttribute("isLoggedIn", true);
        model.addAttribute("fullname", session.getAttribute("FULLNAME"));
        model.addAttribute("role", session.getAttribute("ROLE"));
        
        model.addAttribute("profile", profileDto);
        return "user/profile"; // Trả về templates/user/profile.html
    }

    // Xử lý cập nhật
    @PostMapping("/profile/update")
    public String updateProfile(@ModelAttribute("profile") UserProfileDto request, 
                                HttpSession session, 
                                Model model) {
        Long userId = (Long) session.getAttribute("USER_ID");
        if (userId == null) return "redirect:/auth/login";

        try {
            userService.updateUserProfile(userId, request);
            
            // Cập nhật lại tên trong Session (nếu người dùng đổi tên)
            session.setAttribute("FULLNAME", request.getFullname());
            
            model.addAttribute("message", "Cập nhật hồ sơ thành công!");
        } catch (Exception e) {
            model.addAttribute("error", "Lỗi: " + e.getMessage());
        }

        // Load lại dữ liệu để hiển thị
        return showProfile(session, model);
    }
}