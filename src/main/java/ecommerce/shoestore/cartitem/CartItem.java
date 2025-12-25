package ecommerce.shoestore.cartitem;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

import ecommerce.shoestore.cart.Cart;
import ecommerce.shoestore.shoesvariant.ShoesVariant;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "cartitem", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"cartId", "variantId"})
})

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CartItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "\"cartItemId\"")
    private Long cartItemId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "\"cartId\"", nullable = false)
    private Cart cart;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "\"variantId\"", nullable = false)
    private ShoesVariant variant;

    @Column(nullable = false)
    private int quantity;

    @Column(name = "\"unitPrice\"", nullable = false)
    private BigDecimal unitPrice;

    @Column(name = "\"createdAt\"", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "\"updatedAt\"", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public CartItem(Cart cart, ShoesVariant variant, int quantity, BigDecimal unitPrice) {
        this.cart = cart;
        this.variant = variant;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CartItem cartItem = (CartItem) o;
        return Objects.equals(cartItemId, cartItem.cartItemId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(cartItemId);
    }

}
