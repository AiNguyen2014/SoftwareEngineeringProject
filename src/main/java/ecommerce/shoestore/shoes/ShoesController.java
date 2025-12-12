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
     * Trang chủ - Hiển thị TOÀN BỘ sản phẩm
     * CHỈ CÓ PAGINATION - KHÔNG CÓ FILTER
     *
     * URL: /
     * URL: /?page=2
     */
    @GetMapping("/")
    public String homePage(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "12") int size,
            Model model) {
        // Service trả về DTO đã chuẩn, không cần sửa gì ở đây
        ShoesListDto data = shoesService.getShoesList(page, size);

        // Đẩy dữ liệu ra View
        model.addAttribute("products", data.getProducts());
        model.addAttribute("currentPage", data.getCurrentPage());
        model.addAttribute("totalPages", data.getTotalPages());
        model.addAttribute("totalItems", data.getTotalItems());
        return "shoes-list";
    }

    /**
     * Trang chi tiết sản phẩm
     * URL: /product/{shoeId}
     * Sửa: {id} -> {shoeId} để đồng bộ với Backend
     */
    @GetMapping("/product/{shoeId}")
    public String productDetail(@PathVariable Long shoeId, Model model) {
        // Gọi Service với tham số shoeId
        model.addAttribute("product", shoesService.getShoesDetail(shoeId));
        return "shoes-detail";
    }
}