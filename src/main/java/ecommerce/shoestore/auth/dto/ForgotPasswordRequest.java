package ecommerce.shoestore.auth.dto;
import lombok.Data;

@Data
public class ForgotPasswordRequest {
    private String email;
}