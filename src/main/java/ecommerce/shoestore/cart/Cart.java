package ecommerce.shoestore.cart;

import ecommerce.shoestore.auth.user.User;
import ecommerce.shoestore.cartitem.CartItem;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "cart")
@NoArgsConstructor
@Getter
@Setter
public class Cart {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "\"cartId\"")
    private Long cartId;

    @ManyToOne
    @JoinColumn(name = "\"userId\"", nullable = false)
    private User customer;

    @OneToMany(
            mappedBy = "cart",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )

    private List<CartItem> items = new ArrayList<>();

    public Cart(User customer) {
        this.customer = customer;
    }
}
