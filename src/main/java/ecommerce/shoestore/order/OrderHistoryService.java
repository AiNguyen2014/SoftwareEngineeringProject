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
    
    private String getVietnameseStatus(String statusStr) {
        if (statusStr == null) return "Không xác định";
        
        switch (statusStr.toUpperCase().trim()) {
            case "PENDING": return "Chờ xác nhận";
            case "CONFIRMED": return "Đã xác nhận";
            case "SHIPPING": return "Đang giao hàng";
            case "COMPLETED": return "Hoàn thành";
            case "CANCELLED": return "Đã hủy";
            default: return statusStr;
        }
    }
    
    private String getStatusColorClass(String statusStr) {
        if (statusStr == null) return "secondary";
        
        switch (statusStr.toUpperCase().trim()) {
            case "PENDING": return "warning";
            case "CONFIRMED": return "info";
            case "SHIPPING": return "primary";
            case "SHIPPED": return "primary";
            case "COMPLETED": return "success";
            case "DELIVERED": return "success";
            case "CANCELLED": return "danger";
            default: return "secondary";
        }
    }
    
    public Page<OrderHistoryDto> getCustomerOrderHistory(Long customerId, int page, int size) {
        User customer = userRepository.findById(customerId)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy khách hàng"));
        
        Pageable pageable = PageRequest.of(page, size);
        Page<Order> customerOrders = orderRepository.findByUserIdOrderByCreateAtDesc(customerId, pageable);
        
        return customerOrders.map(order -> {
            OrderHistoryDto dto = convertToHistoryDto(order);
            
            List<OrderTrackingLog> trackingLogs = trackingLogRepository.findByOrderIdOrderByChangeAtDesc(order.getOrderId());
            if (trackingLogs.isEmpty()) {
                orderSyncService.syncSingleOrderToTrackingLog(order);
            }
            
            return dto;
        });
    }
    
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
    
    @Transactional
    public void addOrderStatusChange(Long orderId, String oldStatus, String newStatusStr, 
                                   String changedBy, String comment) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy đơn hàng"));
        
        OrderStatus newStatusEnum = OrderStatus.valueOf(newStatusStr);
        order.setStatus(newStatusEnum);
        orderRepository.save(order);
        
        OrderTrackingLog trackingLog = OrderTrackingLog.builder()
                .orderId(order.getOrderId())
                .oldStatus(oldStatus)
                .newStatus(newStatusStr)
                .changedAt(LocalDateTime.now())
                .changedBy(changedBy)
                .comment(comment)
                .build();
        
        trackingLogRepository.save(trackingLog);
    }
    
    public Page<OrderHistoryDto> getAllOrders(String statusStr, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Order> orders;
        
        if (statusStr == null || statusStr.trim().isEmpty() || statusStr.equals("ALL")) {
            orders = orderRepository.findAll(pageable);
        } else {
            try {
                OrderStatus status = OrderStatus.valueOf(statusStr.toUpperCase().trim());
                orders = orderRepository.findByStatus(status, pageable);
            } catch (IllegalArgumentException e) {
                orders = orderRepository.findAll(pageable);
            }
        }
        return orders.map(this::convertToHistoryDto);
    }
    
    private OrderHistoryDto convertFromTrackingLogToHistoryDto(OrderTrackingLog trackingLog, Order order) {
        OrderStatus orderStatus = OrderStatus.PENDING;
        
        if (trackingLog.getNewStatus() != null && !trackingLog.getNewStatus().trim().isEmpty()) {
            try {
                orderStatus = OrderStatus.valueOf(trackingLog.getNewStatus().toUpperCase().trim());
            } catch (IllegalArgumentException e) {
                orderStatus = OrderStatus.PENDING;
            }
        }
        
        String customerName = "Customer";
        String customerEmail = order.getRecipientEmail() != null ? order.getRecipientEmail() : "customer@example.com";
        
        if (order.getUserId() != null) {
            User user = userRepository.findById(order.getUserId()).orElse(null);
            if (user != null) {
                customerName = user.getFullname() != null ? user.getFullname() : "Customer";
                if (customerEmail == null) {
                    customerEmail = user.getEmail();
                }
            }
        }
        
        return OrderHistoryDto.builder()
                .orderId(order.getOrderId())
                .customerName(customerName)
                .customerEmail(customerEmail != null ? customerEmail : "N/A")
                .createAt(trackingLog.getChangedAt())
                .status(orderStatus)
                .statusDisplay(getVietnameseStatus(trackingLog.getNewStatus()))
                .statusColorClass(getStatusColorClass(trackingLog.getNewStatus()))
                .subTotal(order.getSubTotal() != null ? order.getSubTotal() : BigDecimal.ZERO)
                .discountAmount(order.getDiscountAmount() != null ? order.getDiscountAmount() : BigDecimal.ZERO)
                .totalAmount(order.getTotalAmount() != null ? order.getTotalAmount() : BigDecimal.ZERO)
                .note(order.getNote())
                .build();
    }
    private OrderHistoryDto convertToHistoryDto(Order order) {
        OrderStatus statusEnum = order.getStatus() != null ? order.getStatus() : OrderStatus.PENDING;
        String customerName = "Customer";
        String customerEmail = order.getRecipientEmail() != null ? order.getRecipientEmail() : "customer@example.com";
        
        if (order.getUserId() != null) {
            User user = userRepository.findById(order.getUserId()).orElse(null);
            if (user != null) {
                if (user.getFullname() != null) customerName = user.getFullname();
                if (customerEmail.isEmpty()) customerEmail = user.getEmail();
            }
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
    }
    
    private OrderTrackingLogDto convertToTrackingLogDto(OrderTrackingLog log) {
        return OrderTrackingLogDto.builder()
                .logId(log.getLogId())
                .orderId(log.getOrderId())
                .oldStatus(log.getOldStatus())
                .newStatus(log.getNewStatus())
                .oldStatusDisplay(getVietnameseStatus(log.getOldStatus()))
                .newStatusDisplay(getVietnameseStatus(log.getNewStatus()))
                .changedAt(log.getChangedAt())
                .changedBy(log.getChangedBy())
                .comment(log.getComment())
                .build();
    }
    
    public String getCurrentOrderStatus(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy đơn hàng"));
        return order.getStatus() != null ? order.getStatus().name() : "PENDING";
    }
    
    public long countOrdersByStatus(String statusStr) {
        if (statusStr == null || statusStr.trim().isEmpty()) {
            return orderRepository.count();
        }
        
        try {
            OrderStatus status = OrderStatus.valueOf(statusStr.toUpperCase().trim());
            return orderRepository.countByStatus(status);
        } catch (IllegalArgumentException e) {
            return 0;
        }
    }
}