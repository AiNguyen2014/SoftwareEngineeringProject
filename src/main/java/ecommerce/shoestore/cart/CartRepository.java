package ecommerce.shoestore.cart;

import ecommerce.shoestore.auth.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface CartRepository extends JpaRepository<Cart, Long> {

    Optional<Cart> findByCustomer(User customer);

    @Query("""
        select distinct c from Cart c
        left join fetch c.items i
        left join fetch i.variant v
        where c.customer = :customer
    """)
    Optional<Cart> findCartWithItems(@Param("customer") User customer);

}
