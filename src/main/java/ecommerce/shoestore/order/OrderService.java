package ecommerce.shoestore.order;

import ecommerce.shoestore.auth.user.User;
import ecommerce.shoestore.auth.user.UserRepository;
import ecommerce.shoestore.common.NotFoundException;
import ecommerce.shoestore.order.dto.OrderHistoryDto;
import ecommerce.shoestore.order.dto.OrderTrackingLogDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class OrderService {
    
    @Autowired
    private OrderRepository orderRepository;
    
    @Autowired
    private OrderTrackingLogRepository trackingLogRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    /**
     * Lấy lịch sử đơn hàng của một customer
     */
    public Page<OrderHistoryDto> getCustomerOrderHistory(Long customerId, int page, int size) {
        // Kiểm tra customer có tồn tại không
        User customer = userRepository.findById(customerId)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy khách hàng"));
        
        Pageable pageable = PageRequest.of(page, size);
        Page<Order> orders = orderRepository.findByUserIdOrderByCreateAtDesc(customerId, pageable);
        
        return orders.map(this::convertToHistoryDto);
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
    public void addOrderStatusChange(Long orderId, OrderStatus oldStatus, OrderStatus newStatus, 
                                   String changedBy, String comment) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy đơn hàng"));
        
        // Cập nhật trạng thái đơn hàng
        order.setStatus(newStatus);
        orderRepository.save(order);
        
        // Tạo tracking log
        OrderTrackingLog trackingLog = OrderTrackingLog.builder()
                .orderId(order.getOrderId())
                .oldStatus(oldStatus != null ? oldStatus.name() : null)
                .newStatus(newStatus.name())
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
    
    private OrderHistoryDto convertToHistoryDto(Order order) {
        return OrderHistoryDto.builder()
                .orderId(order.getOrderId())
                .customerName("Customer") // Sẽ được load từ userId nếu cần
                .customerEmail("customer@example.com") // Sẽ được load từ userId nếu cần  
                .createAt(order.getCreateAt())
                .status(order.getStatus())
                .subTotal(order.getSubTotal())
                .discountAmount(order.getDiscountAmount())
                .totalAmount(order.getTotalAmount())
                .note(order.getNote())
                .build();
    }
    
    private OrderTrackingLogDto convertToTrackingLogDto(OrderTrackingLog log) {
        return OrderTrackingLogDto.builder()
                .logId(log.getLogId())
                .orderId(log.getOrderId())
                .oldStatus(log.getOldStatus())
                .newStatus(log.getNewStatus())
                .changeAt(log.getChangeAt())
                .changedBy(log.getChangedBy())
                .comment(log.getComment())
                .build();
    }
}