package ecommerce.shoestore.shoes;

import ecommerce.shoestore.common.NotFoundException;
import ecommerce.shoestore.shoes.dto.ShoesDetailDto;
import ecommerce.shoestore.shoes.dto.ShoesListDto;
import ecommerce.shoestore.shoes.dto.ShoesSummaryDto;
import ecommerce.shoestore.shoesimage.ShoesImage;
import ecommerce.shoestore.shoesvariant.ShoesVariant;
import ecommerce.shoestore.shoesvariant.ShoesVariantDto;
import ecommerce.shoestore.shoes.ShoesType;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Sort; // import để sắp xếp
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class ShoesService {

    private final ShoesRepository shoesRepository;

    /**
     * Lấy danh sách giày có phân trang
     */
    @Transactional(readOnly = true)
    public ShoesListDto getShoesList(int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size);

        // Bước 1: Lấy danh sách ID (có phân trang, không bị lặp)
        Page<Shoes> shoesPage = shoesRepository.findAllPaged(pageable);

        // Bước 2: Lấy danh sách ID từ kết quả
        List<Long> shoeIds = new ArrayList<>();
        for (Shoes s : shoesPage.getContent()) {
            shoeIds.add(s.getShoeId());
        }

        // Bước 3: Lấy chi tiết giày theo IDs (chỉ kèm images)
        List<Shoes> shoesList = new ArrayList<>();
        if (!shoeIds.isEmpty()) {
            shoesList = shoesRepository.findAllByIdsWithImages(shoeIds);
        }

        // Chuyển đổi sang DTO
        List<ShoesSummaryDto> products = new ArrayList<>();
        for (Shoes shoes : shoesList) {
            products.add(convertToSummaryDto(shoes));
        }

        return ShoesListDto.builder()
                .products(products)
                .currentPage(page)
                .totalPages(shoesPage.getTotalPages())
                .totalItems(shoesPage.getTotalElements())
                .build();
    }

    /**
     * Lấy chi tiết 1 sản phẩm giày
     */
    @Transactional(readOnly = true)
    public ShoesDetailDto getShoesDetail(Long shoeId) {
        // Lấy shoes với images
        Shoes shoes = shoesRepository.findByIdWithImages(shoeId)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy sản phẩm ID: " + shoeId));

        // Lấy thêm variants (query riêng để tránh tích Descartes)
        shoesRepository.findByIdWithVariants(shoeId);

        return convertToDetailDto(shoes);
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

        // Không cần tính stock cho trang danh sách (đơn giản hóa)
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

    /**
     * Chuyển đổi Shoes -> ShoesDetailDto (dùng cho trang chi tiết)
     */
    private ShoesDetailDto convertToDetailDto(Shoes shoes) {
        // Lấy tên danh mục
        String categoryName = "General";
        if (shoes.getCategory() != null) {
            categoryName = shoes.getCategory().getName();
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

        // Lấy danh sách sizes, colors và tính tổng tồn kho
        Set<String> sizes = new HashSet<>();
        Set<String> colors = new HashSet<>();
        int totalStock = 0;
        List<ShoesVariantDto> variants = new ArrayList<>();

        if (shoes.getVariants() != null) {
            for (ShoesVariant v : shoes.getVariants()) {
                // Thêm size
                if (v.getSizeValue() != null) {
                    sizes.add(v.getSizeValue());
                }
                // Thêm color
                if (v.getColorValue() != null) {
                    colors.add(v.getColorValue());
                }
                // Cộng dồn stock
                if (v.getStock() != null) {
                    totalStock += v.getStock();
                }
                // Thêm variant DTO
                variants.add(ShoesVariantDto.builder()
                        .variantId(v.getVariantId())
                        .size(v.getSizeValue())
                        .color(v.getColorValue())
                        .stock(v.getStock())
                        .build());
            }
        }

        // Lấy sản phẩm liên quan
        List<ShoesSummaryDto> relatedProducts = getRelatedProducts(shoes);

        return ShoesDetailDto.builder()
                .shoeId(shoes.getShoeId())
                .name(shoes.getName())
                .brand(shoes.getBrand())
                .basePrice(shoes.getBasePrice() != null ? shoes.getBasePrice() : BigDecimal.ZERO)
                .description(shoes.getDescription())
                .category(categoryName)
                .type(shoes.getType() != null ? shoes.getType().name() : null)
                .collection(shoes.getCollection())
                .imageUrls(imageUrls)
                .sizes(sizes)
                .colors(colors)
                .variants(variants)
                .totalStock(totalStock)
                .relatedProducts(relatedProducts)
                .build();
    }

    /**
     * Lấy danh sách sản phẩm liên quan (cùng category)
     */
    private List<ShoesSummaryDto> getRelatedProducts(Shoes shoes) {
        List<ShoesSummaryDto> result = new ArrayList<>();

        if (shoes.getCategory() == null) {
            return result;
        }

        // Lấy tối đa 5 sản phẩm cùng category
        Pageable pageable = PageRequest.of(0, 5);
        List<Shoes> relatedList = shoesRepository.findRelatedProducts(
                shoes.getCategory().getCategoryId(),
                shoes.getShoeId(),
                pageable
        );

        for (Shoes s : relatedList) {
            result.add(convertToSummaryDto(s));
        }

        return result;
    }



    /**
     * Gợi ý tìm kiếm (brand + product name)
     * Dùng cho autocomplete dropdown
     */
    @Transactional(readOnly = true)
    public List<String> getSearchSuggestions(String keyword) {

        // gọi query lấy danh sách gợi ý
        List<String> suggestions = shoesRepository.findSuggestions(keyword);

        if (suggestions == null || suggestions.isEmpty()) {
            return Collections.emptyList();
        }

        // Giới hạn 10 gợi ý tối đa
        return suggestions.stream()
                .distinct()
                .limit(10)
                .collect(Collectors.toList());
    }

    private Sort buildSort(String sortKey) {
        if (sortKey == null || sortKey.isBlank()) {
            return Sort.by("createdAt").descending(); // mặc định: mới nhất
        }

        return switch (sortKey) {
            case "newest" -> Sort.by("createdAt").descending();
            case "price_asc" -> Sort.by("basePrice").ascending();
            case "price_desc" -> Sort.by("basePrice").descending();
            case "name_asc" -> Sort.by("name").ascending();
            case "name_desc" -> Sort.by("name").descending();
            default -> Sort.by("createdAt").descending();
        };
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

        Sort sortObj = buildSort(sort);
        Pageable pageable = PageRequest.of(page - 1, size, sortObj);

        String kw = (keyword != null && !keyword.isBlank())
                ? keyword.trim()
                : null;

        Page<Shoes> pageResult = shoesRepository.searchAndFilter(
                kw,
                categoryId,
                brand,
                type,          // ✅ truyền thẳng String
                minPrice,
                maxPrice,
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

    public List<String> findAllBrands(ShoesType type) {
        if (type == null) {
            return shoesRepository.findDistinctBrands();
        }
        return shoesRepository.findDistinctBrandsByType(type);
    }

    private Pageable buildPageable(int page, int size, String sort) {
        if ("sold".equals(sort)) {
            // sold dùng ORDER BY trong query → KHÔNG dùng Sort
            return PageRequest.of(page - 1, size);
        }

        Sort sortObj = buildSort(sort);
        return PageRequest.of(page - 1, size, sortObj);
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

        Page<Shoes> pageResult = shoesRepository.findBestSeller(
                kw,
                categoryId,
                brand,
                type,          // ✅ dùng trực tiếp String
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
}
