package ecommerce.shoestore.backend.service.impl;

import ecommerce.shoestore.backend.dto.ProductDetailDto;
import ecommerce.shoestore.backend.dto.ProductListDto;
import ecommerce.shoestore.backend.dto.ProductSummaryDto;
import ecommerce.shoestore.backend.entity.Shoes;
import ecommerce.shoestore.backend.entity.ShoesImage;
import ecommerce.shoestore.backend.entity.ShoesVariant;
import ecommerce.shoestore.backend.exception.NotFoundException;
import ecommerce.shoestore.backend.repository.ShoesRepository;
import ecommerce.shoestore.backend.repository.ShoesVariantRepository;
import ecommerce.shoestore.backend.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ShoesRepository shoesRepository;
    private final ShoesVariantRepository variantRepository;

    @Override
    public ProductListDto getProductList(int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size);
        Page<Shoes> shoesPage = shoesRepository.findAll(pageable);

        List<ProductSummaryDto> dtos = shoesPage.getContent().stream()
                .map(this::convertToSummaryDto)
                .collect(Collectors.toList());

        return ProductListDto.builder()
                .products(dtos)
                .currentPage(page)
                .totalPages(shoesPage.getTotalPages())
                .totalItems(shoesPage.getTotalElements())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public ProductDetailDto getProductDetail(Long id) {
        // 1. Fetch Shoes (Nếu không tìm thấy -> Lỗi 404, không phải 500)
        Shoes shoes = shoesRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy sản phẩm ID: " + id));

        // 2. Convert an toàn (Handle Null)
        return convertToDetailDto(shoes);
    }

    private ProductDetailDto convertToDetailDto(Shoes shoes) {
        // Xử lý Category an toàn (tránh NullPointerException)
        String categoryName = "General";
        if (shoes.getCategory() != null) {
            categoryName = shoes.getCategory().getDisplayName();
        }

        // Xử lý Images an toàn
        List<String> imageUrls = new ArrayList<>();
        if (shoes.getImages() != null) {
            imageUrls = shoes.getImages().stream()
                    .map(ShoesImage::getImageUrl)
                    .collect(Collectors.toList());
        }
        // Nếu không có ảnh, thêm ảnh placeholder để giao diện không vỡ
        if (imageUrls.isEmpty()) {
            imageUrls.add("https://placehold.co/600x600?text=No+Image");
        }

        // Xử lý Variants
        Set<String> sizes = new HashSet<>();
        Set<String> colors = new HashSet<>();
        int totalStock = 0;

        if (shoes.getVariants() != null) {
            for (ShoesVariant v : shoes.getVariants()) {
                if (v.getSize() != null) sizes.add(v.getSize());
                if (v.getColor() != null) colors.add(v.getColor());
                if (v.getStock() != null) totalStock += v.getStock();
            }
        }

        return ProductDetailDto.builder()
                .id(shoes.getId())
                .name(shoes.getName())
                .price(shoes.getPrice() != null ? shoes.getPrice() : BigDecimal.ZERO)
                .description(shoes.getDescription())
                .brand(shoes.getBrand())
                .category(categoryName) // Dùng biến đã xử lý null
                .isNew(shoes.getIsNew())
                .imageUrls(imageUrls)
                .sizes(sizes)
                .colors(colors)
                .totalStock(totalStock)
                .relatedProducts(new ArrayList<>()) // Tạm thời để list rỗng
                .build();
    }

    private ProductSummaryDto convertToSummaryDto(Shoes shoes) {
        return ProductSummaryDto.builder()
                .id(shoes.getId())
                .name(shoes.getName())
                .price(shoes.getPrice())
                .thumbnailUrl(shoes.getThumbnailUrl())
                .outOfStock(false)
                .isNew(shoes.getIsNew() != null ? shoes.getIsNew() : false)
                .build();
    }
}