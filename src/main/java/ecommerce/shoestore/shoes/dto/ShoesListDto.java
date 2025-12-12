package ecommerce.shoestore.shoes.dto;

import lombok.*;
import java.util.List;

/**
 * DTO cho Paginated Product List
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