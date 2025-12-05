package ecommerce.shoestore.backend.controller;

import ecommerce.shoestore.backend.dto.ProductListDto;
import ecommerce.shoestore.backend.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @GetMapping("/")
    public String homePage(
            // Thêm tham số phân trang, mặc định trang 1, 12 sản phẩm/trang
            @RequestParam(name = "page", defaultValue = "1") int page,
            @RequestParam(name = "size", defaultValue = "12") int size,
            Model model) {

        // 1. Gọi đúng method trong Service (getProductList thay vì getAllProducts)
        ProductListDto productListDto = productService.getProductList(page, size);

        // 2. Bung dữ liệu từ DTO ra Model để index.html sử dụng
        model.addAttribute("products", productListDto.getProducts()); // Danh sách List<ProductSummaryDto>
        model.addAttribute("currentPage", productListDto.getCurrentPage());
        model.addAttribute("totalPages", productListDto.getTotalPages());
        model.addAttribute("totalItems", productListDto.getTotalItems());

        return "index"; // Trả về index.html
    }

    @GetMapping("/product/{id}")
    public String productDetail(@PathVariable Long id, Model model) {
        // Method này đã đúng
        model.addAttribute("product", productService.getProductDetail(id));
        return "product-detail"; // Trả về product-detail.html
    }
}