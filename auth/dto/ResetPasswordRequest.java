package ecommerce.shoestore.auth.dto;
import lombok.Data;

@Data
public class ResetPasswordRequest {
    private String email;
    private String code;
    private String newPassword;
    private String confirmPassword;

}