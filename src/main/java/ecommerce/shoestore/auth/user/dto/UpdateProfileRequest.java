package ecommerce.shoestore.auth.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import ecommerce.shoestore.auth.user.UserGender;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.multipart.MultipartFile;
import java.time.LocalDate;

@Data
public class UpdateProfileRequest {
    private String email;
    private String avatar;
    @NotBlank(message = "Họ và tên không được để trống")
    private String fullname;
    @Pattern(regexp = "^[0-9]{10}$", message = "Số điện thoại không hợp lệ")
    private String phone;
    private UserGender gender;
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @Past(message = "Ngày sinh phải nhỏ hơn ngày hiện tại")
    private LocalDate dateOfBirth;
    private MultipartFile avatarFile;
    private String province;
    private String district;
    private String commune;
    private String streetDetail;
}