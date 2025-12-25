package ecommerce.shoestore.shoes;

import ecommerce.shoestore.common.NotFoundException;
import ecommerce.shoestore.shoes.dto.ShoesDetailDto;
import ecommerce.shoestore.shoes.dto.ShoesListDto;
import ecommerce.shoestore.shoes.dto.ShoesSummaryDto;
import ecommerce.shoestore.shoesimage.ShoesImage;
import ecommerce.shoestore.shoesvariant.ShoesVariant;
import ecommerce.shoestore.shoesvariant.ShoesVariantDto;
import ecommerce.shoestore.shoesvariant.ShoesVariantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.stream.Collectors;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ShoesService {

    private final ShoesRepository shoesRepository;
    private final ShoesVariantRepository variantRepository;

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

        return ShoesSummaryDto.builder()
                .shoeId(shoes.getShoeId())
                .name(shoes.getName())
                .brand(shoes.getBrand())
                .price(shoes.getBasePrice() != null ? shoes.getBasePrice() : BigDecimal.ZERO)
                .thumbnailUrl(thumbnailUrl)
                .outOfStock(stock <= 0)
                .type(shoes.getType() != null ? shoes.getType().name() : null)
                .build();
    }

    private ShoesDetailDto convertToDetailDto(Shoes shoes) {
        String categoryName = shoes.getCategory() != null ? shoes.getCategory().getName() : "General";

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

        Set<String> sizes = new HashSet<>();
        Set<String> colors = new HashSet<>();
        int totalStock = 0;

        if (shoes.getVariants() != null && !shoes.getVariants().isEmpty()) {
            for (ShoesVariant variant : shoes.getVariants()) {
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
}
