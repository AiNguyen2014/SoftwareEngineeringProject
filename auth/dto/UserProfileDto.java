package ecommerce.shoestore.auth.dto;

import ecommerce.shoestore.auth.user.UserGender;
import lombok.Data;
import java.time.LocalDate;

@Data
public class UserProfileDto {
    private String fullname;
    private String email; 
    private String phone;
    private LocalDate dateOfBirth;
    private UserGender gender;

    private String province;
    private String district;
    private String commune;
    private String streetDetail;
}