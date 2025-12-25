package ecommerce.shoestore.admin.product.dto;

import ecommerce.shoestore.shoes.ShoesType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateShoesRequest {

    @NotBlank(message = "Tên sản phẩm không được để trống")
    private String name;

    @NotBlank(message = "Thương hiệu không được để trống")
    private String brand;

    @NotNull(message = "Loại giày không được để trống")
    private ShoesType type;

    @NotNull(message = "Giá không được để trống")
    @Positive(message = "Giá phải lớn hơn 0")
    private BigDecimal basePrice;

    private String description;

    private String collection;

    @NotNull(message = "Danh mục không được để trống")
    private Long categoryId;

    // Danh sách URL hình ảnh
    private List<ImageDto> images;

    // Danh sách biến thể (màu-size)
    private List<VariantDto> variants;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ImageDto {
        private String url;
        private boolean isThumbnail;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class VariantDto {
        @NotBlank(message = "Màu không được để trống")
        private String color;

        @NotBlank(message = "Size không được để trống")
        private String size;
    }
}
