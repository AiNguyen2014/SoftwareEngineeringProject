package ecommerce.shoestore.cart;

import ecommerce.shoestore.auth.user.User;
import ecommerce.shoestore.cart.dto.CartItemView;
import ecommerce.shoestore.cart.dto.CartSummaryView;
import ecommerce.shoestore.cartitem.CartItem;
import ecommerce.shoestore.cartitem.CartItemRepository;
import ecommerce.shoestore.shoesvariant.ShoesVariant;
import ecommerce.shoestore.shoesvariant.ShoesVariantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ShoesVariantRepository shoesVariantRepository;

    // ================== ADD ITEM ==================
    @Transactional
    public void addItem(User user, Long variantId, int quantity) {

        Cart cart = cartRepository.findByCustomer(user)
                .orElseGet(() -> cartRepository.save(new Cart(user)));

        ShoesVariant variant = shoesVariantRepository.findById(variantId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy biến thể"));

        if (variant.getStock() < quantity) {
            throw new RuntimeException("Không đủ hàng");
        }

        CartItem item = cartItemRepository
                .findByCartAndVariant(cart, variant)
                .orElse(null);

        if (item == null) {
            CartItem newItem = new CartItem(
                    cart,
                    variant,
                    quantity,
                    variant.getShoes().getBasePrice()
            );
            cartItemRepository.save(newItem);
        } else {
            int newQty = item.getQuantity() + quantity;
            if (newQty > variant.getStock()) {
                throw new RuntimeException("Vượt quá tồn kho");
            }
            item.setQuantity(newQty);
        }
    }

    // ================== REMOVE ITEM ==================
    @Transactional
    public void removeItem(User user, Long cartItemId) {

        Cart cart = cartRepository.findByCustomer(user)
                .orElseThrow(() -> new RuntimeException("Cart không tồn tại"));

        CartItem item = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new RuntimeException("Item không tồn tại"));

        if (!item.getCart().equals(cart)) {
            throw new RuntimeException("Item không thuộc cart này");
        }

        cartItemRepository.delete(item);
    }

    // ================== INCREASE ==================
    @Transactional
    public void increaseQuantity(User user, Long cartItemId) {

        Cart cart = cartRepository.findByCustomer(user)
                .orElseThrow(() -> new RuntimeException("Cart không tồn tại"));

        CartItem item = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new RuntimeException("Item không tồn tại"));

        if (!item.getCart().equals(cart)) {
            throw new RuntimeException("Item không thuộc cart này");
        }

        ShoesVariant variant = item.getVariant();
        int newQty = item.getQuantity() + 1;

        if (newQty > variant.getStock()) {
            throw new RuntimeException("Vượt quá tồn kho");
        }

        item.setQuantity(newQty);
    }

    // ================== DECREASE ==================
    @Transactional
    public void decreaseQuantity(User user, Long cartItemId) {

        Cart cart = cartRepository.findByCustomer(user)
                .orElseThrow(() -> new RuntimeException("Cart không tồn tại"));

        CartItem item = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new RuntimeException("Item không tồn tại"));

        if (!item.getCart().equals(cart)) {
            throw new RuntimeException("Item không thuộc cart này");
        }

        int newQty = item.getQuantity() - 1;

        if (newQty <= 0) {
            cartItemRepository.delete(item);
        } else {
            item.setQuantity(newQty);
        }
    }

    // ================== GET CART SUMMARY FOR VIEW ==================
    @Transactional(readOnly = true)
    public CartSummaryView getCartSummaryForView(User user) {

        Cart cart = cartRepository.findCartWithItems(user)
                .orElse(null);

        // Nếu không có cart, return empty cart summary (không tạo mới trong read-only transaction)
        if (cart == null) {
            return new CartSummaryView(List.of(), BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO);
        }

        List<CartItemView> items = cart.getItems().stream().map(item -> {

            var variant = item.getVariant();
            var shoes = variant.getShoes();

            String imageUrl
                    = shoes.getImages() == null || shoes.getImages().isEmpty()
                    ? "/images/default-shoe.jpg"
                    : shoes.getImages().iterator().next().getUrl();

            BigDecimal subtotal = item.getUnitPrice()
                    .multiply(BigDecimal.valueOf(item.getQuantity()));

            return new CartItemView(
                    item.getCartItemId(),
                    shoes.getShoeId(),
                    variant.getVariantId(),
                    shoes.getName(),
                    shoes.getBrand(),
                    variant.getColor().name(),
                    variant.getSize().name(),
                    imageUrl,
                    item.getUnitPrice(),
                    item.getQuantity(),
                    subtotal
            );
        }).toList();

        BigDecimal cartSubtotal = items.stream()
                .map(CartItemView::subtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal shipping = BigDecimal.valueOf(30_000);
        BigDecimal total = cartSubtotal.add(shipping);

        return new CartSummaryView(items, cartSubtotal, shipping, total);
    }

    // ================== GET OR CREATE CART ==================
    @Transactional
    public Cart getOrCreateCart(User user) {
        return cartRepository.findByCustomer(user)
                .orElseGet(() -> cartRepository.save(new Cart(user)));
    }
}