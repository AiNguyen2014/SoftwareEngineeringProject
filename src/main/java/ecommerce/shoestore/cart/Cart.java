package ecommerce.shoestore.cart;

import ecommerce.shoestore.auth.user.User;
import jakarta.persistence.*;
import lombok.*;
import ecommerce.shoestore.cartitem.CartItem;
import ecommerce.shoestore.shoes.Shoes;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "cart")
@NoArgsConstructor 
@Getter
public class Cart {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "userId")
    private User customer;

    @OneToMany(
        mappedBy = "cart",
        cascade = CascadeType.ALL,
        orphanRemoval = true
    )

    private List<CartItem> items = new ArrayList<>();

    public Cart(User customer){
        this.customer = customer;
    }

    public void addItem(Shoes shoes, int quantity){
        for(CartItem item : items){
            if (item.getShoes().getShoeId().equals(shoes.getShoeId())){
                item.increaseQuantity(quantity);
                return;
            }
        }
    }

    public void removeItem(Long shoeId){
        items.removeIf(item -> item.getShoes().getShoeId().equals(shoeId));
    }
}
