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
    private String getVietnameseStatus(String status) {
        if (status == null) return "Không xác định";
        
        switch (status.toUpperCase()) {
            case "PENDING": return "Chờ xử lý";
            case "CONFIRMED": return "Đã xác nhận"; 
            case "SHIPPING": return "Đang giao hàng";
            case "COMPLETED": return "Hoàn thành";
            case "CANCELLED": return "Đã hủy";
            // Thêm các trạng thái mở rộng
            case "WAITING_PAYMENT": return "Chờ thanh toán";
            case "PAID": return "Đã thanh toán";
            case "WAITING_CONFIRMATION": return "Chờ xác nhận";
            case "PACKING": return "Đang đóng gói";
            case "COMPLETE_DELIVERY": return "Giao hàng thành công";
            case "CANCELED": return "Đã hủy";
            case "REQUEST_REFUND": return "Yêu cầu hoàn tiền";
            case "REFUND": return "Đã hoàn tiền";
            default: return status;
        }
    }
    
    /**
     * Lấy CSS class cho trạng thái
     */
    private String getStatusColorClass(String status) {
        if (status == null) return "secondary";
        
        switch (status.toUpperCase()) {
            case "PENDING": return "warning";
            case "CONFIRMED": return "info"; 
            case "SHIPPING": return "primary";
            case "COMPLETED": return "success";
            case "CANCELLED": return "danger";
            default: return "secondary";
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
        
        System.out.println("DEBUG: Looking for orders with userId: " + customerId);
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
    public void addOrderStatusChange(Long orderId, String oldStatus, String newStatus, 
                                   String changedBy, String comment) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy đơn hàng"));
        
        // Cập nhật trạng thái đơn hàng
        order.setStatus(newStatus);
        orderRepository.save(order);
        
        // Tạo tracking log
        OrderTrackingLog trackingLog = OrderTrackingLog.builder()
                .orderId(order.getOrderId())
                .oldStatus(oldStatus)
                .newStatus(newStatus)
                .changedAt(LocalDateTime.now()) // Explicitly set changedAt
                .changedBy(changedBy)
                .comment(comment)
                .build();
        
        trackingLogRepository.save(trackingLog);
    }
    
    /**
     * Lấy tất cả đơn hàng (cho admin)
     */
    public Page<OrderHistoryDto> getAllOrders(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Order> orders = orderRepository.findAll(pageable);
        
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
            OrderStatus orderStatus = OrderStatus.WAITING_CONFIRMATION; // Default value
            try {
                if (trackingLog.getNewStatus() != null && !trackingLog.getNewStatus().trim().isEmpty()) {
                    String status = trackingLog.getNewStatus().toUpperCase().trim();
                    // Map PENDING to WAITING_CONFIRMATION for enum compatibility
                    if ("PENDING".equals(status)) {
                        status = "WAITING_CONFIRMATION";
                    }
                    orderStatus = OrderStatus.valueOf(status);
                }
            } catch (IllegalArgumentException e) {
                System.err.println("WARNING: Invalid newStatus '" + trackingLog.getNewStatus() + 
                                 "' for tracking log " + trackingLog.getLogId() + ". Using default WAITING_CONFIRMATION");
            }
            
            return OrderHistoryDto.builder()
                    .orderId(order.getOrderId())
                    .customerName(order.getRecipientEmail() != null ? order.getRecipientEmail() : "Customer") 
                    .customerEmail(order.getRecipientEmail() != null ? order.getRecipientEmail() : "customer@example.com")  
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
                    .status(OrderStatus.WAITING_CONFIRMATION)
                    .subTotal(BigDecimal.ZERO)
                    .discountAmount(BigDecimal.ZERO)
                    .totalAmount(BigDecimal.ZERO)
                    .note("Lỗi xử lý dữ liệu")
                    .build();
        }
    }

    private OrderHistoryDto convertToHistoryDto(Order order) {
        try {
            // Debug: Log conversion
            System.out.println("DEBUG Converting order: " + order.getOrderId() + " with status: " + order.getStatus());
            
            // Xử lý OrderStatus safely
            OrderStatus orderStatus = OrderStatus.WAITING_CONFIRMATION; // Default value
            try {
                if (order.getStatus() != null && !order.getStatus().trim().isEmpty()) {
                    String status = order.getStatus().toUpperCase().trim();
                    // Map PENDING to WAITING_CONFIRMATION for enum compatibility
                    if ("PENDING".equals(status)) {
                        status = "WAITING_CONFIRMATION";
                    }
                    orderStatus = OrderStatus.valueOf(status);
                }
            } catch (IllegalArgumentException e) {
                System.err.println("WARNING: Invalid status '" + order.getStatus() + "' for order " + order.getOrderId() + ". Using default WAITING_CONFIRMATION");
            }
            
            return OrderHistoryDto.builder()
                    .orderId(order.getOrderId())
                    .customerName(order.getRecipientEmail() != null ? order.getRecipientEmail() : "Customer") 
                    .customerEmail(order.getRecipientEmail() != null ? order.getRecipientEmail() : "customer@example.com")  
                    .createAt(order.getCreateAt())
                    .status(orderStatus) // Sử dụng orderStatus đã xử lý
                    .subTotal(order.getSubTotal() != null ? order.getSubTotal() : BigDecimal.ZERO)
                    .discountAmount(order.getDiscountAmount() != null ? order.getDiscountAmount() : BigDecimal.ZERO)
                    .totalAmount(order.getTotalAmount() != null ? order.getTotalAmount() : BigDecimal.ZERO)
                    .note(order.getNote())
                    .build();
        } catch (Exception e) {
            System.err.println("ERROR converting order " + order.getOrderId() + ": " + e.getMessage());
            e.printStackTrace();
            
            // Return a fallback DTO instead of null
            return OrderHistoryDto.builder()
                    .orderId(order.getOrderId())
                    .customerName("Customer")
                    .customerEmail("customer@example.com")
                    .createAt(order.getCreateAt() != null ? order.getCreateAt() : LocalDateTime.now())
                    .status(OrderStatus.WAITING_CONFIRMATION)
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
                .changeAt(log.getChangedAt())
                .changedBy(log.getChangedBy())
                .comment(log.getComment())
                .build();
    }
}