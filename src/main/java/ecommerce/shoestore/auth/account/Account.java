package ecommerce.shoestore.auth.account;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "account")
@Data @NoArgsConstructor @AllArgsConstructor
public class Account {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "\"accountId\"")
    private Long accountId;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    private UserRole role;

    private String verificationCode;

    private Long verificationCodeExpiry; 
    private boolean enabled = false; 
}