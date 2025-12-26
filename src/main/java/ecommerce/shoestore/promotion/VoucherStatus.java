package ecommerce.shoestore.promotion;

public enum VoucherStatus {
    DRAFT,      // Nháp - chưa đến ngày bắt đầu
    ACTIVE,     // Đang hoạt động
    ENDED,      // Đã kết thúc - quá ngày kết thúc
    CANCELLED   // Bị hủy - bị tắt enabled
}
