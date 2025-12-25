package ecommerce.shoestore.order;

import ecommerce.shoestore.order.dto.MockOrderDto;
import ecommerce.shoestore.order.dto.MockTrackingLogDto;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class OrderMockService {
    
    public List<MockOrderDto> getMockCustomerOrderHistory(Long customerId) {
        List<MockOrderDto> orders = new ArrayList<>();
        
        MockOrderDto order1 = new MockOrderDto();
        order1.setOrderId(1001L);
        order1.setCustomerName("Nguyễn Văn A");
        order1.setCustomerEmail("nguyenvana@email.com");
        order1.setStatus("COMPLETE_DELIVERY");
        order1.setTotalAmount(new BigDecimal("2500000"));
        order1.setSubTotal(new BigDecimal("2450000"));
        order1.setDiscountAmount(new BigDecimal("0"));
        order1.setCreateAt(LocalDateTime.now().minusDays(10));
        orders.add(order1);
        
        MockOrderDto order2 = new MockOrderDto();
        order2.setOrderId(1002L);
        order2.setCustomerName("Nguyễn Văn A");
        order2.setCustomerEmail("nguyenvana@email.com");
        order2.setStatus("SHIPPING");
        order2.setTotalAmount(new BigDecimal("1800000"));
        order2.setSubTotal(new BigDecimal("1770000"));
        order2.setDiscountAmount(new BigDecimal("0"));
        order2.setCreateAt(LocalDateTime.now().minusDays(3));
        orders.add(order2);
        
        return orders;
    }
    
    public MockOrderDto getMockOrderWithTrackingLogs(Long orderId) {
        MockOrderDto order = new MockOrderDto();
        order.setOrderId(orderId);
        order.setCustomerName("Nguyễn Văn A");
        order.setStatus("SHIPPING");
        order.setTotalAmount(new BigDecimal("2500000"));
        order.setCreateAt(LocalDateTime.now().minusDays(5));
        
        List<MockTrackingLogDto> trackingLogs = new ArrayList<>();
        
        MockTrackingLogDto log1 = new MockTrackingLogDto();
        log1.setLogId(1L);
        log1.setOrderId(orderId);
        log1.setOldStatus("NONE");
        log1.setNewStatus("WAITING_CONFIRMATION");
        log1.setChangedAt(LocalDateTime.now().minusDays(5));
        log1.setChangedBy("System Auto");
        log1.setComment("Đơn hàng được tạo");
        trackingLogs.add(log1);
        
        order.setTrackingLogs(trackingLogs);
        return order;
    }
    
    public List<MockOrderDto> getMockAllOrders() {
        List<MockOrderDto> allOrders = new ArrayList<>();
        allOrders.addAll(getMockCustomerOrderHistory(1L));
        return allOrders;
    }
    
    public List<MockOrderDto> getOrdersForPage(List<MockOrderDto> allOrders, int page, int size) {
        int start = page * size;
        int end = Math.min(start + size, allOrders.size());
        
        if (start >= allOrders.size()) {
            return new ArrayList<>();
        }
        
        return allOrders.subList(start, end);
    }
    
    public int calculateTotalPages(int totalElements, int size) {
        return (int) Math.ceil((double) totalElements / size);
    }
}