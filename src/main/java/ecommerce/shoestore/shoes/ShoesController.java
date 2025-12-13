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

    @GetMapping("/")
    public String homePage(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "12") int size,
            Model model) {

        ShoesListDto data = shoesService.getShoesList(page, size);

        model.addAttribute("products", data.getProducts());
        model.addAttribute("currentPage", data.getCurrentPage());
        model.addAttribute("totalPages", data.getTotalPages());
        model.addAttribute("totalItems", data.getTotalItems());

        return "shoes-list";
    }

    @GetMapping("/product/{shoeId}")
    public String productDetail(@PathVariable Long shoeId, Model model) {
        model.addAttribute("product", shoesService.getShoesDetail(shoeId));
        return "shoes-detail";
    }
}