package ecommerce.shoestore.order;

import ecommerce.shoestore.auth.user.User;
import ecommerce.shoestore.auth.user.UserRepository;
import ecommerce.shoestore.common.NotFoundException;
import ecommerce.shoestore.order.dto.OrderHistoryDto;
import ecommerce.shoestore.order.dto.OrderTrackingLogDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class OrderHistoryService {
    
    @Autowired
    private OrderHistoryRepository orderRepository;
    
    @Autowired
    private OrderTrackingLogRepository trackingLogRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private OrderSyncService orderSyncService;
    
    /**
     * Chuyển đổi trạng thái đơn hàng sang tiếng Việt
     */
    private String getVietnameseStatus(String statusStr) {
        if (statusStr == null) return "Không xác định";
        
        try {
            // Chấp nhận cả String thường và Enum name
            String statusUpper = statusStr.toUpperCase().trim();
            
            switch (statusUpper) {
                case "PENDING":   return "Chờ xử lý";
                case "CONFIRMED": return "Đã xác nhận";
                case "SHIPPING":  return "Đang giao hàng";
                case "COMPLETED": return "Hoàn thành";
                case "CANCELLED":  return "Đã hủy";
                default:        return statusStr;
            }
        } catch (Exception e) {
            return statusStr;
        }
    }
    
    /**
     * Lấy CSS class cho trạng thái
     */
    /*Mới sửa từ Pb */
    private String getStatusColorClass(String statusStr) {
        if (statusStr == null) return "secondary";
        try {
            String statusUpper = statusStr.toUpperCase().trim();

            switch (statusUpper) {
                case "PENDING":   return "warning text-dark"; // Màu vàng
                case "CONFIRMED": return "info text-dark";    // Màu xanh dương nhạt
                case "SHIPPING":  return "primary"; // Màu xanh dương đậm
                case "COMPLETED": return "success"; // Màu xanh lá
                case "CANCELLED":  return "danger";  // Màu đỏ
                default:        return "secondary";
            }
        } catch (Exception e) {
            return "secondary";
        }
    }
    
    /**
     * Lấy lịch sử đơn hàng của một customer từ OrderTrackingLog
     */
    public Page<OrderHistoryDto> getCustomerOrderHistory(Long customerId, int page, int size) {
        // Kiểm tra customer có tồn tại không
        User customer = userRepository.findById(customerId)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy khách hàng"));
        
        // Sync dữ liệu từ Order sang OrderTrackingLog trước
        orderSyncService.syncOrdersToTrackingLog();
        /*Đã xoá cái print kia bởi PB */
        List<OrderTrackingLog> allTrackingLogs = trackingLogRepository.findAllByOrderByChangeAtDesc();
        
        // Group theo orderId và lấy log mới nhất của mỗi order
        Map<Long, OrderTrackingLog> latestLogsByOrderId = allTrackingLogs.stream()
                .collect(Collectors.groupingBy(
                    OrderTrackingLog::getOrderId,
                    Collectors.collectingAndThen(
                        Collectors.maxBy(Comparator.comparing(OrderTrackingLog::getChangedAt)),
                        Optional::get
                    )
                ));
        
        // Lấy các orders của customer và tạo DTO từ tracking logs
        List<OrderHistoryDto> customerOrderHistory = new ArrayList<>();
        
        for (OrderTrackingLog latestLog : latestLogsByOrderId.values()) {
            try {
                // Lấy thông tin order để kiểm tra userId
                Order order = orderRepository.findById(latestLog.getOrderId()).orElse(null);
                if (order != null && order.getUserId().equals(customerId)) {
                    OrderHistoryDto dto = convertFromTrackingLogToHistoryDto(latestLog, order);
                    if (dto != null) {
                        customerOrderHistory.add(dto);
                    }
                }
            } catch (Exception e) {
                System.err.println("ERROR processing tracking log: " + e.getMessage());
            }
        }
        
        // Sắp xếp theo thời gian changedAt mới nhất
        customerOrderHistory.sort((a, b) -> b.getCreateAt().compareTo(a.getCreateAt()));
        
        // Tạo Page từ List
        Pageable pageable = PageRequest.of(page, size);
        int start = Math.min((int) pageable.getOffset(), customerOrderHistory.size());
        int end = Math.min((start + pageable.getPageSize()), customerOrderHistory.size());
        List<OrderHistoryDto> pageContent = customerOrderHistory.subList(start, end);
        
        // Debug: Log database query result
        System.out.println("DEBUG Service: Found " + customerOrderHistory.size() + " orders from tracking logs for userId " + customerId);
        
        return new PageImpl<>(pageContent, pageable, customerOrderHistory.size());
    }
    
    /**
     * Lấy chi tiết tracking log của một đơn hàng
     */
    public OrderHistoryDto getOrderWithTrackingLogs(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy đơn hàng"));
        
        List<OrderTrackingLog> trackingLogs = trackingLogRepository.findByOrderId(orderId);
        
        OrderHistoryDto dto = convertToHistoryDto(order);
        dto.setTrackingLogs(trackingLogs.stream()
                .map(this::convertToTrackingLogDto)
                .collect(Collectors.toList()));
        
        return dto;
    }
    
    /**
     * Thêm tracking log mới khi thay đổi trạng thái đơn hàng
     */
    @Transactional
    public void addOrderStatusChange(Long orderId, String oldStatus, String newStatusStr, 
                                   String changedBy, String comment) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy đơn hàng"));
        // Chuyển đổi oldStatus và newStatus từ String sang Enum nếu cần (PB mới sửa)
        try {
            OrderStatus newStatusEnum = OrderStatus.valueOf(newStatusStr);
            order.setStatus(newStatusEnum);
        } catch (IllegalArgumentException e) {
            // Nếu string không khớp enum, cố gắng map thủ công hoặc giữ nguyên nếu cần thiết
            // Nhưng Order.status là Enum nên bắt buộc phải khớp
            System.err.println("Lỗi convert status: " + newStatusStr);
            throw new RuntimeException("Trạng thái không hợp lệ: " + newStatusStr);
        }
        // Cập nhật trạng thái đơn hàng
        orderRepository.save(order);
        
        // Tạo tracking log (Có chỉnh lại tên biến)
        OrderTrackingLog trackingLog = OrderTrackingLog.builder()
                .orderId(order.getOrderId())
                .oldStatus(oldStatus)
                .newStatus(newStatusStr)
                .changedAt(LocalDateTime.now()) // Explicitly set changedAt
                .changedBy(changedBy)
                .comment(comment)
                .build();
        
        trackingLogRepository.save(trackingLog);
    }
    
    /**
     * Lấy tất cả đơn hàng (cho admin)
     */
    public Page<OrderHistoryDto> getAllOrders(String statusStr, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Order> orders;
        if (statusStr == null || statusStr.trim().isEmpty() || statusStr.equals("ALL")) {
            orders = orderRepository.findAll(pageable);
        } else {
            try {
                // Chuyển String sang Enum
                OrderStatus status = OrderStatus.valueOf(statusStr.toUpperCase().trim());
                orders = orderRepository.findByStatus(status, pageable);
            } catch (IllegalArgumentException e) {
                // Nếu status sai -> Lấy tất cả (fallback)
                orders = orderRepository.findAll(pageable);
            }
        }
        return orders.map(this::convertToHistoryDto);
    }
    
    /**
     * Convert từ OrderTrackingLog + Order sang OrderHistoryDto  
     */
    private OrderHistoryDto convertFromTrackingLogToHistoryDto(OrderTrackingLog trackingLog, Order order) {
        try {
            // Debug: Log conversion
            System.out.println("DEBUG Converting from tracking log: " + trackingLog.getLogId() + 
                              " for order: " + order.getOrderId() + 
                              " with newStatus: " + trackingLog.getNewStatus());
            
            // Sử dụng newStatus từ tracking log làm trạng thái hiện tại
            OrderStatus orderStatus = OrderStatus.PENDING; // Default value
            try {
                if (trackingLog.getNewStatus() != null && !trackingLog.getNewStatus().trim().isEmpty()) {
                    String statusStr = trackingLog.getNewStatus().toUpperCase().trim();
                    // Map PENDING for enum compatibility
                    if ("PENDING".equals(statusStr)) {
                        statusStr = "PENDING";
                    }
                    orderStatus = OrderStatus.valueOf(statusStr);
                }
            } catch (IllegalArgumentException e) {
                System.err.println("WARNING: Invalid newStatus '" + trackingLog.getNewStatus() + 
                                 "' for tracking log " + trackingLog.getLogId() + ". Using default PENDING");
            }
            
            // Lấy fullname từ User table via userId
            String customerName = "Customer"; // Default fallback
            String customerEmail = order.getRecipientEmail() != null ? order.getRecipientEmail() : "customer@example.com";
            
            try {
                if (order.getUserId() != null) {
                    User user = userRepository.findById(order.getUserId()).orElse(null);
                    if (user != null) {
                        customerName = user.getFullname() != null ? user.getFullname() : "Customer";
                        // Use actual user email if recipientEmail is null
                        if (customerEmail == null) {
                            customerEmail = user.getEmail();
                        }
                    }
                }
            } catch (Exception e) {
                System.err.println("Warning: Could not fetch user info for userId " + order.getUserId() + ": " + e.getMessage());
            }
            
            return OrderHistoryDto.builder()
                    .orderId(order.getOrderId())
                    .customerName(customerName) 
                    .customerEmail(customerEmail != null ? customerEmail : "N/A")
                    .createAt(trackingLog.getChangedAt()) // Sử dụng changedAt từ tracking log
                    .status(orderStatus) // Sử dụng newStatus từ tracking log
                    .statusDisplay(getVietnameseStatus(trackingLog.getNewStatus())) // Tiếng Việt
                    .statusColorClass(getStatusColorClass(trackingLog.getNewStatus())) // CSS class
                    .subTotal(order.getSubTotal() != null ? order.getSubTotal() : BigDecimal.ZERO)
                    .discountAmount(order.getDiscountAmount() != null ? order.getDiscountAmount() : BigDecimal.ZERO)
                    .totalAmount(order.getTotalAmount() != null ? order.getTotalAmount() : BigDecimal.ZERO)
                    .note(order.getNote())
                    .build();
        } catch (Exception e) {
            System.err.println("ERROR converting tracking log " + trackingLog.getLogId() + ": " + e.getMessage());
            e.printStackTrace();
            
            // Return a fallback DTO instead of null
            return OrderHistoryDto.builder()
                    .orderId(order.getOrderId())
                    .customerName("Customer")
                    .customerEmail("customer@example.com")
                    .createAt(trackingLog.getChangedAt() != null ? trackingLog.getChangedAt() : LocalDateTime.now())
                    .status(OrderStatus.PENDING)
                    .subTotal(BigDecimal.ZERO)
                    .discountAmount(BigDecimal.ZERO)
                    .totalAmount(BigDecimal.ZERO)
                    .note("Lỗi xử lý dữ liệu")
                    .build();
        }
    }
    // Convert từ Order sang OrderHistoryDto
    private OrderHistoryDto convertToHistoryDto(Order order) {
        try {
            OrderStatus statusEnum = order.getStatus() != null ? order.getStatus() : OrderStatus.PENDING;
            String customerName = "Customer"; // Default fallback
            String customerEmail = order.getRecipientEmail() != null ? order.getRecipientEmail() : "customer@example.com";
            
            // Xử lý OrderStatus safely
            try {
                if (order.getUserId() != null) {
                    User user = userRepository.findById(order.getUserId()).orElse(null);
                    if (user != null) {
                        if (user.getFullname() != null) customerName = user.getFullname();
                        if (customerEmail.isEmpty()) customerEmail = user.getEmail();
                    }
                }
            } catch (Exception ex) {
                // Log nhẹ, không chặn luồng
            }
                return OrderHistoryDto.builder()
                        .orderId(order.getOrderId())
                        .customerName(customerName)
                        .customerEmail(customerEmail)  
                        .createAt(order.getCreateAt())
                        .status(statusEnum)
                        .statusDisplay(getVietnameseStatus(statusEnum.name()))
                        .statusColorClass(getStatusColorClass(statusEnum.name()))
                        .subTotal(order.getSubTotal() != null ? order.getSubTotal() : BigDecimal.ZERO)
                        .discountAmount(order.getDiscountAmount() != null ? order.getDiscountAmount() : BigDecimal.ZERO)
                        .totalAmount(order.getTotalAmount() != null ? order.getTotalAmount() : BigDecimal.ZERO)
                        .note(order.getNote())
                        .build();
        } catch (Exception ex) {
            System.err.println("ERROR converting order " + order.getOrderId() + ": " + ex.getMessage());
            ex.printStackTrace();
            
            return OrderHistoryDto.builder()
                .orderId(order.getOrderId())
                .customerName("Error Loading")
                .customerEmail("error@example.com")
                .createAt(LocalDateTime.now())
                .status(OrderStatus.PENDING)
                .subTotal(BigDecimal.ZERO)
                .discountAmount(BigDecimal.ZERO)
                .totalAmount(BigDecimal.ZERO)
                .note("Lỗi xử lý dữ liệu")
                .build();
        }
    }
    
    private OrderTrackingLogDto convertToTrackingLogDto(OrderTrackingLog log) {
        return OrderTrackingLogDto.builder()
                .logId(log.getLogId())
                .orderId(log.getOrderId())
                .oldStatus(log.getOldStatus())
                .newStatus(log.getNewStatus())
                .oldStatusDisplay(getVietnameseStatus(log.getOldStatus())) // Tiếng Việt
                .newStatusDisplay(getVietnameseStatus(log.getNewStatus())) // Tiếng Việt
                .changedAt(log.getChangedAt())
                .changedBy(log.getChangedBy())
                .comment(log.getComment())
                .build();
    }
}