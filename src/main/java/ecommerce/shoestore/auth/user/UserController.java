package ecommerce.shoestore.auth.user;

import ecommerce.shoestore.auth.dto.UserProfileDto;
// Import Service Upload (Hãy chắc chắn bạn đã tạo file này ở bước trước)
import ecommerce.shoestore.auth.image.FileUploadService; 

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid; // Import Validate
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile; // Import để nhận file

import java.util.Enumeration;

@Controller
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private FileUploadService fileUploadService; // 1. Inject Service Upload

    // --- 1. HIỂN THỊ HỒ SƠ ---
    @GetMapping("/profile")
    public String showProfile(HttpSession session, Model model) {
        Long userId = (Long) session.getAttribute("USER_ID");
        
        // --- Debug Session (Giữ lại để bạn kiểm tra lỗi) ---
        System.out.println("DEBUG PROFILE:");
        System.out.println(" - Đọc USER_ID từ Session: " + userId);
        if (userId == null) {
            System.out.println("SESSION ATTRIBUTES HIỆN CÓ:");
            Enumeration<String> attrs = session.getAttributeNames();
            while(attrs.hasMoreElements()) {
                String name = attrs.nextElement();
                System.out.println(" - " + name + " = " + session.getAttribute(name));
            }
            System.out.println(">>> LỖI: Không tìm thấy ID -> Đá về Login");
            return "redirect:/auth/login";
        }
        // ---------------------------------------------------

        // Lấy thông tin user đưa vào model
        UserProfileDto profileDto = userService.getUserProfile(userId);
        
        // Nạp thông tin Header
        model.addAttribute("isLoggedIn", true);
        model.addAttribute("fullname", session.getAttribute("FULLNAME"));
        model.addAttribute("role", session.getAttribute("ROLE"));
        
        // Đưa DTO vào form
        model.addAttribute("profile", profileDto);
        
        return "user/profile"; 
    }

    // --- 2. XỬ LÝ CẬP NHẬT (CÓ ẢNH) ---
    @PostMapping("/profile/update")
    public String updateProfile(@Valid @ModelAttribute("profile") UserProfileDto request,
                                BindingResult result, // Biến chứa lỗi validate
                                @RequestParam("avatarFile") MultipartFile file, // 2. Nhận file ảnh từ form
                                HttpSession session, 
                                Model model) {
        
        Long userId = (Long) session.getAttribute("USER_ID");
        if (userId == null) return "redirect:/auth/login";

        // 3. Nếu dữ liệu nhập sai (VD: bỏ trống tên) -> Trả về form báo lỗi
        if (result.hasErrors()) {
            // Nạp lại header để giao diện không bị vỡ
            model.addAttribute("isLoggedIn", true);
            model.addAttribute("fullname", session.getAttribute("FULLNAME"));
            model.addAttribute("role", session.getAttribute("ROLE"));
            return "user/profile";
        }

        try {
            // 4. Xử lý Upload ảnh (Nếu người dùng có chọn file)
            if (!file.isEmpty()) {
                // Upload lên Cloudinary và lấy link URL
                String imageURL = fileUploadService.uploadFile(file);
                // Gán link vào DTO
                request.setAvatar(imageURL);

                session.setAttribute("AVATAR", imageURL);
            }

            // 5. Gọi Service lưu vào DB
            userService.updateUserProfile(userId, request);
            
            // Cập nhật lại tên trong Session (phòng trường hợp đổi tên)
            session.setAttribute("FULLNAME", request.getFullname());
            
            model.addAttribute("message", "Cập nhật hồ sơ thành công!");
        } catch (Exception e) {
            e.printStackTrace(); // In lỗi ra terminal để dễ sửa
            model.addAttribute("error", "Lỗi: " + e.getMessage());
        }

        // Load lại trang profile
        return showProfile(session, model);
    }
}