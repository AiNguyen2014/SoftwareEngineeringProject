package ecommerce.shoestore.shoes;

import ecommerce.shoestore.common.NotFoundException;
import ecommerce.shoestore.shoes.dto.ShoesDetailDto;
import ecommerce.shoestore.shoes.dto.ShoesListDto;
import ecommerce.shoestore.shoes.dto.ShoesSummaryDto;
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
        
        // Lấy danh sách sản phẩm từ database
        Page<Shoes> shoesPage = shoesRepository.findAllActive(pageable);

        // Chuyển đổi sang DTO
        List<ShoesSummaryDto> products = new ArrayList<>();
        for (Shoes shoes : shoesPage.getContent()) {
            ShoesSummaryDto dto = convertToSummaryDto(shoes);
            products.add(dto);
        }

        // Trả về kết quả
        return ShoesListDto.builder()
                .products(products)
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
        log.info("Lấy chi tiết sản phẩm ID: {}", shoeId);

        // Query 1: Lấy sản phẩm với images (tách riêng để tránh lặp ảnh)
        Shoes shoesWithImages = shoesRepository.findByIdWithImages(shoeId)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy sản phẩm ID: " + shoeId));

        // Query 2: Lấy variants riêng (tránh Cartesian product)
        Shoes shoesWithVariants = shoesRepository.findByIdWithVariants(shoeId)
                .orElse(null);

        // Gộp variants vào shoes chính
        if (shoesWithVariants != null && shoesWithVariants.getVariants() != null) {
            shoesWithImages.setVariants(shoesWithVariants.getVariants());
        }

        // Chuyển đổi sang DTO
        ShoesDetailDto dto = convertToDetailDto(shoesWithImages);

        // Lấy sản phẩm liên quan
        List<ShoesSummaryDto> relatedProducts = getRelatedProducts(shoesWithImages);
        dto.setRelatedProducts(relatedProducts);

        return dto;
    }

    // ========== PRIVATE METHODS ==========

    /**
     * Chuyển đổi Shoes -> ShoesSummaryDto (cho danh sách)
     */
    private ShoesSummaryDto convertToSummaryDto(Shoes shoes) {
        // Lấy ảnh thumbnail
        String thumbnailUrl = getThumbnailUrl(shoes);
        
        // Kiểm tra hết hàng
        boolean outOfStock = isOutOfStock(shoes.getShoeId());
        
        // Kiểm tra sản phẩm mới (trong vòng 14 ngày)
        boolean isNew = isNewProduct(shoes);

        return ShoesSummaryDto.builder()
                .shoeId(shoes.getShoeId())
                .name(shoes.getName())
                .brand(shoes.getBrand())
                .price(shoes.getBasePrice() != null ? shoes.getBasePrice() : BigDecimal.ZERO)
                .thumbnailUrl(thumbnailUrl)
                .outOfStock(outOfStock)
                .isNew(isNew)
                .type(shoes.getType() != null ? shoes.getType().name() : null)
                .build();
    }

    /**
     * Chuyển đổi Shoes -> ShoesDetailDto (cho trang chi tiết)
     */
    private ShoesDetailDto convertToDetailDto(Shoes shoes) {
        // Lấy tên category
        String categoryName = "General";
        if (shoes.getCategory() != null) {
            categoryName = shoes.getCategory().getName();
        }

        // Lấy danh sách ảnh
        List<String> imageUrls = new ArrayList<>();
        if (shoes.getImages() != null) {
            for (ShoesImage img : shoes.getImages()) {
                imageUrls.add(img.getUrl());
            }
        }
        if (imageUrls.isEmpty()) {
            imageUrls.add("https://placehold.co/600x600?text=No+Image");
        }

        // Lấy sizes, colors và tính tổng stock
        Set<String> sizes = new HashSet<>();
        Set<String> colors = new HashSet<>();
        int totalStock = 0;

        if (shoes.getVariants() != null) {
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
                .totalStock(totalStock)
                .build();
    }

    /**
     * Lấy URL ảnh thumbnail
     */
    private String getThumbnailUrl(Shoes shoes) {
        // Nếu không có ảnh -> trả về placeholder
        if (shoes.getImages() == null || shoes.getImages().isEmpty()) {
            return "https://placehold.co/400x400?text=No+Image";
        }

        // Tìm ảnh có isThumbnail = true
        for (ShoesImage img : shoes.getImages()) {
            if (img.isThumbnail()) {
                return img.getUrl();
            }
        }

        // Nếu không có thumbnail -> lấy ảnh đầu tiên
        return shoes.getImages().get(0).getUrl();
    }

    /**
     * Kiểm tra sản phẩm hết hàng
     */
    private boolean isOutOfStock(Long shoeId) {
        Integer totalStock = variantRepository.getTotalStockByShoeId(shoeId);
        return totalStock == null || totalStock <= 0;
    }
    
    /**
     * Kiểm tra sản phẩm có phải là sản phẩm mới không
     * Sản phẩm mới = được tạo trong vòng NEW_PRODUCT_DAYS ngày
     */
    private boolean isNewProduct(Shoes shoes) {
        if (shoes.getCreatedAt() == null) {
            return false;
        }
        OffsetDateTime cutoffDate = OffsetDateTime.now().minusDays(NEW_PRODUCT_DAYS);
        return shoes.getCreatedAt().isAfter(cutoffDate);
    }

    /**
     * Lấy sản phẩm liên quan (cùng category, tối đa 4 sản phẩm)
     */
    private List<ShoesSummaryDto> getRelatedProducts(Shoes shoes) {
        List<ShoesSummaryDto> result = new ArrayList<>();

        // Nếu không có category -> trả về rỗng
        if (shoes.getCategory() == null) {
            return result;
        }

        try {
            // Lấy 4 sản phẩm cùng category
            Pageable pageable = PageRequest.of(0, 4);
            List<Shoes> relatedShoes = shoesRepository.findRelatedProducts(
                    shoes.getCategory().getCategoryId(),
                    shoes.getShoeId(),
                    pageable
            );

            // Chuyển đổi sang DTO
            for (Shoes related : relatedShoes) {
                ShoesSummaryDto dto = convertToSummaryDto(related);
                result.add(dto);
            }
        } catch (Exception e) {
            log.warn("Lỗi khi lấy sản phẩm liên quan: {}", e.getMessage());
        }

        return result;
    }
}