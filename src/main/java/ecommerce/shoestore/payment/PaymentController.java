package ecommerce.shoestore.payment;

import ecommerce.shoestore.order.Order;
import ecommerce.shoestore.order.OrderItem;
import ecommerce.shoestore.order.OrderRepository;
import ecommerce.shoestore.order.OrderService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/payment")
@RequiredArgsConstructor
public class PaymentController {
    
    private final VNPayService vnPayService;
    private final PaymentRepository paymentRepository;
    private final PaymentTransactionRepository paymentTransactionRepository;
    private final OrderRepository orderRepository;
    private final OrderService orderService;
    
    /**
     * Tạo payment và redirect đến VNPay
     * GET/POST /payment/create-vnpay
     */
    @GetMapping("/create-vnpay")
    @Transactional
    public String createVNPayPayment(
            @RequestParam Long orderId,
            HttpServletRequest request,
            HttpSession session,
            RedirectAttributes redirectAttributes) {
        
        try {
            Long userId = (Long) session.getAttribute("USER_ID");
            if (userId == null) {
                return "redirect:/auth/login";
            }
            
            // Lấy thông tin order
            Order order = orderRepository.findById(orderId)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng"));
            
            // Kiểm tra order thuộc về user
            if (!order.getUserId().equals(userId)) {
                throw new RuntimeException("Đơn hàng không hợp lệ");
            }
            
            // Tạo payment record trong bảng payment
            Payment payment = new Payment();
            payment.setOrderId(orderId);
            payment.setProvider("VNPAY");
            payment.setAmount(order.getTotalAmount());
            payment.setStatus("PENDING");
            payment = paymentRepository.save(payment);
            
            // Tạo payment transaction record
            PaymentTransaction transaction = new PaymentTransaction();
            transaction.setOrderId(orderId);
            transaction.setVnpTxnRef(String.valueOf(orderId));
            transaction.setAmount(order.getTotalAmount());
            transaction.setPaymentMethod("VNPAY");
            transaction.setStatus("PENDING");
            transaction = paymentTransactionRepository.save(transaction);
            
            // Tạo payment URL
            String ipAddress = vnPayService.getIpAddress(request);
            String orderInfo = "Thanh toan don hang " + orderId;
            long amount = order.getTotalAmount().longValue();
            
            String paymentUrl = vnPayService.createPaymentUrl(orderId, userId, amount, orderInfo, ipAddress);
            
            return "redirect:" + paymentUrl;
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Không thể tạo thanh toán: " + e.getMessage());
            return "redirect:/order/payment";
        }
    }
    
