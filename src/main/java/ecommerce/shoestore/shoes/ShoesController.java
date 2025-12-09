package ecommerce.shoestore.shoes;

import ecommerce.shoestore.shoes.dto.ShoesListDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
public class ShoesController {

    private final ShoesService shoesService;

    /**
     * Trang chủ - Hiển thị danh sách sản phẩm
     * URL: / hoặc /?category=MEN&page=1
     */
    @GetMapping("/")
    public String homePage(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "12") int size,
            @RequestParam(required = false) String category,
            Model model) {

        ShoesListDto data = shoesService.getShoesList(page, size, category);

        // Đẩy dữ liệu ra View
        model.addAttribute("products", data.getProducts());
        model.addAttribute("currentPage", data.getCurrentPage());
        model.addAttribute("totalPages", data.getTotalPages());
        model.addAttribute("totalItems", data.getTotalItems());
        model.addAttribute("currentCategory", category);

        return "index"; // ✅ File templates/index.html
    }

    /**
     * Trang chi tiết sản phẩm
     * URL: /product/{id}
     */
    @GetMapping("/product/{id}")
    public String productDetail(@PathVariable Long id, Model model) {
        model.addAttribute("product", shoesService.getShoesDetail(id));
        return "shoes-detail"; // ✅ ĐÚNG: File templates/shoes-detail.html
    }
}