package ecommerce.shoestore.order;

import ecommerce.shoestore.cart.Cart;
import ecommerce.shoestore.cart.CartRepository;
import ecommerce.shoestore.cartitem.CartItem;
import ecommerce.shoestore.shoesvariant.ShoesVariant;
import ecommerce.shoestore.shoesvariant.ShoesVariantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService {
    
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final CartRepository cartRepository;
    private final ShoesVariantRepository shoesVariantRepository;
    
    private static final BigDecimal SHIPPING_FEE = new BigDecimal("30000");
    
    @Transactional
    public Order createOrderFromCart(Long userId, String recipientName, String recipientPhone, 
                                     String recipientEmail, String recipientAddress, 
                                     String paymentMethod, String note, Cart cart) {
        
        // Tính subTotal từ cart
        BigDecimal subTotal = BigDecimal.ZERO;
        for (CartItem item : cart.getItems()) {
            BigDecimal itemTotal = item.getVariant().getShoes().getBasePrice()
                    .multiply(BigDecimal.valueOf(item.getQuantity()));
            subTotal = subTotal.add(itemTotal);
        }
        
        // Tính discountAmount (có thể tích hợp voucher sau)
        BigDecimal discountAmount = BigDecimal.ZERO;
        
        // Tính totalAmount
        BigDecimal totalAmount = subTotal.add(SHIPPING_FEE).subtract(discountAmount);
        
        // Tạo Order
        Order order = new Order();
        order.setUserId(userId);
        order.setRecipientName(recipientName);
        order.setRecipientPhone(recipientPhone);
        order.setRecipientEmail(recipientEmail);
        order.setRecipientAddress(recipientAddress);
        order.setSubTotal(subTotal);
        order.setShippingFee(SHIPPING_FEE);
        order.setDiscountAmount(discountAmount);
        order.setTotalAmount(totalAmount);
        // Use payment method value directly from form (COD or TRANSFER)
        order.setPaymentMethod(paymentMethod);
        order.setNote(note);
        order.setStatus("PENDING");
        
        order = orderRepository.save(order);
        
        // Tạo OrderItems
        for (CartItem item : cart.getItems()) {
            ShoesVariant variant = item.getVariant();
            
            OrderItem orderItem = new OrderItem();
            orderItem.setOrderId(order.getOrderId());
            orderItem.setShoeId(variant.getShoes().getShoeId());
            orderItem.setQuantity((long) item.getQuantity());
            orderItem.setProductName(variant.getShoes().getName());
            orderItem.setVariantInfo("Size: " + variant.getSize() + ", Color: " + variant.getColor());
            orderItem.setUnitPrice(variant.getShoes().getBasePrice());
            orderItem.setShopDiscount(BigDecimal.ZERO);
            orderItem.setItemTotal(variant.getShoes().getBasePrice()
                    .multiply(BigDecimal.valueOf(item.getQuantity())));
            orderItemRepository.save(orderItem);
        }
        
        // Xóa cart
        cartRepository.delete(cart);
        
        return order;
    }
    
    @Transactional
    public Order createOrderBuyNow(Long userId, String recipientName, String recipientPhone,
                                   String recipientEmail, String recipientAddress,
                                   String paymentMethod, String note,
                                   Long variantId, Integer quantity) {
        
        ShoesVariant variant = shoesVariantRepository.findById(variantId)
                .orElseThrow(() -> new RuntimeException("Variant not found"));
        
        // Tính subTotal
        BigDecimal subTotal = variant.getShoes().getBasePrice()
                .multiply(BigDecimal.valueOf(quantity));
        
        // Tính discountAmount
        BigDecimal discountAmount = BigDecimal.ZERO;
        
        // Tính totalAmount
        BigDecimal totalAmount = subTotal.add(SHIPPING_FEE).subtract(discountAmount);
        
        // Tạo Order
        Order order = new Order();
        order.setUserId(userId);
        order.setRecipientName(recipientName);
        order.setRecipientPhone(recipientPhone);
        order.setRecipientEmail(recipientEmail);
        order.setRecipientAddress(recipientAddress);
        order.setSubTotal(subTotal);
        order.setShippingFee(SHIPPING_FEE);
        order.setDiscountAmount(discountAmount);
        order.setTotalAmount(totalAmount);
        // Use payment method value directly from form (COD or TRANSFER)
        order.setPaymentMethod(paymentMethod);
        order.setNote(note);
        order.setStatus("PENDING");
        
        order = orderRepository.save(order);
        
        // Tạo OrderItem
        OrderItem orderItem = new OrderItem();
        orderItem.setOrderId(order.getOrderId());
        orderItem.setShoeId(variant.getShoes().getShoeId());
        orderItem.setQuantity((long) quantity);
        orderItem.setProductName(variant.getShoes().getName());
        orderItem.setVariantInfo("Size: " + variant.getSize() + ", Color: " + variant.getColor());
        orderItem.setUnitPrice(variant.getShoes().getBasePrice());
        orderItem.setShopDiscount(BigDecimal.ZERO);
        orderItem.setItemTotal(subTotal);
        orderItemRepository.save(orderItem);
        
        return order;
    }
    
    public Order getOrderById(Long orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));
    }
    
    public List<OrderItem> getOrderItems(Long orderId) {
        return orderItemRepository.findByOrderId(orderId);
    }
}
