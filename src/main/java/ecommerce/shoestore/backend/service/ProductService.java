package ecommerce.shoestore.backend.service;

import ecommerce.shoestore.backend.dto.ProductDetailDto;
import ecommerce.shoestore.backend.dto.ProductListDto;
// 👇 THÊM DÒNG NÀY ĐỂ KHẮC PHỤC LỖI
import ecommerce.shoestore.backend.exception.NotFoundException;

import java.util.List;

/**
 * Service interface cho Product
 * Định nghĩa các business logic methods
 */
public interface ProductService {

    /**
     * Lấy danh sách sản phẩm có phân trang
     *
     * @param page Trang hiện tại (bắt đầu từ 1)
     * @param pageSize Số sản phẩm mỗi trang
     * @return ProductListDto chứa danh sách + thông tin phân trang
     */
    ProductListDto getProductList(int page, int pageSize);

    /**
     * Lấy chi tiết một sản phẩm
     *
     * @param id ID của sản phẩm
     * @return ProductDetailDto chứa đầy đủ thông tin
     * @throws NotFoundException nếu không tìm thấy sản phẩm
     */
    ProductDetailDto getProductDetail(Long id);
}