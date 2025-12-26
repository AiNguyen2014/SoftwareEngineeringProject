package ecommerce.shoestore.auth.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class RegisterRequest {
    private String fullname;
    private String email;
    private String phone;
    private LocalDate dateOfBirth;
    private String gender; 
    
    private String username;
    private String password;
    private String confirmPassword; 
    
    private String province;
    private String district;
    private String commune;
    private String streetDetail;
}