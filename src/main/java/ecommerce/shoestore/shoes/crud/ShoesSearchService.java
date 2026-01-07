package ecommerce.shoestore.shoes.crud;

import ecommerce.shoestore.shoes.Shoes;
import ecommerce.shoestore.shoes.ShoesType;
import ecommerce.shoestore.shoes.dto.ShoesDetailDto;
import ecommerce.shoestore.shoes.dto.ShoesListDto;
import ecommerce.shoestore.shoes.dto.ShoesSummaryDto;
import ecommerce.shoestore.shoesimage.ShoesImage;
import ecommerce.shoestore.shoesvariant.ShoesVariant;
import ecommerce.shoestore.shoesvariant.ShoesVariantDto;
import lombok.RequiredArgsConstructor;

import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ShoesSearchService {

    private final ShoesSearchRepository shoesSearchRepository;

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

        // Validate: nếu page > totalPages, chuyển về trang cuối
        int totalPages = pageResult.getTotalPages();
        if (totalPages > 0 && page > totalPages) {
            page = totalPages;
            pageable = PageRequest.of(page - 1, size);
            pageResult = shoesSearchRepository.searchAndFilter(
                    kw, categoryId, brand, type, minPrice, maxPrice, sortKey, pageable
            );
        }

        // Lấy stock map cho tất cả shoes trong kết quả
        List<Long> shoeIds = pageResult.getContent().stream()
                .map(Shoes::getShoeId)
                .toList();
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

        // Validate: nếu page > totalPages, chuyển về trang cuối
        int totalPages = pageResult.getTotalPages();
        if (totalPages > 0 && page > totalPages) {
            page = totalPages;
            pageable = PageRequest.of(page - 1, size);
            pageResult = shoesSearchRepository.findBestSeller(
                    kw, categoryId, brand, type, minPrice, maxPrice, pageable
            );
        }

        // Lấy stock map cho tất cả shoes trong kết quả
        List<Long> shoeIds = pageResult.getContent().stream()
                .map(Shoes::getShoeId)
                .toList();
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

    public List<String> findAllBrands(ShoesType type) {
        if (type == null) {
            return shoesSearchRepository.findDistinctBrands();
        }
        return shoesSearchRepository.findDistinctBrandsByType(type);
    }

    private String buildSortKey(String sortKey) {
        if (sortKey == null || sortKey.isBlank()) {
            return "name_asc"; // mặc định
        }

        return switch (sortKey) {
            case "newest", "price_asc", "price_desc", "name_asc", "name_desc" ->
                sortKey;
            default ->
                "name_asc";
        };
    }

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

        // Tính tổng stock thực tế từ variants
        int totalStock = 0;
        if (shoes.getVariants() != null) {
            for (ShoesVariant variant : shoes.getVariants()) {
                if (variant.getStock() != null) {
                    totalStock += variant.getStock();
                }
            }
        }

        // Kiểm tra sản phẩm mới (trong vòng 14 ngày)
        boolean isNew = false;
        if (shoes.getCreatedAt() != null) {
            long daysSinceCreated = ChronoUnit.DAYS.between(shoes.getCreatedAt(), OffsetDateTime.now());
            isNew = daysSinceCreated <= 14;
        }

        return ShoesSummaryDto.builder()
                .shoeId(shoes.getShoeId())
                .name(shoes.getName())
                .brand(shoes.getBrand())
                .price(shoes.getBasePrice() != null ? shoes.getBasePrice() : BigDecimal.ZERO)
                .thumbnailUrl(thumbnailUrl)
                .outOfStock(totalStock <= 0)
                .isNew(isNew)
                .type(shoes.getType() != null ? shoes.getType().name() : null)
                .build();
    }

    /**
     * Chuyển đổi Shoes -> ShoesDetailDto (dùng cho trang chi tiết)
     */
    private ShoesDetailDto convertToDetailDto(Shoes shoes) {
        // Lấy tên danh mục và categoryId
        String categoryName = "General";
        Long categoryId = null;
        if (shoes.getCategory() != null) {
            categoryName = shoes.getCategory().getName();
            categoryId = shoes.getCategory().getCategoryId();
        }

        // Lấy danh sách URL hình ảnh
        List<String> imageUrls = new ArrayList<>();
        if (shoes.getImages() != null) {
            for (ShoesImage img : shoes.getImages()) {
                imageUrls.add(img.getUrl());
            }
        }
        if (imageUrls.isEmpty()) {
            imageUrls.add("https://placehold.co/600x600?text=No+Image");
        }

        // Lấy danh sách sizes, colors, variants và tổng tồn kho
        Set<String> sizes = new HashSet<>();
        Set<String> colors = new HashSet<>();
        int totalStock = 0;
        List<ShoesVariantDto> variants = new ArrayList<>();

        if (shoes.getVariants() != null) {
            for (ShoesVariant v : shoes.getVariants()) {
                if (v.getSizeValue() != null) {
                    sizes.add(v.getSizeValue());
                }
                if (v.getColorValue() != null) {
                    colors.add(v.getColorValue());
                }
                if (v.getStock() != null) {
                    totalStock += v.getStock();
                }

                variants.add(ShoesVariantDto.builder()
                        .variantId(v.getVariantId())
                        .size(v.getSizeValue())
                        .color(v.getColorValue())
                        .stock(v.getStock())
                        .build());
            }
        }

        return ShoesDetailDto.builder()
                .shoeId(shoes.getShoeId())
                .name(shoes.getName())
                .brand(shoes.getBrand())
                .basePrice(shoes.getBasePrice() != null ? shoes.getBasePrice() : BigDecimal.ZERO)
                .description(shoes.getDescription())
                .category(categoryName)
                .categoryId(categoryId)
                .type(shoes.getType() != null ? shoes.getType().name() : null)
                .collection(shoes.getCollection())
                .imageUrls(imageUrls)
                .sizes(sizes)
                .colors(colors)
                .variants(variants)
                .totalStock(totalStock)
                .build();
    }
}
