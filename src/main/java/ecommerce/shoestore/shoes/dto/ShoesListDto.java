package ecommerce.shoestore.shoes.dto;

import lombok.*;
import java.util.List;

/**
 * DTO chứa danh sách sản phẩm + thông tin phân trang
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor

public class ShoesListDto {
    private List<ShoesSummaryDto> products;

    private int currentPage;

    private int totalPages;
    
    private long totalItems;
}