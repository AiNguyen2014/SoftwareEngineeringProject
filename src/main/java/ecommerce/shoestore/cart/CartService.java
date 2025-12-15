package ecommerce.shoestore.cart;

import ecommerce.shoestore.auth.user.User;
import ecommerce.shoestore.cartitem.CartItem;
import ecommerce.shoestore.shoes.Shoes;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class CartService {

    private final CartRepository cartRepository;

    /**
     * Lấy cart của customer, nếu chưa có thì tạo mới
     */
    public Cart getOrCreateCart(User customer) {
        return cartRepository.findByCustomer(customer)
                .orElseGet(() -> cartRepository.save(new Cart(customer)));
    }

    /**
     * Thêm sản phẩm vào giỏ hàng
     */
    public void addItem(User customer, Shoes shoes, int quantity) {
        Cart cart = getOrCreateCart(customer);

        // kiểm tra đã có item chưa
        for (CartItem item : cart.getItems()) {
            if (item.getShoes().getShoeId().equals(shoes.getShoeId())) {
                item.increaseQuantity(quantity);
                return;
            }
        }

        // chưa có → tạo CartItem mới
        CartItem newItem = CartItem.create(cart, shoes, quantity);
        cart.getItems().add(newItem);

    }

    /**
     * Xóa một sản phẩm khỏi giỏ
     */
    public void removeItem(User customer, Long shoeId) {
        Cart cart = getOrCreateCart(customer);
        cart.removeItem(shoeId);
    }

    /**
     * Xóa toàn bộ giỏ hàng
     */
    public void clearCart(User customer) {
        Cart cart = getOrCreateCart(customer);
        cart.getItems().clear();
    }
}
