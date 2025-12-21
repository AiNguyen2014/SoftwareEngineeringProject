package ecommerce.shoestore.shoes;

import ecommerce.shoestore.shoes.dto.ShoesListDto;
import ecommerce.shoestore.category.CategoryService;
import ecommerce.shoestore.category.CategoryRepository;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import java.util.List;
import java.math.BigDecimal;

@Controller
@RequiredArgsConstructor
public class ShoesController {

    private final ShoesService shoesService;
    private final CategoryService categoryService;
    private final CategoryRepository categoryRepository;

    @GetMapping("/")
    public String homePage(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "12") int size,
            Model model, HttpSession session) {
        
        // Quốc thêm cho phần login xong thì hiện ra trang chủ
        String fullname = (String) session.getAttribute("FULLNAME");
        if (fullname != null) {
            model.addAttribute("isLoggedIn", true);
            model.addAttribute("fullname", fullname);
            model.addAttribute("role", session.getAttribute("ROLE"));

            model.addAttribute("avatar", session.getAttribute("AVATAR"));
        } else {
            model.addAttribute("isLoggedIn", false);
        }

        ShoesListDto data = shoesService.getShoesList(page, size);

        model.addAttribute("products", data.getProducts());
        model.addAttribute("currentPage", data.getCurrentPage());
        model.addAttribute("totalPages", data.getTotalPages());
        model.addAttribute("totalItems", data.getTotalItems());
        model.addAttribute("categories", categoryRepository.findAll());
        model.addAttribute("brands", shoesService.findAllBrands(null));

        return "shoes-list";
    }

    @GetMapping("/product/{shoeId}")
    public String productDetail(@PathVariable Long shoeId, Model model,HttpSession session) {

        // Quốc thêm cho phần login
        String fullname = (String) session.getAttribute("FULLNAME");
        if (fullname != null) {
            model.addAttribute("isLoggedIn", true);
            model.addAttribute("fullname", fullname);
            model.addAttribute("role", session.getAttribute("ROLE"));

            model.addAttribute("avatar", session.getAttribute("AVATAR"));
        } else {
            model.addAttribute("isLoggedIn", false);
        }

        model.addAttribute("product", shoesService.getShoesDetail(shoeId));
        return "shoes-detail";
    }

    // API GỢI Ý TÌM KIẾM (Autocomplete, trả về JSON)
    @GetMapping("/api/search-suggestions")
    @ResponseBody
    public List<String> searchSuggestions(@RequestParam String keyword) {

        if (keyword == null || keyword.trim().length() < 2) {
            return List.of("Nhập ít nhất 2 ký tự để gợi ý");
        }

        List<String> suggestions = shoesService.getSearchSuggestions(keyword);

        if (suggestions.isEmpty()) {
            return List.of("Không có gợi ý nào");
        }

        return suggestions;
    }

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
        model.addAttribute("categories",
                categoryRepository.findCategoriesByShoesType(shoesType));
        model.addAttribute("brands",
                shoesService.findAllBrands(shoesType));


        ShoesListDto data = shoesService.searchProducts(
                keyword,
                categoryId,
                brand,
                shoesType,
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

        // GIỮ TRẠNG THÁI
        model.addAttribute("keyword", keyword);
        model.addAttribute("gender", gender);
        model.addAttribute("categoryId", categoryId);
        model.addAttribute("minPrice", minPrice);
        model.addAttribute("maxPrice", maxPrice);
        model.addAttribute("sort", sort);

        if (data.getTotalItems() == 0) {
            model.addAttribute("errorMessage", "Không tìm thấy sản phẩm phù hợp");
        }

        return "shoes-list";
    }

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