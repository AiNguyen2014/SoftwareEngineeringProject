package ecommerce.shoestore.payment;

public enum PaymentStatus {
    PENDING,    // Chờ thanh toán
    SUCCESS,    // Thanh toán thành công
    FAILED,     // Thanh toán thất bại
    CANCELLED   // Hủy thanh toán
}
