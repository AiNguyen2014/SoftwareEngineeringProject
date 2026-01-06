package ecommerce.shoestore.auth.user;

import ecommerce.shoestore.auth.email.EmailService;
import ecommerce.shoestore.auth.user.dto.ChangePasswordRequest;
import ecommerce.shoestore.auth.user.dto.UpdateProfileRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Random;

@Controller
@RequestMapping("/user/profile")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final EmailService emailService;

    @GetMapping
    public String viewProfile(HttpSession session, Model model) {

        Long userId = (Long) session.getAttribute("USER_ID");
        String email = (String) session.getAttribute("EMAIL");

        if (userId == null || email == null) {
            return "redirect:/auth/login";
        }

        try {
            UpdateProfileRequest profileData = userService.getProfileForDisplay(email);

            model.addAttribute("profile", profileData);
            return "user/profile";
        } catch (Exception e) {
            return "redirect:/auth/login";
        }
    }

    @PostMapping("/update")
    public String updateProfile(
            @Valid @ModelAttribute("profile") UpdateProfileRequest request,
            BindingResult bindingResult,
            HttpSession session,
            RedirectAttributes redirectAttributes,
            Model model) {

        String email = (String) session.getAttribute("EMAIL");
        if (email == null) return "redirect:/auth/login";

        // Nếu dữ liệu không hợp lệ quay lại trang profile  và hiển thị lỗi
        if (bindingResult.hasErrors()) {
            model.addAttribute("activeTab", "profile");
            return "user/profile";
        }

        try {
            userService.updateProfile(email, request);

            session.setAttribute("FULLNAME", request.getFullname());
            User updatedUser = userService.getCurrentUser(email);
            session.setAttribute("AVATAR", updatedUser.getAvatar());

            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật hồ sơ thành công!");
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi: " + e.getMessage());
        }

        return "redirect:/user/profile";
    }


    @PostMapping("/send-otp-password")
    @ResponseBody // Trả về JSON/String cho JavaScript xử lý
    public ResponseEntity<String> sendOtpForPasswordChange(HttpSession session) {
        String email = (String) session.getAttribute("EMAIL");
        if (email == null) {
            return ResponseEntity.status(401).body("Vui lòng đăng nhập lại.");
        }

        // Tạo mã OTP ngẫu nhiên
        String code = String.valueOf(new Random().nextInt(900000) + 100000);

        // Lưu OTP vào Session
        session.setAttribute("OTP_CODE", code);

        // Gửi email
        emailService.sendEmail(email, "Xác thực đổi mật khẩu", "Mã xác thực của bạn là: " + code);

        return ResponseEntity.ok("Đã gửi mã OTP thành công!");
    }

    @PostMapping("/change-password")
    public String changePassword(@ModelAttribute ChangePasswordRequest request,
                                 HttpSession session,
                                 RedirectAttributes redirectAttributes) {

        String email = (String) session.getAttribute("EMAIL");
        if (email == null) return "redirect:/auth/login";

        String sessionOtp = (String) session.getAttribute("OTP_CODE");
        String inputOtp = request.getOtp();

        if (sessionOtp == null || !sessionOtp.equals(inputOtp)) {
            redirectAttributes.addFlashAttribute("errorMessage", "Mã OTP không chính xác hoặc đã hết hạn!");
            redirectAttributes.addFlashAttribute("activeTab", "password");
            return "redirect:/user/profile";
        }

        session.removeAttribute("OTP_CODE");

        try {
            userService.changePassword(email, request);
            redirectAttributes.addFlashAttribute("successMessage", "Đổi mật khẩu thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        redirectAttributes.addFlashAttribute("activeTab", "password");
        return "redirect:/user/profile";
    }
}