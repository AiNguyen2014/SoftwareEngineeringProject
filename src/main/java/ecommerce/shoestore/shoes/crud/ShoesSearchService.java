package ecommerce.shoestore.shoes.crud;

import ecommerce.shoestore.shoes.Shoes;
import ecommerce.shoestore.shoes.ShoesType;
import ecommerce.shoestore.shoes.dto.ShoesListDto;
import ecommerce.shoestore.shoes.dto.ShoesSummaryDto;
import ecommerce.shoestore.shoesimage.ShoesImage;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ShoesSearchService {

    private final ShoesSearchRepository shoesSearchRepository;

    /**
     * Gợi ý tìm kiếm (brand + product name)
     * Dùng cho autocomplete dropdown
     */
    @Transactional(readOnly = true)
    public List<String> getSearchSuggestions(String keyword) {
        List<String> suggestions = shoesSearchRepository.findSuggestions(keyword);

        if (suggestions == null || suggestions.isEmpty()) {
            return Collections.emptyList();
        }

        // Giới hạn 10 gợi ý tối đa
        return suggestions.stream()
                .distinct()
                .limit(10)
                .collect(Collectors.toList());
    }

    /**
     * Tìm kiếm sản phẩm với filter và sort
     */
    @Transactional(readOnly = true)
    public ShoesListDto searchProducts(
            String keyword,
            Long categoryId,
            String brand,
            String type,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            int page,
            int size,
            String sort
    ) {
        brand = (brand != null && brand.isBlank()) ? null : brand;
        sort = (sort != null && sort.isBlank()) ? null : sort;

        if (minPrice != null && minPrice.compareTo(BigDecimal.ZERO) <= 0) {
            minPrice = null;
        }
        if (maxPrice != null && maxPrice.compareTo(BigDecimal.ZERO) <= 0) {
            maxPrice = null;
        }

        // sold → chuyển sang query riêng
        if ("sold".equals(sort)) {
            return searchProductsWithSoldSort(
                    keyword,
                    categoryId,
                    brand,
                    type,
                    minPrice,
                    maxPrice,
                    page,
                    size
            );
        }

        String sortKey = buildSortKey(sort);
        Pageable pageable = PageRequest.of(page - 1, size);

        String kw = (keyword != null && !keyword.isBlank())
                ? keyword.trim()
                : null;

        Page<Shoes> pageResult = shoesSearchRepository.searchAndFilter(
                kw,
                categoryId,
                brand,
                type,
                minPrice,
                maxPrice,
                sortKey,
                pageable
        );

        List<ShoesSummaryDto> dtos = pageResult.getContent().stream()
                .map(this::convertToSummaryDto)
                .toList();

        return ShoesListDto.builder()
                .products(dtos)
                .currentPage(page)
                .totalPages(pageResult.getTotalPages())
                .totalItems(pageResult.getTotalElements())
                .build();
    }

    /**
     * Tìm kiếm sản phẩm với sort theo số lượng bán
     */
    @Transactional(readOnly = true)
    public ShoesListDto searchProductsWithSoldSort(
            String keyword,
            Long categoryId,
            String brand,
            String type,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            int page,
            int size
    ) {
        Pageable pageable = PageRequest.of(page - 1, size);

        String kw = (keyword != null && !keyword.isBlank())
                ? keyword.trim()
                : null;

        Page<Shoes> pageResult = shoesSearchRepository.findBestSeller(
                kw,
                categoryId,
                brand,
                type,
                minPrice,
                maxPrice,
                pageable
        );

        List<ShoesSummaryDto> products = pageResult.getContent().stream()
                .map(this::convertToSummaryDto)
                .toList();

        return ShoesListDto.builder()
                .products(products)
                .currentPage(page)
                .totalPages(pageResult.getTotalPages())
                .totalItems(pageResult.getTotalElements())
                .build();
    }

    /**
     * Lấy danh sách brands
     */
    public List<String> findAllBrands(ShoesType type) {
        if (type == null) {
            return shoesSearchRepository.findDistinctBrands();
        }
        return shoesSearchRepository.findDistinctBrandsByType(type);
    }

    /**
     * Build sort key hợp lệ
     */
    private String buildSortKey(String sortKey) {
        if (sortKey == null || sortKey.isBlank()) {
            return "name_asc"; // mặc định
        }

        return switch (sortKey) {
            case "newest",
                 "price_asc",
                 "price_desc",
                 "name_asc",
                 "name_desc" -> sortKey;
            default -> "name_asc";
        };
    }

    /**
     * Chuyển đổi Shoes -> ShoesSummaryDto (dùng cho danh sách)
     */
    private ShoesSummaryDto convertToSummaryDto(Shoes shoes) {
        // Lấy ảnh thumbnail
        String thumbnailUrl = "https://placehold.co/400x400?text=No+Image";
        if (shoes.getImages() != null) {
            for (ShoesImage img : shoes.getImages()) {
                if (img.isThumbnail()) {
                    thumbnailUrl = img.getUrl();
                    break;
                }
            }
            // Nếu không có thumbnail, lấy ảnh đầu tiên
            if (thumbnailUrl.contains("placehold") && !shoes.getImages().isEmpty()) {
                thumbnailUrl = shoes.getImages().iterator().next().getUrl();
            }
        }

        // Không cần tính stock cho trang danh sách
        int totalStock = 1; // Mặc định là còn hàng

        return ShoesSummaryDto.builder()
                .shoeId(shoes.getShoeId())
                .name(shoes.getName())
                .brand(shoes.getBrand())
                .price(shoes.getBasePrice() != null ? shoes.getBasePrice() : BigDecimal.ZERO)
                .thumbnailUrl(thumbnailUrl)
                .outOfStock(totalStock <= 0)
                .type(shoes.getType() != null ? shoes.getType().name() : null)
                .build();
    }
}
