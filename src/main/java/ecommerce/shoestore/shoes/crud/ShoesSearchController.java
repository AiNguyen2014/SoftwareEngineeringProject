package ecommerce.shoestore.shoes.crud;

import ecommerce.shoestore.category.CategoryRepository;
import ecommerce.shoestore.shoes.ShoesType;
import ecommerce.shoestore.shoes.dto.ShoesListDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.math.BigDecimal;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class ShoesSearchController {

    private final ShoesSearchService shoesSearchService;
    private final CategoryRepository categoryRepository;

    /**
     * API GỢI Ý TÌM KIẾM (Autocomplete, trả về JSON)
     */
    @GetMapping("/api/search-suggestions")
    @ResponseBody
    public List<String> searchSuggestions(@RequestParam String keyword) {
        if (keyword == null || keyword.trim().length() < 2) {
            return List.of("Nhập ít nhất 2 ký tự để gợi ý");
        }

        List<String> suggestions = shoesSearchService.getSearchSuggestions(keyword);

        if (suggestions.isEmpty()) {
            return List.of("Không có gợi ý nào");
        }

        return suggestions;
    }

    /**
     * Trang danh sách sản phẩm với filter và search
     */
    @GetMapping("/products")
    public String listProducts(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String gender,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String brand,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(defaultValue = "name_asc") String sort,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "12") int size,
            Model model
    ) {
        ShoesType shoesType = parseGender(gender);

        // Hiển thị tất cả category (kể cả chưa có sản phẩm)
        model.addAttribute("categories", categoryRepository.findAllCategories());

        model.addAttribute("brands",
                shoesSearchService.findAllBrands(shoesType));

        String type = shoesType != null ? shoesType.name() : null;

        ShoesListDto data = shoesSearchService.searchProducts(
                keyword,
                categoryId,
                brand,
                type,
                minPrice,
                maxPrice,
                page,
                size,
                sort
        );

        model.addAttribute("products", data.getProducts());
        model.addAttribute("currentPage", data.getCurrentPage());
        model.addAttribute("totalPages", data.getTotalPages());
        model.addAttribute("totalItems", data.getTotalItems());
        model.addAttribute("resultCount", data.getTotalSearchResults());

        // GIỮ TRẠNG THÁI FILTER
        model.addAttribute("keyword", keyword);
        model.addAttribute("gender", gender);
        model.addAttribute("categoryId", categoryId);
        model.addAttribute("brand", brand);
        model.addAttribute("minPrice", minPrice);
        model.addAttribute("maxPrice", maxPrice);
        model.addAttribute("sort", sort);

        if (data.getTotalItems() == 0) {
            model.addAttribute("errorMessage", "Không tìm thấy sản phẩm phù hợp");
        }

        return "shoe/shoes-list";
    }

    /**
     * Parse gender string thành ShoesType enum
     */
    private ShoesType parseGender(String gender) {
        if (gender == null || gender.isBlank()) return null;

        return switch (gender.toLowerCase()) {
            case "male" -> ShoesType.FOR_MALE;
            case "female" -> ShoesType.FOR_FEMALE;
            case "unisex" -> ShoesType.FOR_UNISEX;
            default -> null;
        };
    }
}
