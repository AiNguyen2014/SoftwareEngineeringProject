package ecommerce.shoestore.order;

import ecommerce.shoestore.cart.Cart;
import ecommerce.shoestore.cart.CartRepository;
import ecommerce.shoestore.cartitem.CartItem;
import ecommerce.shoestore.cartitem.CartItemRepository;
import ecommerce.shoestore.promotion.CustomerPromotionService;
import ecommerce.shoestore.promotion.Voucher;
import ecommerce.shoestore.promotion.dto.VoucherValidationResult;
import ecommerce.shoestore.payment.Payment;
import ecommerce.shoestore.payment.PaymentRepository;
import ecommerce.shoestore.payment.PaymentTransaction;
import ecommerce.shoestore.payment.PaymentTransactionRepository;
import ecommerce.shoestore.shoesvariant.ShoesVariant;
import ecommerce.shoestore.shoesvariant.ShoesVariantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService {
    
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ShoesVariantRepository shoesVariantRepository;
    private final CustomerPromotionService customerPromotionService;
    private final PaymentRepository paymentRepository;
    private final PaymentTransactionRepository paymentTransactionRepository;
    private final OrderAddressRepository orderAddressRepository;
    private static final BigDecimal SHIPPING_FEE = new BigDecimal("30000");
    
    @Transactional
    public Order createOrderFromCart(Long userId, Long addressId, String recipientEmail,
                                     String paymentMethod, String note, Cart cart, String voucherCode) {
        
        System.out.println("=== Creating order from cart ===");
        System.out.println("VoucherCode received: [" + voucherCode + "]");
        
        // Tính subTotal từ cart
        BigDecimal subTotal = BigDecimal.ZERO;
        for (CartItem item : cart.getItems()) {
            BigDecimal itemTotal = item.getVariant().getShoes().getBasePrice()
                    .multiply(BigDecimal.valueOf(item.getQuantity()));
            subTotal = subTotal.add(itemTotal);
        }
        
        System.out.println("SubTotal calculated: " + subTotal);
        
        // Validate và tính discountAmount từ voucher
        BigDecimal discountAmount = BigDecimal.ZERO;
        Voucher appliedVoucher = null;
        
        // Chỉ validate voucher nếu có code thực sự (không null, không rỗng, không chỉ có khoảng trắng)
        if (voucherCode != null && !voucherCode.trim().isEmpty()) {
            System.out.println("Validating voucher: " + voucherCode.trim());
            VoucherValidationResult validation = customerPromotionService.validateVoucher(
                    voucherCode.trim(), userId, subTotal);
            
            if (validation.isValid()) {
                appliedVoucher = validation.getVoucher();
                discountAmount = validation.getDiscountAmount();
                System.out.println("Voucher valid! Discount: " + discountAmount);
            } else {
                System.out.println("Voucher validation failed: " + validation.getErrorMessage());
                throw new IllegalArgumentException(validation.getErrorMessage());
            }
        } else {
            System.out.println("No voucher code provided, skipping validation");
        }

        // Tính totalAmount
        BigDecimal totalAmount = subTotal.add(SHIPPING_FEE).subtract(discountAmount);

        // Lấy thông tin địa chỉ để điền recipient fields
        OrderAddress address = orderAddressRepository.findById(addressId)
                .orElseThrow(() -> new RuntimeException("Địa chỉ không tồn tại"));

        // Generate order code
        String orderCode = "ORDER" + System.currentTimeMillis();

        // Tạo Order
        Order order = new Order();
        order.setUserId(userId);
        order.setOrderAddressId(addressId);
        order.setRecipientEmail(recipientEmail);
        order.setRecipientName(address.getRecipientName());
        order.setRecipientPhone(address.getRecipientPhone());
        order.setRecipientAddress(
                address.getProvince() + ", "
                + address.getDistrict() + ", "
                + address.getCommune() + ", "
                + address.getStreetDetail()
        );
        order.setSubTotal(subTotal);
        order.setShippingFee(SHIPPING_FEE);
        order.setDiscountAmount(discountAmount);
        order.setTotalAmount(totalAmount);
        // Use payment method value directly from form (COD or TRANSFER)
        order.setPaymentMethod(paymentMethod);
        order.setNote(note);
        order.setStatus(OrderStatus.PENDING);

        // Set payment status based on payment method
        if ("VNPAY".equals(paymentMethod)) {
            order.setPaymentStatus("UNPAID");
        } else {
            order.setPaymentStatus("UNPAID");
        }

        order = orderRepository.save(order);
        
        // Create PaymentTransaction and Payment for VNPAY
        if ("VNPAY".equals(paymentMethod)) {
            PaymentTransaction transaction = new PaymentTransaction();
            transaction.setOrderId(order.getOrderId());
            transaction.setAmount(totalAmount);
            transaction.setPaymentMethod("VNPAY");
            transaction.setStatus("PENDING");
            transaction.setVnpTxnRef(order.getOrderId().toString());
            paymentTransactionRepository.save(transaction);
            System.out.println("Created PaymentTransaction for Order ID: " + order.getOrderId());
            
            // Create Payment record
            Payment payment = new Payment();
            payment.setOrderId(order.getOrderId());
            payment.setAmount(totalAmount);
            payment.setProvider("VNPAY");
            payment.setCurrency("VND");
            payment.setStatus("PENDING");
            paymentRepository.save(payment);
            System.out.println("Created Payment for Order ID: " + order.getOrderId());
        }
        
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
        
        // Áp dụng voucher vào order nếu có
        if (appliedVoucher != null) {
            customerPromotionService.applyVoucherToOrder(order, appliedVoucher, userId);
        }
        
        // Xóa cart
        cartRepository.delete(cart);
        
        return order;
    }
    
    @Transactional
    public Order createOrderBuyNow(Long userId, Long addressId, String recipientEmail,
                                   String paymentMethod, String note,
                                   Long variantId, Integer quantity, String voucherCode) {
        
        System.out.println("=== Creating order from BUY NOW ===");
        System.out.println("VoucherCode received: [" + voucherCode + "]");
        
        ShoesVariant variant = shoesVariantRepository.findById(variantId)
                .orElseThrow(() -> new RuntimeException("Variant not found"));
        
        // Tính subTotal
        BigDecimal subTotal = variant.getShoes().getBasePrice()
                .multiply(BigDecimal.valueOf(quantity));
        
        System.out.println("SubTotal calculated: " + subTotal);
        
        // Validate và tính discountAmount từ voucher
        BigDecimal discountAmount = BigDecimal.ZERO;
        Voucher appliedVoucher = null;
        
        // Chỉ validate voucher nếu có code thực sự
        if (voucherCode != null && !voucherCode.trim().isEmpty()) {
            System.out.println("Validating voucher: " + voucherCode.trim());
            VoucherValidationResult validation = customerPromotionService.validateVoucher(
                    voucherCode.trim(), userId, subTotal);
            
            if (validation.isValid()) {
                appliedVoucher = validation.getVoucher();
                discountAmount = validation.getDiscountAmount();
                System.out.println("Voucher valid! Discount: " + discountAmount);
            } else {
                System.out.println("Voucher validation failed: " + validation.getErrorMessage());
                throw new IllegalArgumentException(validation.getErrorMessage());
            }
        } else {
            System.out.println("No voucher code provided, skipping validation");
        }
        
        // Tính totalAmount
        BigDecimal totalAmount = subTotal.add(SHIPPING_FEE).subtract(discountAmount);

        // Lấy thông tin địa chỉ để điền recipient fields
        OrderAddress address = orderAddressRepository.findById(addressId)
                .orElseThrow(() -> new RuntimeException("Địa chỉ không tồn tại"));

        // Generate order code
        String orderCode = "ORDER" + System.currentTimeMillis();

        // Tạo Order
        Order order = new Order();
        order.setUserId(userId);
        order.setOrderAddressId(addressId);
        order.setRecipientEmail(recipientEmail);
        order.setRecipientName(address.getRecipientName());
        order.setRecipientPhone(address.getRecipientPhone());
        order.setRecipientAddress(
                address.getProvince() + ", "
                + address.getDistrict() + ", "
                + address.getCommune() + ", "
                + address.getStreetDetail()
        );
        order.setSubTotal(subTotal);
        order.setShippingFee(SHIPPING_FEE);
        order.setDiscountAmount(discountAmount);
        order.setTotalAmount(totalAmount);
        // Use payment method value directly from form (COD or TRANSFER)
        order.setPaymentMethod(paymentMethod);
        order.setOrderCode(orderCode);
        order.setNote(note);
        order.setStatus(OrderStatus.PENDING);

        // Set payment status based on payment method
        if ("VNPAY".equals(paymentMethod)) {
            order.setPaymentStatus("UNPAID");
        } else {
            order.setPaymentStatus("UNPAID");
        }

        order = orderRepository.save(order);
        
        // Create PaymentTransaction and Payment for VNPAY
        if ("VNPAY".equals(paymentMethod)) {
            PaymentTransaction transaction = new PaymentTransaction();
            transaction.setOrderId(order.getOrderId());
            transaction.setAmount(totalAmount);
            transaction.setPaymentMethod("VNPAY");
            transaction.setStatus("PENDING");
            transaction.setVnpTxnRef(order.getOrderId().toString());
            paymentTransactionRepository.save(transaction);
            System.out.println("Created PaymentTransaction for Order ID: " + order.getOrderId());
            
            // Create Payment record
            Payment payment = new Payment();
            payment.setOrderId(order.getOrderId());
            payment.setAmount(totalAmount);
            payment.setProvider("VNPAY");
            payment.setCurrency("VND");
            payment.setStatus("PENDING");
            paymentRepository.save(payment);
            System.out.println("Created Payment for Order ID: " + order.getOrderId());
        }
        
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
        
        // Áp dụng voucher vào order nếu có
        if (appliedVoucher != null) {
            customerPromotionService.applyVoucherToOrder(order, appliedVoucher, userId);
        }
        
        return order;
    }
    
    public Order getOrderById(Long orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));
    }
    
    public List<OrderItem> getOrderItems(Long orderId) {
        return orderItemRepository.findByOrderId(orderId);
    }
    
    @Transactional
    public Order createOrderFromSelectedItems(Long userId, Long addressId, String recipientEmail,
            String paymentMethod, String note,
            List<CartItem> selectedItems, String voucherCode) {

        System.out.println("=== Creating order from selected items ===");
        System.out.println("VoucherCode received: [" + voucherCode + "]");
        
        // Tính subTotal từ items được chọn
        BigDecimal subTotal = BigDecimal.ZERO;
        for (CartItem item : selectedItems) {
            BigDecimal itemTotal = item.getUnitPrice()
                    .multiply(BigDecimal.valueOf(item.getQuantity()));
            subTotal = subTotal.add(itemTotal);
        }
        
        System.out.println("SubTotal calculated: " + subTotal);
        
        // Validate và tính discountAmount từ voucher
        BigDecimal discountAmount = BigDecimal.ZERO;
        Voucher appliedVoucher = null;
        
        // Chỉ validate voucher nếu có code thực sự
        if (voucherCode != null && !voucherCode.trim().isEmpty()) {
            System.out.println("Validating voucher: " + voucherCode.trim());
            VoucherValidationResult validation = customerPromotionService.validateVoucher(
                    voucherCode.trim(), userId, subTotal);
            
            if (validation.isValid()) {
                appliedVoucher = validation.getVoucher();
                discountAmount = validation.getDiscountAmount();
                System.out.println("Voucher valid! Discount: " + discountAmount);
            } else {
                System.out.println("Voucher validation failed: " + validation.getErrorMessage());
                throw new IllegalArgumentException(validation.getErrorMessage());
            }
        } else {
            System.out.println("No voucher code provided, skipping validation");
        }

        BigDecimal totalAmount = subTotal.add(SHIPPING_FEE).subtract(discountAmount);

        // Tạo Order
        Order order = new Order();
        order.setUserId(userId);
        order.setOrderAddressId(addressId);
        order.setRecipientEmail(recipientEmail);
        order.setSubTotal(subTotal);
        order.setShippingFee(SHIPPING_FEE);
        order.setDiscountAmount(discountAmount);
        order.setTotalAmount(totalAmount);
        order.setPaymentMethod(paymentMethod);
        order.setNote(note);
        order.setStatus(OrderStatus.PENDING);

        order = orderRepository.save(order);
        
        // Create PaymentTransaction and Payment for VNPAY
        if ("VNPAY".equals(paymentMethod)) {
            PaymentTransaction transaction = new PaymentTransaction();
            transaction.setOrderId(order.getOrderId());
            transaction.setAmount(totalAmount);
            transaction.setPaymentMethod("VNPAY");
            transaction.setStatus("PENDING");
            transaction.setVnpTxnRef(order.getOrderId().toString());
            paymentTransactionRepository.save(transaction);
            System.out.println("Created PaymentTransaction for Order ID: " + order.getOrderId());
            
            // Create Payment record
            Payment payment = new Payment();
            payment.setOrderId(order.getOrderId());
            payment.setAmount(totalAmount);
            payment.setProvider("VNPAY");
            payment.setCurrency("VND");
            payment.setStatus("PENDING");
            paymentRepository.save(payment);
            System.out.println("Created Payment for Order ID: " + order.getOrderId());
        }

        // Tạo OrderItems CHỈ cho items được chọn
        for (CartItem item : selectedItems) {
            ShoesVariant variant = item.getVariant();

            OrderItem orderItem = new OrderItem();
            orderItem.setOrderId(order.getOrderId());
            orderItem.setShoeId(variant.getShoes().getShoeId());
            orderItem.setQuantity((long) item.getQuantity());
            orderItem.setProductName(variant.getShoes().getName());
            orderItem.setVariantInfo("Size: " + variant.getSize() + ", Color: " + variant.getColor());
            orderItem.setUnitPrice(item.getUnitPrice());
            orderItem.setShopDiscount(BigDecimal.ZERO);
            orderItem.setItemTotal(item.getUnitPrice()
                    .multiply(BigDecimal.valueOf(item.getQuantity())));
            orderItemRepository.save(orderItem);
        }
        // Áp dụng voucher vào order nếu có
        if (appliedVoucher != null) {
            customerPromotionService.applyVoucherToOrder(order, appliedVoucher, userId);
        }

        // === XÓA CART ITEMS ĐÃ ĐẶT HÀNG ===
        List<Long> cartItemIdsToRemove = selectedItems.stream()
                .map(CartItem::getCartItemId)
                .toList();

        System.out.println("Deleting selected cart items: " + cartItemIdsToRemove);
        // Use batch delete for immediate removal and better consistency
        cartItemRepository.deleteAllByIdInBatch(cartItemIdsToRemove);
        // Ensure delete is flushed before returning
        cartItemRepository.flush();
        System.out.println("Deleted selected cart items successfully");

        return order;
    }
    //Thêm hàm cập nhật trạng thái đơn hàng
    public void updateOrderStatus(Long orderId, OrderStatus newStatus) {
    Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng ID: " + orderId));

    if (order.getStatus() == OrderStatus.CANCELLED) {
        throw new RuntimeException("Đơn hàng đã bị huỷ, không thể cập nhật!");
    }
    if (order.getStatus() == OrderStatus.COMPLETED) {
        throw new RuntimeException("Đã giao thành công!");
    }

    order.setStatus(newStatus);
    orderRepository.save(order);
}
}
