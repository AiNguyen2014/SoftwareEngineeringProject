package ecommerce.shoestore.shoes;

import ecommerce.shoestore.common.NotFoundException;
import ecommerce.shoestore.shoes.dto.ShoesDetailDto;
import ecommerce.shoestore.shoes.dto.ShoesListDto;
import ecommerce.shoestore.shoes.dto.ShoesSummaryDto;
import ecommerce.shoestore.shoesimage.ShoesImage;
import ecommerce.shoestore.shoesvariant.ShoesVariant;
import ecommerce.shoestore.shoesvariant.ShoesVariantDto;
import ecommerce.shoestore.shoesvariant.ShoesVariantRepository;
import ecommerce.shoestore.shoes.ShoesType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Sort; // import để sắp xếp
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class ShoesService {

    private final ShoesRepository shoesRepository;
    private final ShoesVariantRepository variantRepository;

    /** Số ngày để xác định sản phẩm là "mới" */
    private static final int NEW_PRODUCT_DAYS = 14;

    /**
     * Lấy danh sách sản phẩm (có phân trang)
     */
    @Transactional(readOnly = true)
    public ShoesListDto getShoesList(int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size);

        // First, get paginated basic shoe data
        Page<Shoes> shoesPage = shoesRepository.findAllPaged(pageable);
        List<Long> shoeIds = shoesPage.getContent().stream()
                .map(Shoes::getShoeId)
                .collect(Collectors.toList());

        // Then fetch with details for the specific IDs
        List<Shoes> shoesList = shoeIds.isEmpty()
                ? new ArrayList<>()
                : shoesRepository.findAllWithDetailsByIds(shoeIds);

        Map<Long, Integer> stockMap = getStockMapForShoes(shoesList);

        List<ShoesSummaryDto> dtos = shoesList.stream()
                .map(shoes -> convertToSummaryDto(shoes, stockMap))
                .collect(Collectors.toList());

        return ShoesListDto.builder()
                .products(dtos)
                .currentPage(page)
                .totalPages(shoesPage.getTotalPages())
                .totalItems(shoesPage.getTotalElements())
                .build();
    }

    /**
     * Lấy chi tiết sản phẩm
     */
    @Transactional(readOnly = true)
    public ShoesDetailDto getShoesDetail(Long shoeId) {
        Shoes shoes = shoesRepository.findByIdWithDetails(shoeId)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy sản phẩm ID: " + shoeId));

        return convertToDetailDto(shoes);
    }

    private Map<Long, Integer> getStockMapForShoes(List<Shoes> shoesList) {
        if (shoesList.isEmpty()) {
            return Collections.emptyMap();
        }

        List<Long> shoeIds = shoesList.stream()
                .map(Shoes::getShoeId)
                .collect(Collectors.toList());

        List<Object[]> stockResults = variantRepository.getTotalStocksByShoeIds(shoeIds);

        return stockResults.stream()
                .collect(Collectors.toMap(
                        row -> (Long) row[0],
                        row -> ((Number) row[1]).intValue()
                ));
    }

    private ShoesSummaryDto convertToSummaryDto(Shoes shoes, Map<Long, Integer> stockMap) {
        String thumbnailUrl = getThumbnailUrl(shoes);
        int stock = stockMap.getOrDefault(shoes.getShoeId(), 0);
        boolean outOfStock = stock <= 0;

        return ShoesSummaryDto.builder()
                .shoeId(shoes.getShoeId())
                .name(shoes.getName())
                .brand(shoes.getBrand())
                .price(shoes.getBasePrice() != null ? shoes.getBasePrice() : BigDecimal.ZERO)
                .thumbnailUrl(thumbnailUrl)
                .outOfStock(outOfStock)
                .type(shoes.getType() != null ? shoes.getType().name() : null)
                .build();
    }

    private boolean isNewProduct(Shoes shoes) {
        if (shoes.getCreatedAt() == null) {
            return false;
        }
        return shoes.getCreatedAt()
                .isAfter(OffsetDateTime.now().minusDays(NEW_PRODUCT_DAYS));
    }

    /**
     * Chuyển đổi Shoes -> ShoesDetailDto (cho trang chi tiết)
     */
    private ShoesDetailDto convertToDetailDto(Shoes shoes) {
        String categoryName = shoes.getCategory() != null ? shoes.getCategory().getName() : "General";

        // Lấy danh sách ảnh
        List<String> imageUrls = new ArrayList<>();
        String thumbnailUrl = null;

        if (shoes.getImages() != null && !shoes.getImages().isEmpty()) {
            for (ShoesImage img : shoes.getImages()) {
                imageUrls.add(img.getUrl());
                if (img.isThumbnail()) {
                    thumbnailUrl = img.getUrl();
                }
            }
        }

        if (thumbnailUrl == null && !imageUrls.isEmpty()) {
            thumbnailUrl = imageUrls.get(0);
        }

        if (imageUrls.isEmpty()) {
            imageUrls.add("https://placehold.co/600x600?text=No+Image");
        }

        // Lấy sizes, colors và tính tổng stock
        Set<String> sizes = new HashSet<>();
        Set<String> colors = new HashSet<>();
        int totalStock = 0;

        if (shoes.getVariants() != null && !shoes.getVariants().isEmpty()) {
            for (ShoesVariant variant : shoes.getVariants()) {
                // Lấy size
                if (variant.getSizeValue() != null) {
                    sizes.add(variant.getSizeValue());
                }
                // Lấy color
                if (variant.getColorValue() != null) {
                    colors.add(variant.getColorValue());
                }
                // Cộng dồn stock
                if (variant.getStock() != null) {
                    totalStock += variant.getStock();
                }
            }
        }

        List<ShoesSummaryDto> relatedProducts = getRelatedProducts(shoes);

        List<ShoesVariantDto> variants = new ArrayList<>();

        if (shoes.getVariants() != null && !shoes.getVariants().isEmpty()) {
            for (ShoesVariant v : shoes.getVariants()) {
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
     * Lấy URL ảnh thumbnail
     */
    private String getThumbnailUrl(Shoes shoes) {
        if (shoes.getImages() != null && !shoes.getImages().isEmpty()) {
            Optional<ShoesImage> thumbnail = shoes.getImages().stream()
                    .filter(ShoesImage::isThumbnail)
                    .findFirst();

            if (thumbnail.isPresent()) {
                return thumbnail.get().getUrl();
            }
            return shoes.getImages().iterator().next().getUrl();
        }
        return "https://placehold.co/400x400?text=No+Image";
    }

    private List<ShoesSummaryDto> getRelatedProducts(Shoes shoes) {
        if (shoes.getCategory() == null) {
            return new ArrayList<>();
        }

        Pageable pageable = PageRequest.of(0, 5);

        List<Shoes> relatedList = shoesRepository.findRelatedProducts(
                shoes.getCategory().getCategoryId(),
                shoes.getShoeId(),
                pageable
        );

        Map<Long, Integer> stockMap = getStockMapForShoes(relatedList);

        return relatedList.stream()
                .map(s -> convertToSummaryDto(s, stockMap))
                .collect(Collectors.toList());
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
            ShoesType shoesType,
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

        if ("sold".equals(sort)) {
            return searchProductsWithSoldSort(
                    keyword,
                    categoryId,
                    brand,
                    shoesType,
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
                shoesType,
                minPrice,
                maxPrice,
                pageable
        );

        List<Shoes> shoesList = pageResult.getContent();

        Map<Long, Integer> stockMap = getStockMapForShoes(shoesList);

        List<ShoesSummaryDto> dtos = shoesList.stream()
                .map(shoes -> convertToSummaryDto(shoes, stockMap))
                .toList();


        return ShoesListDto.builder()
                .products(dtos)
                .currentPage(page)
                .totalPages(pageResult.getTotalPages())
                .totalItems(pageResult.getTotalElements())
                .totalSearchResults(
                        kw != null && pageResult.getTotalElements() == 0
                                ? 0
                                : -1
                )
                .build();
    }

    public List<String> findAllBrands(ShoesType type) {
        return shoesRepository.findDistinctBrands(type);
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
            ShoesType shoesType,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            int page,
            int size
    ) {
        Pageable pageable = PageRequest.of(page - 1, size);

        String type = shoesType != null ? shoesType.name() : null;
        String kw = (keyword != null && !keyword.isBlank()) ? keyword.trim() : null;

        Page<Shoes> pageResult = shoesRepository.findBestSeller(
                kw,
                categoryId,
                brand,
                type,
                minPrice,
                maxPrice,
                pageable
        );

        List<Shoes> shoesList = pageResult.getContent();
        Map<Long, Integer> stockMap = getStockMapForShoes(shoesList);

        return ShoesListDto.builder()
                .products(
                        shoesList.stream()
                                .map(s -> convertToSummaryDto(s, stockMap))
                                .toList()
                )
                .currentPage(page)
                .totalPages(pageResult.getTotalPages())
                .totalItems(pageResult.getTotalElements())
                .build();
    }
}
