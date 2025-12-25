package ecommerce.shoestore.order;

public enum OrderStatus {
    WAITING_PAYMENT,
    PAID,
    WAITING_CONFIRMATION,
    CONFIRMED,
    PACKING,
    SHIPPING,
    COMPLETE_DELIVERY,
    CANCELED,
    REQUEST_REFUND,
    REFUND
}