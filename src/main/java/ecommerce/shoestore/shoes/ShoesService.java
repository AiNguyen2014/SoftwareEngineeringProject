package ecommerce.shoestore.shoes;

import ecommerce.shoestore.common.NotFoundException;
import ecommerce.shoestore.shoes.dto.ShoesDetailDto;
import ecommerce.shoestore.shoes.dto.ShoesListDto;
import ecommerce.shoestore.shoes.dto.ShoesSummaryDto;
import ecommerce.shoestore.shoesimage.ShoesImage;
import ecommerce.shoestore.shoesvariant.ShoesVariant;
import ecommerce.shoestore.shoesvariant.ShoesVariantDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

/**
 * Service chính cho Shoes - chỉ chứa logic hiển thị chi tiết sản phẩm
 * Các logic search/filter/sort đã được tách ra ShoesSearchService
 */
@Service
@RequiredArgsConstructor
public class ShoesService {

    private final ShoesRepository shoesRepository;

    /**
     * Lấy danh sách giày có phân trang
     */
    @Transactional(readOnly = true)
    public ShoesListDto getShoesList(int page, int size) {
        // Validate page: nếu < 1 thì mặc định về 1
        if (page < 1) {
            page = 1;
        }
        Pageable pageable = PageRequest.of(page - 1, size);

        // Bước 1: Lấy danh sách ID (có phân trang, không bị lặp)
        Page<Shoes> shoesPage = shoesRepository.findAllPaged(pageable);

        // Validate: nếu page > totalPages, chuyển về trang cuối
        int totalPages = shoesPage.getTotalPages();
        if (totalPages > 0 && page > totalPages) {
            page = totalPages;
            pageable = PageRequest.of(page - 1, size);
            shoesPage = shoesRepository.findAllPaged(pageable);
        }

        // Bước 2: Lấy danh sách ID từ kết quả
        List<Long> shoeIds = new ArrayList<>();
        for (Shoes s : shoesPage.getContent()) {
            shoeIds.add(s.getShoeId());
        }

        // Bước 3: Lấy chi tiết giày theo IDs (chỉ kèm images)
        List<Shoes> shoesList = new ArrayList<>();
        Map<Long, Shoes> shoesWithVariantsMap = new HashMap<>();
        
        if (!shoeIds.isEmpty()) {
            shoesList = shoesRepository.findAllByIdsWithImages(shoeIds);
            
            // Lấy variants riêng để tính stock (query riêng tránh tích Descartes)
            List<Shoes> shoesWithVariants = shoesRepository.findAllByIdsWithVariants(shoeIds);
            for (Shoes s : shoesWithVariants) {
                shoesWithVariantsMap.put(s.getShoeId(), s);
            }
            
            // Merge variants vào shoesList
            for (Shoes s : shoesList) {
                Shoes withVariants = shoesWithVariantsMap.get(s.getShoeId());
                if (withVariants != null) {
                    s.setVariants(withVariants.getVariants());
                }
            }
            
            // Sắp xếp lại theo thứ tự của shoeIds (vì database không đảm bảo thứ tự)
            List<Shoes> sortedList = new ArrayList<>();
            for (Long id : shoeIds) {
                for (Shoes s : shoesList) {
                    if (s.getShoeId().equals(id)) {
                        sortedList.add(s);
                        break;
                    }
                }
            }
            shoesList = sortedList;
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
        // Lấy shoes với images và category
        Shoes shoes = shoesRepository.findByIdWithImages(shoeId)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy sản phẩm ID: " + shoeId));

        // Lấy thêm variants (query riêng để tránh tích Descartes)
        // Cần gán lại vào biến shoes để có variants
        Shoes shoesWithVariants = shoesRepository.findByIdWithVariants(shoeId)
                .orElse(shoes);
        
        // Gán variants từ query thứ 2 vào shoes entity
        shoes.setVariants(shoesWithVariants.getVariants());

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
                .categoryId(categoryId)
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
}
