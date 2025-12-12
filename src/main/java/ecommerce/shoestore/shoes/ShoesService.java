package ecommerce.shoestore.shoes;

import ecommerce.shoestore.common.NotFoundException;
import ecommerce.shoestore.shoes.dto.*;
import ecommerce.shoestore.shoesimage.ShoesImage;
import ecommerce.shoestore.shoesvariant.ShoesVariant;
import ecommerce.shoestore.shoesvariant.ShoesVariantRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
public class ShoesService {

    private final ShoesRepository shoesRepository;
    private final ShoesVariantRepository variantRepository;

    /**
     * Lấy TOÀN BỘ sản phẩm có phân trang
     */
    @Transactional(readOnly = true)
    public ShoesListDto getShoesList(int page, int size) {
        log.info("Fetching ALL shoes - page: {}, size: {}", page, size);

        Pageable pageable = PageRequest.of(page - 1, size);

        // Gọi findAll() từ Repository
        Page<Shoes> shoesPage = shoesRepository.findAll(pageable);

        List<ShoesSummaryDto> dtos = shoesPage.getContent().stream()
                .map(this::convertToSummaryDto)
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
     * Sửa: tham số id -> shoeId
     */
    @Transactional(readOnly = true)
    public ShoesDetailDto getShoesDetail(Long shoeId) {
        log.info("Fetching shoes detail for shoeId: {}", shoeId);

        // Sửa: Dùng findByIdWithDetails với tham số shoeId
        Shoes shoes = shoesRepository.findByIdWithDetails(shoeId)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy sản phẩm ID: " + shoeId));

        return convertToDetailDto(shoes);
    }

    // ==================== PRIVATE MAPPERS ====================

    /**
     * Convert Shoes Entity -> ShoesSummaryDto (cho List View)
     */
    private ShoesSummaryDto convertToSummaryDto(Shoes shoes) {
        String thumbnailUrl = getThumbnailUrl(shoes);

        // Sửa: shoes.getId() -> shoes.getShoeId()
        boolean outOfStock = isOutOfStock(shoes.getShoeId());

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

    /**
     * Convert Shoes Entity -> ShoesDetailDto (cho Detail View)
     */
    private ShoesDetailDto convertToDetailDto(Shoes shoes) {
        // Xử lý Category
        String categoryName = "General";
        if (shoes.getCategory() != null) {
            categoryName = shoes.getCategory().getName();
        }

        // Xử lý Images (Set -> List String)
        List<String> imageUrls = new ArrayList<>();
        String thumbnailUrl = null;

        if (shoes.getImages() != null && !shoes.getImages().isEmpty()) {
            for (ShoesImage img : shoes.getImages()) {
                imageUrls.add(img.getUrl());
                // Sửa: getIsThumbnail() (Do Lombok sinh ra cho field Boolean object)
                if (Boolean.TRUE.equals(img.getIsThumbnail())) {
                    thumbnailUrl = img.getUrl();
                }
            }
        }

        // Nếu không có thumbnail set cứng, lấy ảnh đầu tiên
        if (thumbnailUrl == null && !imageUrls.isEmpty()) {
            thumbnailUrl = imageUrls.get(0);
        }

        if (imageUrls.isEmpty()) {
            imageUrls.add("https://placehold.co/600x600?text=No+Image");
        }

        // Xử lý Variants (Sizes & Colors)
        Set<String> sizes = new HashSet<>();
        Set<String> colors = new HashSet<>();
        int totalStock = 0;

        if (shoes.getVariants() != null && !shoes.getVariants().isEmpty()) {
            for (ShoesVariant variant : shoes.getVariants()) {
                // Sửa: Dùng helper method từ Entity ShoesVariant
                if (variant.getSizeValue() != null) {
                    sizes.add(variant.getSizeValue());
                }
                if (variant.getColorValue() != null) {
                    colors.add(variant.getColorValue());
                }
                if (variant.getStock() != null) {
                    totalStock += variant.getStock();
                }
            }
        }

        // Lấy Related Products
        List<ShoesSummaryDto> relatedProducts = getRelatedProducts(shoes);

        return ShoesDetailDto.builder()
                .shoeId(shoes.getShoeId())
                .name(shoes.getName())
                .brand(shoes.getBrand())
                .basePrice(shoes.getBasePrice() != null ? shoes.getBasePrice() : BigDecimal.ZERO) // ✅ Sửa: getBasePrice
                .description(shoes.getDescription())
                .category(categoryName)
                .type(shoes.getType() != null ? shoes.getType().name() : null)
                .collection(shoes.getCollection())
                .imageUrls(imageUrls)
                .sizes(sizes)
                .colors(colors)
                .totalStock(totalStock)
                .relatedProducts(relatedProducts)
                .build();
    }

    /**
     * Lấy thumbnail URL
     */
    private String getThumbnailUrl(Shoes shoes) {
        if (shoes.getImages() != null && !shoes.getImages().isEmpty()) {
            Optional<ShoesImage> thumbnail = shoes.getImages().stream()
                    .filter(img -> Boolean.TRUE.equals(img.getIsThumbnail()))
                    .findFirst();

            if (thumbnail.isPresent()) {
                return thumbnail.get().getUrl();
            }
            return shoes.getImages().iterator().next().getUrl();
        }

        return "https://placehold.co/400x400?text=No+Image";
    }

    /**
     * Kiểm tra tồn kho
     */
    private boolean isOutOfStock(Long shoeId) { // Sửa tên tham số
        // Lưu ý: Cần đảm bảo ShoesVariantRepository cũng đã đổi method query theo shoeId
        Integer totalStock = variantRepository.getTotalStockByShoeId(shoeId);
        return totalStock == null || totalStock <= 0;
    }

    /**
     * Lấy 5 sản phẩm liên quan
     */
    private List<ShoesSummaryDto> getRelatedProducts(Shoes shoes) {
        if (shoes.getCategory() == null) {
            return new ArrayList<>();
        }

        try {
            Pageable pageable = PageRequest.of(0, 5);

            // Sửa: getCategoryId() và getShoeId()
            // Lưu ý: Cần đảm bảo Category Entity đã có field categoryId
            Page<Shoes> relatedPage = shoesRepository.findRelatedProducts(
                    shoes.getCategory().getCategoryId(),
                    shoes.getShoeId(),
                    pageable
            );

            return relatedPage.getContent().stream()
                    .map(this::convertToSummaryDto)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.warn("Error fetching related products for shoe ID: {}", shoes.getShoeId(), e);
            return new ArrayList<>();
        }
    }
}