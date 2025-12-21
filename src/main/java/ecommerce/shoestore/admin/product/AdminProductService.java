package ecommerce.shoestore.admin.product;

import ecommerce.shoestore.admin.product.dto.AdminShoesDetailDto;
import ecommerce.shoestore.admin.product.dto.AdminShoesListItemDto;
import ecommerce.shoestore.admin.product.dto.CreateShoesRequest;
import ecommerce.shoestore.admin.product.dto.UpdateShoesRequest;
import ecommerce.shoestore.category.Category;
import ecommerce.shoestore.category.CategoryRepository;
import ecommerce.shoestore.common.NotFoundException;
import ecommerce.shoestore.shoes.Shoes;
import ecommerce.shoestore.shoes.ShoesRepository;
import ecommerce.shoestore.shoesimage.ShoesImage;
import ecommerce.shoestore.shoesvariant.ShoesVariant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.util.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ecommerce.shoestore.shoesvariant.Color;
import ecommerce.shoestore.shoesvariant.Size;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminProductService {

    private final ShoesRepository shoesRepository;
    private final CategoryRepository categoryRepository;

    private String normalizeSize(String raw) {
        if (raw == null) return null;
        String trimmed = raw.trim();
        if (trimmed.isEmpty()) return null;
        if (trimmed.toUpperCase().startsWith("SIZE_")) {
            return trimmed.toUpperCase();
        }
        // Nếu chỉ nhập số, chuẩn hóa thành SIZE_<num>
        String digits = trimmed.replaceAll("[^0-9]", "");
        if (!digits.isEmpty()) {
            return "SIZE_" + digits;
        }
        return trimmed.toUpperCase();
    }

    /* =========================
       ADMIN – LIST PRODUCT
       ========================= */
    @Transactional(readOnly = true)
    public Page<AdminShoesListItemDto> getAdminProductList(
            int page,
            int size,
            String keyword,
            Long categoryId,
            String brand,
            String status
    ) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by("shoeId").descending());
        Boolean statusFilter = (status == null || status.isBlank()) ? null : Boolean.valueOf(status);

        Page<Shoes> shoesPage = shoesRepository.searchForAdminBasic(
                keyword,
                categoryId,
                brand,
                statusFilter,
                pageable
        );

        return shoesPage.map(shoes -> AdminShoesListItemDto.builder()
                .shoeId(shoes.getShoeId())
                .name(shoes.getName())
                .brand(shoes.getBrand())
                .categoryName(shoes.getCategory() != null ? shoes.getCategory().getName() : null)
                .basePrice(shoes.getBasePrice())
                .status(shoes.getStatus() != null ? shoes.getStatus() : Boolean.FALSE)
                .build());
    }

    /* =========================
       ADMIN – GET DETAIL FOR EDIT/VIEW
       ========================= */
    @Transactional(readOnly = true)
    public AdminShoesDetailDto getAdminShoesDetail(Long shoeId) {
        Shoes shoes = shoesRepository.findByIdForAdmin(shoeId)
                .orElseGet(() -> shoesRepository.findById(shoeId)
                        .orElseThrow(() -> new NotFoundException("Không tìm thấy sản phẩm ID: " + shoeId)));

        List<AdminShoesDetailDto.ImageDto> imageDtos = shoes.getImages() != null
                ? shoes.getImages().stream()
                .map(img -> AdminShoesDetailDto.ImageDto.builder()
                        .imageId(img.getImageId())
                        .url(img.getUrl())
                        .isThumbnail(img.isThumbnail())
                        .build())
                .collect(Collectors.toList())
                : new ArrayList<>();

        List<AdminShoesDetailDto.VariantDto> variantDtos = shoes.getVariants() != null
            ? shoes.getVariants().stream()
            .map(v -> AdminShoesDetailDto.VariantDto.builder()
                .variantId(v.getVariantId())
                .color(v.getColor() != null ? v.getColor().name() : null)
                .size(v.getSize() != null ? v.getSize().getValue() : null)
                .build())
            .collect(Collectors.toList())
            : new ArrayList<>();

        return AdminShoesDetailDto.builder()
                .shoeId(shoes.getShoeId())
                .name(shoes.getName())
                .brand(shoes.getBrand())
                .type(shoes.getType())
                .basePrice(shoes.getBasePrice())
                .description(shoes.getDescription())
                .collection(shoes.getCollection())
                .categoryId(shoes.getCategory() != null ? shoes.getCategory().getCategoryId() : null)
                .categoryName(shoes.getCategory() != null ? shoes.getCategory().getName() : null)
                .status(shoes.getStatus())
                .images(imageDtos)
                .variants(variantDtos)
                .build();
    }

    /* =========================
       ADMIN – CREATE PRODUCT
       ========================= */
    @Transactional
    public Long createShoes(CreateShoesRequest request) {
        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new NotFoundException("Không tìm thấy danh mục ID: " + request.getCategoryId()));

        Shoes shoes = Shoes.builder()
                .name(request.getName())
                .brand(request.getBrand())
                .type(request.getType())
                .basePrice(request.getBasePrice())
                .description(request.getDescription())
                .collection(request.getCollection())
                .category(category)
                .status(Boolean.TRUE)
                .build();

        shoes = shoesRepository.save(shoes);

        if (request.getImages() != null && !request.getImages().isEmpty()) {
            Set<ShoesImage> images = new HashSet<>();
            for (CreateShoesRequest.ImageDto imgDto : request.getImages()) {
                if (imgDto == null || !StringUtils.hasText(imgDto.getUrl())) {
                    continue; // bỏ qua ảnh không có URL
                }
                ShoesImage image = ShoesImage.builder()
                        .url(imgDto.getUrl())
                        .isThumbnail(imgDto.isThumbnail())
                        .shoes(shoes)
                        .build();
                images.add(image);
            }
            shoes.setImages(images);
        }

        if (request.getVariants() != null && !request.getVariants().isEmpty()) {
            List<CreateShoesRequest.VariantDto> validVariants = new ArrayList<>();

            for (CreateShoesRequest.VariantDto vDto : request.getVariants()) {
                if (vDto == null) {
                    continue;
                }
                String normSize = normalizeSize(vDto.getSize());
                if (!StringUtils.hasText(vDto.getColor()) || !StringUtils.hasText(normSize)) {
                    continue; // bỏ qua biến thể trống hoặc thiếu dữ liệu
                }
                vDto.setSize(normSize); // giữ kích thước đã chuẩn hóa cho các bước sau
                validVariants.add(vDto);
            }

            if (validVariants.isEmpty()) {
                throw new IllegalArgumentException("Vui lòng nhập ít nhất 1 biến thể hợp lệ (màu và size)");
            }

            Set<String> variantKeys = new HashSet<>();
            for (CreateShoesRequest.VariantDto vDto : validVariants) {
                String key = vDto.getColor() + "-" + vDto.getSize();
                if (!variantKeys.add(key)) {
                    throw new IllegalArgumentException("Biến thể " + key + " bị trùng lặp");
                }
            }

            Set<ShoesVariant> variants = new HashSet<>();
            for (CreateShoesRequest.VariantDto vDto : validVariants) {
                ShoesVariant variant = ShoesVariant.builder()
                        .color(Color.valueOf(vDto.getColor().trim().toUpperCase()))
                        .size(Size.valueOf(vDto.getSize().trim().toUpperCase()))
                        .stock(0) // quản lý tồn kho tách riêng, mặc định 0
                        .shoes(shoes)
                        .build();
                variants.add(variant);
            }
            shoes.setVariants(variants);
        }

        shoes = shoesRepository.save(shoes);
        log.info("Admin created product: {} with ID: {}", shoes.getName(), shoes.getShoeId());
        return shoes.getShoeId();
    }

    /* =========================
       ADMIN – UPDATE PRODUCT
       ========================= */
    @Transactional
    public void updateShoes(Long shoeId, UpdateShoesRequest request) {
        Shoes shoes = shoesRepository.findById(shoeId)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy sản phẩm ID: " + shoeId));

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new NotFoundException("Không tìm thấy danh mục ID: " + request.getCategoryId()));

        shoes.setName(request.getName());
        shoes.setBrand(request.getBrand());
        shoes.setType(request.getType());
        shoes.setBasePrice(request.getBasePrice());
        shoes.setDescription(request.getDescription());
        shoes.setCollection(request.getCollection());
        shoes.setCategory(category);

        if (request.getImages() != null) {
            Set<ShoesImage> newImages = new HashSet<>();
            for (UpdateShoesRequest.ImageDto imgDto : request.getImages()) {
                if (imgDto == null || !StringUtils.hasText(imgDto.getUrl())) {
                    continue; // bỏ qua ảnh không có URL
                }
                ShoesImage image = ShoesImage.builder()
                        .imageId(imgDto.getImageId())
                        .url(imgDto.getUrl())
                        .isThumbnail(imgDto.isThumbnail())
                        .shoes(shoes)
                        .build();
                newImages.add(image);
            }
            if (shoes.getImages() == null) {
                shoes.setImages(new HashSet<>());
            }
            shoes.getImages().clear();
            shoes.getImages().addAll(newImages);
        }

        if (request.getVariants() != null) {
            List<UpdateShoesRequest.VariantDto> validVariants = new ArrayList<>();

            for (UpdateShoesRequest.VariantDto vDto : request.getVariants()) {
                if (vDto == null) {
                    continue;
                }
                String normSize = normalizeSize(vDto.getSize());
                if (!StringUtils.hasText(vDto.getColor()) || !StringUtils.hasText(normSize)) {
                    continue; // bỏ biến thể thiếu màu hoặc size
                }
                vDto.setSize(normSize);
                validVariants.add(vDto);
            }

            if (validVariants.isEmpty()) {
                throw new IllegalArgumentException("Vui lòng nhập ít nhất 1 biến thể hợp lệ (màu và size)");
            }

            Set<String> variantKeys = new HashSet<>();
            for (UpdateShoesRequest.VariantDto vDto : validVariants) {
                String key = vDto.getColor() + "-" + vDto.getSize();
                if (!variantKeys.add(key)) {
                    throw new IllegalArgumentException("Biến thể " + key + " bị trùng lặp");
                }
            }

            // Giữ stock cũ nếu có, default 0 cho biến thể mới
            var oldVariants = shoes.getVariants() != null
                    ? shoes.getVariants().stream()
                        .filter(v -> v.getVariantId() != null)
                        .collect(Collectors.toMap(ShoesVariant::getVariantId, v -> v))
                    : java.util.Collections.<Long, ShoesVariant>emptyMap();

            Set<ShoesVariant> newVariants = new HashSet<>();
            for (UpdateShoesRequest.VariantDto vDto : validVariants) {
                Integer stockVal = 0;
                if (vDto.getVariantId() != null && oldVariants.containsKey(vDto.getVariantId())) {
                    stockVal = oldVariants.get(vDto.getVariantId()).getStock();
                }
                ShoesVariant variant = ShoesVariant.builder()
                        .variantId(vDto.getVariantId())
                        .color(Color.valueOf(vDto.getColor().trim().toUpperCase()))
                        .size(Size.valueOf(vDto.getSize().trim().toUpperCase()))
                        .stock(stockVal)
                        .shoes(shoes)
                        .build();
                newVariants.add(variant);
            }
            if (shoes.getVariants() == null) {
                shoes.setVariants(new HashSet<>());
            }
            shoes.getVariants().clear();
            shoes.getVariants().addAll(newVariants);
        }

        shoesRepository.save(shoes);
        log.info("Admin updated product ID: {}", shoeId);
    }

    /* =========================
       ADMIN – CHANGE/TOGGLE PRODUCT STATUS
       ========================= */
    @Transactional
    public void changeProductStatus(Long shoeId, Boolean status) {
        Shoes shoes = shoesRepository.findById(shoeId)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy sản phẩm ID: " + shoeId));

        shoes.setStatus(status);
        shoesRepository.save(shoes);

        String statusText = status ? "ĐANG BÁN" : "NGỪNG BÁN";
        log.info("Admin thay đổi trạng thái sản phẩm ID: {} thành {}", shoeId, statusText);
    }

    @Transactional
    public void toggleStatus(Long shoeId) {
        Shoes shoes = shoesRepository.findById(shoeId)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy sản phẩm ID: " + shoeId));

        boolean current = shoes.getStatus() != null && shoes.getStatus();
        shoes.setStatus(!current);
        shoesRepository.save(shoes);
        log.info("Admin toggled status for product ID: {} to {}", shoeId, !current ? "ĐANG BÁN" : "NGỪNG BÁN");
    }
}
