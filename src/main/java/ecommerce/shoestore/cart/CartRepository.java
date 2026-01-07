package ecommerce.shoestore.cart;

import ecommerce.shoestore.auth.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface CartRepository extends JpaRepository<Cart, Long> {

    @Query("select c from Cart c where c.user = :user")
    Optional<Cart> findByCustomer(@Param("user") User user);

    @Query("select c from Cart c "
            + "left join fetch c.items i "
            + "left join fetch i.variant v "
            + "left join fetch v.shoes s "
            + "left join fetch s.category "
            + "where c.user = :user")
    Optional<Cart> findCartWithItems(@Param("user") User user);

    @Query("select coalesce(sum(i.quantity), 0) from Cart c "
            + "join c.items i "
            + "where c.user = :user")
    int countItemsByUser(@Param("user") User user);
}
