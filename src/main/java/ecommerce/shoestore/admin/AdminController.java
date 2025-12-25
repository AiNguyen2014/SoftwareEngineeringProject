package ecommerce.shoestore.admin;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @GetMapping
    public String dashboard(Model model) {
        // xác định menu đang active
        model.addAttribute("activeMenu", "dashboard");

        // nội dung chính
        model.addAttribute("content", "admin/dashboard :: content");

        return "admin/layout";
    }
}
