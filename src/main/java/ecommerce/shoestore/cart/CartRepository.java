package ecommerce.shoestore.cart;

import ecommerce.shoestore.auth.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CartRepository extends JpaRepository<Cart, Long> {

    //Mỗi user chỉ có 1 cart
    Optional<Cart> findByCustomer(User customer);
}