    /**
     * Xử lý callback từ VNPay
     * GET /payment/vnpay-return
     */
    @GetMapping("/vnpay-return")
    @Transactional
    public String vnpayReturn(
            @RequestParam Map<String, String> params,
            HttpSession session,
            Model model,
            RedirectAttributes redirectAttributes) {
        
        try {
            System.out.println("\n===== VNPAY CALLBACK RECEIVED =====");
            System.out.println("Params: " + params);
            
            // Verify signature
            boolean isValid = vnPayService.verifyPaymentCallback(params);
            if (!isValid) {
                throw new RuntimeException("Chữ ký không hợp lệ");
            }
            
            // Lấy thông tin từ params
            String vnpResponseCode = params.get("vnp_ResponseCode");
            String vnpTxnRef = params.get("vnp_TxnRef"); // orderId
            String vnpTransactionNo = params.get("vnp_TransactionNo");
            String vnpBankCode = params.get("vnp_BankCode");
            String vnpCardType = params.get("vnp_CardType");
            String vnpPayDateStr = params.get("vnp_PayDate");
            
            Long orderId = Long.parseLong(vnpTxnRef);
            
            // Lấy payment transaction record
            PaymentTransaction transaction = paymentTransactionRepository.findByOrderId(orderId)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy giao dịch thanh toán"));
            
            // Lấy payment record
            Payment payment = paymentRepository.findByOrderId(orderId)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy thông tin thanh toán"));
            
            // Lấy order
            Order order = orderRepository.findById(orderId)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng"));
            
            // Parse payment date
            LocalDateTime vnpPayDate = null;
            if (vnpPayDateStr != null && !vnpPayDateStr.isEmpty()) {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
                vnpPayDate = LocalDateTime.parse(vnpPayDateStr, formatter);
            }
            
            // Check response code
            if ("00".equals(vnpResponseCode)) {
                // Thanh toán thành công
                // Dùng native query để update - KHÔNG set field vào entity để tránh Hibernate auto-update
                paymentTransactionRepository.updatePaymentTransaction(
                    transaction.getId(),
                    "SUCCESS",
                    vnpTransactionNo,
                    vnpBankCode,
                    vnpCardType,
                    vnpResponseCode
                );
                
                payment.setStatus("SUCCESS");
                payment.setTransactionCode(vnpTransactionNo);
                payment.setPaidAt(vnpPayDate);
                
                order.setPaymentStatus("PAID");
                order.setTransactionId(vnpTransactionNo);
                order.setPaidAt(vnpPayDate);
                order.setStatus("PENDING"); // Chờ xác nhận từ cửa hàng
                
                paymentRepository.save(payment);
                orderRepository.save(order);
                
                System.out.println("Payment SUCCESS for order: " + orderId);
                
                // Redirect đến trang success
                return "redirect:/payment/success?orderId=" + orderId;
                
            } else {
                // Thanh toán thất bại
                // Dùng native query để update - tránh conflict với trigger
                paymentTransactionRepository.updatePaymentTransaction(
                    transaction.getId(),
                    "FAILED",
                    vnpTransactionNo,
                    vnpBankCode,
                    vnpCardType,
                    vnpResponseCode
                );
                
                payment.setStatus("FAILED");
                order.setPaymentStatus("UNPAID");
                
                paymentRepository.save(payment);
                orderRepository.save(order);
                
                System.out.println("Payment FAILED for order: " + orderId);
                
                String errorMessage = vnPayService.getResponseMessage(vnpResponseCode);
                
                // Redirect đến trang failed
                return "redirect:/payment/failed?orderId=" + orderId + "&message=" + errorMessage;
            }
            
        } catch (Exception e) {
            System.out.println("===== VNPAY CALLBACK ERROR =====");
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Lỗi xử lý thanh toán: " + e.getMessage());
            return "redirect:/";
        }
    }
    
    /**
     * Trang thanh toán thành công
     * GET /payment/success
     */
    @GetMapping("/success")
    public String paymentSuccess(@RequestParam Long orderId, Model model, HttpSession session) {
        
        Long userId = (Long) session.getAttribute("USER_ID");
        if (userId == null) {
            return "redirect:/auth/login";
        }
        
        try {
            Order order = orderRepository.findById(orderId)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng"));
            
            Payment payment = paymentRepository.findByOrderId(orderId)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy thông tin thanh toán"));
            
            List<OrderItem> orderItems = orderService.getOrderItems(orderId);
            
            model.addAttribute("order", order);
            model.addAttribute("payment", payment);
            model.addAttribute("orderItems", orderItems);
            
            return "payment-success";
            
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "error";
        }
    }
    
    /**
     * Trang thanh toán thất bại
     * GET /payment/failed
     */
    @GetMapping("/failed")
    public String paymentFailed(
            @RequestParam Long orderId,
            @RequestParam(required = false) String message,
            Model model,
            HttpSession session) {
        
        Long userId = (Long) session.getAttribute("USER_ID");
        if (userId == null) {
            return "redirect:/auth/login";
        }
        
        try {
            Order order = orderRepository.findById(orderId)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng"));
            
            Payment payment = paymentRepository.findByOrderId(orderId).orElse(null);
            
            model.addAttribute("order", order);
            model.addAttribute("payment", payment);
            model.addAttribute("errorMessage", message != null ? message : "Giao dịch không thành công");
            
            return "payment-failed";
            
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "error";
        }
    }
    
    /**
     * API kiểm tra trạng thái thanh toán
     * GET /payment/status/{orderId}
     */
    @GetMapping("/status/{orderId}")
    @ResponseBody
    public Map<String, Object> getPaymentStatus(@PathVariable Long orderId, HttpSession session) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            Long userId = (Long) session.getAttribute("USER_ID");
            if (userId == null) {
                response.put("success", false);
                response.put("message", "Chưa đăng nhập");
                return response;
            }
            
            Payment payment = paymentRepository.findByOrderId(orderId).orElse(null);
            
            if (payment != null) {
                response.put("success", true);
                response.put("status", payment.getStatus());
                response.put("provider", payment.getProvider());
                response.put("amount", payment.getAmount());
            } else {
                response.put("success", false);
                response.put("message", "Không tìm thấy thông tin thanh toán");
            }
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
        }
        
        return response;
    }
}
