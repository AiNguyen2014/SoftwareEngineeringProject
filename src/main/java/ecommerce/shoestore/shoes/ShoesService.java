package ecommerce.shoestore.shoes;

import ecommerce.shoestore.common.NotFoundException;
import ecommerce.shoestore.shoes.dto.*;
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
     * Lấy danh sách sản phẩm có phân trang + filter category
     */
    @Transactional(readOnly = true)
    public ShoesListDto getShoesList(int page, int size, String category) {
        log.info("Fetching shoes list - page: {}, size: {}, category: {}", page, size, category);

        Pageable pageable = PageRequest.of(page - 1, size);
        Page<Shoes> shoesPage;

        if (category != null && !category.isEmpty()) {
            shoesPage = shoesRepository.findByCategory_Name(category, pageable);
        } else {
            shoesPage = shoesRepository.findAll(pageable);
        }

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
     */
    @Transactional(readOnly = true)
    public ShoesDetailDto getShoesDetail(Long id) {
        log.info("Fetching shoes detail for ID: {}", id);

        Shoes shoes = shoesRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy sản phẩm ID: " + id));

        return convertToDetailDto(shoes);
    }

    // ==================== PRIVATE MAPPERS ====================

    /**
     * Convert Shoes Entity -> ShoesSummaryDto (cho List View)
     */
    private ShoesSummaryDto convertToSummaryDto(Shoes shoes) {
        // Lấy thumbnail
        String thumbnailUrl = getThumbnailUrl(shoes);

        // Kiểm tra còn hàng không
        boolean outOfStock = isOutOfStock(shoes.getId());

        return ShoesSummaryDto.builder()
                .id(shoes.getId())
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

        // Xử lý Images
        List<String> imageUrls = new ArrayList<>();
        String thumbnailUrl = null;

        if (shoes.getImages() != null && !shoes.getImages().isEmpty()) {
            for (ShoesImage img : shoes.getImages()) {
                imageUrls.add(img.getUrl());
                if (Boolean.TRUE.equals(img.getIsThumbnail())) {
                    thumbnailUrl = img.getUrl();
                }
            }
        }

        // Nếu không có thumbnail, lấy ảnh đầu tiên
        if (thumbnailUrl == null && !imageUrls.isEmpty()) {
            thumbnailUrl = imageUrls.get(0);
        }

        // Nếu không có ảnh nào, dùng placeholder
        if (imageUrls.isEmpty()) {
            imageUrls.add("https://placehold.co/600x600?text=No+Image");
            thumbnailUrl = imageUrls.get(0);
        }

        // Xử lý Variants (Sizes & Colors)
        Set<String> sizes = new HashSet<>();
        Set<String> colors = new HashSet<>();
        int totalStock = 0;

        if (shoes.getVariants() != null && !shoes.getVariants().isEmpty()) {
            for (ShoesVariant variant : shoes.getVariants()) {
                if (variant.getSize() != null) {
                    sizes.add(variant.getSizeValue());
                }
                if (variant.getColor() != null) {
                    colors.add(variant.getColorValue());
                }
                if (variant.getStock() != null) {
                    totalStock += variant.getStock();
                }
            }
        }

        return ShoesDetailDto.builder()
                .id(shoes.getId())
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
                .totalStock(totalStock)
                .relatedProducts(new ArrayList<>()) // TODO: Implement later
                .build();
    }

    /**
     * Lấy thumbnail URL từ Images
     */
    private String getThumbnailUrl(Shoes shoes) {
        if (shoes.getImages() != null && !shoes.getImages().isEmpty()) {
            Optional<ShoesImage> thumbnail = shoes.getImages().stream()
                    .filter(img -> Boolean.TRUE.equals(img.getIsThumbnail()))
                    .findFirst();

            if (thumbnail.isPresent()) {
                return thumbnail.get().getUrl();
            }

            return shoes.getImages().get(0).getUrl();
        }

        return "https://placehold.co/400x400?text=No+Image";
    }

    /**
     * Kiểm tra sản phẩm còn hàng không
     */
    private boolean isOutOfStock(Long shoesId) {
        Integer totalStock = variantRepository.getTotalStockByShoeId(shoesId);
        return totalStock == null || totalStock <= 0;
    }
}