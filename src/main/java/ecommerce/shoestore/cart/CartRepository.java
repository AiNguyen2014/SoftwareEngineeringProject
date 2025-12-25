package ecommerce.shoestore.cart;

import ecommerce.shoestore.auth.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface CartRepository extends JpaRepository<Cart, Long> {

    Optional<Cart> findByCustomer(User customer);

    @Query("select c from Cart c "
            + "left join fetch c.items i "
            + "left join fetch i.variant v "
            + "left join fetch v.shoes s "
            + "left join fetch s.images "
            + "where c.customer = :customer")
    Optional<Cart> findCartWithItems(@Param("customer") User customer);

    @Query("select coalesce(sum(i.quantity), 0) from Cart c "
            + "join c.items i "
            + "where c.customer = :customer")
    int countItemsByUser(@Param("customer") User customer);
}
