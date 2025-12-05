package ecommerce.shoestore.backend.dto;

import lombok.*;
import java.util.List;

/**
 * DTO for Paginated Product List
 * Chứa danh sách sản phẩm + thông tin phân trang
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductListDto {

    // Danh sách sản phẩm trong trang hiện tại
    private List<ProductSummaryDto> products;

    // Trang hiện tại (bắt đầu từ 1)
    private int currentPage;

    // Tổng số trang
    private int totalPages;

    // Tổng số sản phẩm
    private long totalItems;
}
