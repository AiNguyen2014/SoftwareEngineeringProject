package ecommerce.shoestore.shoes;

import ecommerce.shoestore.promotion.CustomerPromotionService;
import ecommerce.shoestore.promotion.PromotionCampaign;
import ecommerce.shoestore.review.Review;
import ecommerce.shoestore.review.ReviewRepository;
import ecommerce.shoestore.shoes.crud.ShoesSearchService;
import ecommerce.shoestore.shoes.dto.ShoesDetailDto;
import ecommerce.shoestore.shoes.dto.ShoesListDto;
import ecommerce.shoestore.category.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * Controller chính cho Shoes - chỉ chứa trang home và chi tiết sản phẩm
 * Các endpoint search/filter đã được tách ra ShoesSearchController
 */
@Controller
@RequiredArgsConstructor
public class ShoesController {

    private final ShoesService shoesService;
    private final ShoesSearchService shoesSearchService;
    private final CategoryRepository categoryRepository;
    private final CustomerPromotionService customerPromotionService;
    private final ReviewRepository reviewRepository;

    @GetMapping("/")
    public String homePage(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "12") int size,
            Model model) {
        // Session attributes (isLoggedIn, fullname, role, avatar)
        // được tự động thêm bởi SessionModelAdvice

        // Validate page: nếu < 1 thì chuyển về trang 1
        if (page < 1) {
            page = 1;
        }

        ShoesListDto data = shoesService.getShoesList(page, size);

        model.addAttribute("products", data.getProducts());
        model.addAttribute("currentPage", data.getCurrentPage());
        model.addAttribute("totalPages", data.getTotalPages());
        model.addAttribute("totalItems", data.getTotalItems());
        model.addAttribute("categories", categoryRepository.findAll());
        model.addAttribute("brands", shoesSearchService.findAllBrands(null));

        return "shoe/shoes-list";
    }

    @GetMapping("/product/{shoeId}")
    public String productDetail(@PathVariable Long shoeId, Model model) {
        // Session attributes được tự động thêm bởi SessionModelAdvice
        ShoesDetailDto product = shoesService.getShoesDetail(shoeId);
        model.addAttribute("product", product);

        // Lấy danh sách đánh giá thực tế từ database
        List<Review> reviews = reviewRepository.findByShoesIdWithDetails(shoeId);
        model.addAttribute("reviews", reviews);

        double averageRate = reviews.stream()
                .mapToInt(Review::getRate)
                .average()
                .orElse(0.0);
        model.addAttribute("averageRate", averageRate);
        
        // Lấy các campaign khuyến mãi đang áp dụng cho sản phẩm này
        List<PromotionCampaign> activeCampaigns = customerPromotionService.getActiveCampaignsForProduct(
                shoeId, product.getCategoryId());
        model.addAttribute("activeCampaigns", activeCampaigns);

        return "shoe/shoes-detail";
    }
}
