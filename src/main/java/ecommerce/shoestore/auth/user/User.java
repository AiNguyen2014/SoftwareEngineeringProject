package ecommerce.shoestore.auth.user;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

import ecommerce.shoestore.auth.account.Account;
import ecommerce.shoestore.auth.address.Address;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "\"userId\"")
    private Long userId;

    @Column(nullable = false)
    private String fullname;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(name = "\"dateOfBirth\"", nullable = false)
    private LocalDate dateOfBirth;

    @Enumerated(EnumType.STRING)
    private UserGender gender;

    @Column(nullable = false, unique = true)
    private String phone;

    @Column(columnDefinition = "TEXT", nullable = true)
    private String avatar;

    @Column(name = "\"registrationDate\"", updatable = false)
    private LocalDateTime registrationDate;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "\"accountId\"", referencedColumnName = "\"accountId\"")
    private Account account;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "\"addressId\"", referencedColumnName = "\"addressId\"")
    private Address address;

    @PrePersist
    protected void onCreate() {
        this.registrationDate = LocalDateTime.now();
    }
}
