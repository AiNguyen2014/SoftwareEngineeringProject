package ecommerce.shoestore.admin;

import ecommerce.shoestore.auth.user.UserRepository;
import ecommerce.shoestore.order.OrderHistoryRepository;
import ecommerce.shoestore.promotion.PromotionCampaignRepository;
import ecommerce.shoestore.shoes.ShoesRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {
    
    private final ShoesRepository shoesRepository;
    private final OrderHistoryRepository orderRepository;
    private final PromotionCampaignRepository promotionCampaignRepository;
    private final UserRepository userRepository;

    @GetMapping
    public String dashboard(Model model) {
        // xác định menu đang active
        model.addAttribute("activeMenu", "dashboard");

        // Lấy thống kê thực tế
        Long totalProducts = shoesRepository.count();
        Long totalOrders = orderRepository.count();
        Long totalPromotions = promotionCampaignRepository.count();
        Long totalUsers = userRepository.count();

        // Thêm vào model
        model.addAttribute("totalProducts", totalProducts);
        model.addAttribute("totalOrders", totalOrders);
        model.addAttribute("totalPromotions", totalPromotions);
        model.addAttribute("totalUsers", totalUsers);

        // nội dung chính
        model.addAttribute("content", "admin/dashboard :: content");

        return "admin/layout";
    }
}
