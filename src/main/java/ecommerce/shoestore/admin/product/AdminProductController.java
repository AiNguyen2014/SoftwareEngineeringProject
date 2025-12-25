package ecommerce.shoestore.admin.product;

import ecommerce.shoestore.admin.product.dto.AdminShoesDetailDto;
import ecommerce.shoestore.admin.product.dto.AdminShoesListItemDto;
import ecommerce.shoestore.admin.product.dto.CreateShoesRequest;
import ecommerce.shoestore.admin.product.dto.UpdateShoesRequest;
import ecommerce.shoestore.category.CategoryRepository;
import ecommerce.shoestore.common.NotFoundException;
import ecommerce.shoestore.shoes.ShoesType;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/products")
@RequiredArgsConstructor
@Slf4j
public class AdminProductController {

    private final AdminProductService adminProductService;
    private final CategoryRepository categoryRepository;

    /**
     * Danh sách sản phẩm + phân trang + lọc
     */
    @GetMapping
    public String listProducts(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String brand,
            @RequestParam(required = false) String status,
            Model model
    ) {

        Page<AdminShoesListItemDto> productPage
                = adminProductService.getAdminProductList(page, size, keyword, categoryId, brand, status);

        if (page > productPage.getTotalPages() && productPage.getTotalPages() > 0) {
            return "redirect:/admin/products?page=" + productPage.getTotalPages();
        }

        model.addAttribute("productsPage", productPage);
        model.addAttribute("products", productPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", productPage.getTotalPages());
        model.addAttribute("keyword", keyword);
        model.addAttribute("categoryId", categoryId);
        model.addAttribute("brand", brand);
        model.addAttribute("status", status);
        model.addAttribute("categories", categoryRepository.findAllByOrderByNameAsc());
        return "admin/product/list";
    }

    /**
     * Mở form thêm sản phẩm
     */
    @GetMapping("/create")
    public String showCreateForm(Model model) {
        model.addAttribute("categories", categoryRepository.findAll());
        model.addAttribute("shoesTypes", ShoesType.values());
        model.addAttribute("request", new CreateShoesRequest());
        return "admin/product/create";
    }

    /**
     * Xử lý submit thêm sản phẩm
     */
    @PostMapping("/create")
    public String createProduct(
            @Valid @ModelAttribute("request") CreateShoesRequest request,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("categories", categoryRepository.findAll());
            model.addAttribute("shoesTypes", ShoesType.values());
            model.addAttribute("error", "Dữ liệu không hợp lệ. Vui lòng kiểm tra lại.");
            return "admin/product/create";
        }

        try {
            adminProductService.createShoes(request);
            redirectAttributes.addFlashAttribute("successMessage", "Thêm sản phẩm thành công");
            return "redirect:/admin/products";
        } catch (Exception e) {
            log.error("Lỗi khi tạo sản phẩm", e);
            model.addAttribute("categories", categoryRepository.findAll());
            model.addAttribute("shoesTypes", ShoesType.values());
            model.addAttribute("error", e.getMessage());
            return "admin/product/create";
        }
    }

    /**
     * Mở form chỉnh sửa sản phẩm
     */
    @GetMapping("/{id}/edit")
    public String showEditForm(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        try {
            AdminShoesDetailDto shoesDetail = adminProductService.getAdminShoesDetail(id);
            model.addAttribute("categories", categoryRepository.findAll());
            model.addAttribute("shoesTypes", ShoesType.values());
            model.addAttribute("shoes", shoesDetail);
            return "admin/product/edit";
        } catch (NotFoundException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/admin/products";
        }
    }

    /**
     * Xử lý submit chỉnh sửa sản phẩm
     */
    @PostMapping("/{id}/edit")
    public String updateProduct(
            @PathVariable Long id,
            @Valid @ModelAttribute("request") UpdateShoesRequest request,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("categories", categoryRepository.findAll());
            model.addAttribute("shoesTypes", ShoesType.values());
            try {
                model.addAttribute("shoes", adminProductService.getAdminShoesDetail(id));
            } catch (Exception ignored) {
                // keep model minimal if not found
            }
            model.addAttribute("error", "Dữ liệu không hợp lệ. Vui lòng kiểm tra lại.");
            return "admin/product/edit";
        }

        try {
            adminProductService.updateShoes(id, request);
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật sản phẩm thành công");
            return "redirect:/admin/products";
        } catch (Exception e) {
            log.error("Lỗi khi cập nhật sản phẩm {}", id, e);
            model.addAttribute("categories", categoryRepository.findAll());
            model.addAttribute("shoesTypes", ShoesType.values());
            try {
                model.addAttribute("shoes", adminProductService.getAdminShoesDetail(id));
            } catch (Exception ignored) {
            }
            model.addAttribute("error", e.getMessage());
            return "admin/product/edit";
        }
    }

    /**
     * Toggle trạng thái sản phẩm
     */
    @PostMapping("/{id}/toggle-status")
    public String toggleProductStatus(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            adminProductService.toggleStatus(id);
            redirectAttributes.addFlashAttribute("successMessage", "Đã cập nhật trạng thái sản phẩm");
        } catch (Exception e) {
            log.error("Lỗi khi thay đổi trạng thái sản phẩm {}", id, e);
            redirectAttributes.addFlashAttribute("errorMessage", "Không thể thay đổi trạng thái. Vui lòng thử lại.");
        }
        return "redirect:/admin/products";
    }

    /**
     * Trang chi tiết sản phẩm (đọc-only)
     */
    @GetMapping("/{id}")
    public String viewProductDetail(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        try {
            AdminShoesDetailDto shoesDetail = adminProductService.getAdminShoesDetail(id);
            model.addAttribute("shoes", shoesDetail);
            return "admin/product/detail";
        } catch (NotFoundException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/admin/products";
        }
    }
}
