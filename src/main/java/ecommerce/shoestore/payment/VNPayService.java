package ecommerce.shoestore.payment;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
@RequiredArgsConstructor
public class VNPayService {
    
    private final VNPayConfig vnPayConfig;
    
    /**
     * Tạo URL thanh toán VNPay
     */
    public String createPaymentUrl(Long orderId, Long userId, long amount, String orderInfo, String ipAddress) {
        try {
            Map<String, String> vnpParams = new HashMap<>();
            
            vnpParams.put("vnp_Version", "2.1.0");
            vnpParams.put("vnp_Command", "pay");
            vnpParams.put("vnp_TmnCode", vnPayConfig.getVnpTmnCode());
            vnpParams.put("vnp_Amount", String.valueOf(amount * 100)); // VNPay yêu cầu số tiền * 100
            vnpParams.put("vnp_CurrCode", "VND");
            vnpParams.put("vnp_TxnRef", String.valueOf(orderId)); // Mã đơn hàng
            vnpParams.put("vnp_OrderInfo", orderInfo);
            vnpParams.put("vnp_OrderType", "other");
            vnpParams.put("vnp_Locale", "vn");
            vnpParams.put("vnp_ReturnUrl", vnPayConfig.getVnpReturnUrl());
            vnpParams.put("vnp_IpAddr", ipAddress);
            
            Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("Etc/GMT+7"));
            SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
            String vnpCreateDate = formatter.format(calendar.getTime());
            vnpParams.put("vnp_CreateDate", vnpCreateDate);
            
            calendar.add(Calendar.MINUTE, 15); // Hết hạn sau 15 phút
            String vnpExpireDate = formatter.format(calendar.getTime());
            vnpParams.put("vnp_ExpireDate", vnpExpireDate);
            
            // Sắp xếp params theo thứ tự alphabet
            List<String> fieldNames = new ArrayList<>(vnpParams.keySet());
            Collections.sort(fieldNames);
            
            StringBuilder hashData = new StringBuilder();
            StringBuilder query = new StringBuilder();
            
            Iterator<String> itr = fieldNames.iterator();
            while (itr.hasNext()) {
                String fieldName = itr.next();
                String fieldValue = vnpParams.get(fieldName);
                if (fieldValue != null && fieldValue.length() > 0) {
                    // Build hash data
                    hashData.append(fieldName);
                    hashData.append('=');
                    hashData.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString()));
                    
                    // Build query
                    query.append(URLEncoder.encode(fieldName, StandardCharsets.US_ASCII.toString()));
                    query.append('=');
                    query.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString()));
                    
                    if (itr.hasNext()) {
                        query.append('&');
                        hashData.append('&');
                    }
                }
            }
            
            String queryUrl = query.toString();
            String vnpSecureHash = hmacSHA512(vnPayConfig.getVnpHashSecret(), hashData.toString());
            queryUrl += "&vnp_SecureHash=" + vnpSecureHash;
            
            return vnPayConfig.getVnpPayUrl() + "?" + queryUrl;
            
        } catch (Exception e) {
            throw new RuntimeException("Error creating VNPay payment URL", e);
        }
    }
    
    /**
     * Verify callback từ VNPay
     */
    public boolean verifyPaymentCallback(Map<String, String> params) {
        try {
            String vnpSecureHash = params.get("vnp_SecureHash");
            params.remove("vnp_SecureHash");
            params.remove("vnp_SecureHashType");
            
            // Sắp xếp params theo alphabet
            List<String> fieldNames = new ArrayList<>(params.keySet());
            Collections.sort(fieldNames);
            
            StringBuilder hashData = new StringBuilder();
            Iterator<String> itr = fieldNames.iterator();
            
            while (itr.hasNext()) {
                String fieldName = itr.next();
                String fieldValue = params.get(fieldName);
                if (fieldValue != null && fieldValue.length() > 0) {
                    hashData.append(fieldName);
                    hashData.append('=');
                    hashData.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString()));
                    if (itr.hasNext()) {
                        hashData.append('&');
                    }
                }
            }
            
            String calculatedHash = hmacSHA512(vnPayConfig.getVnpHashSecret(), hashData.toString());
            return calculatedHash.equals(vnpSecureHash);
            
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Hash HMAC SHA512
     */
    private String hmacSHA512(String key, String data) {
        try {
            Mac hmac512 = Mac.getInstance("HmacSHA512");
            SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA512");
            hmac512.init(secretKey);
            byte[] result = hmac512.doFinal(data.getBytes(StandardCharsets.UTF_8));
            
            StringBuilder sb = new StringBuilder();
            for (byte b : result) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
            
        } catch (Exception e) {
            throw new RuntimeException("Error calculating HMAC SHA512", e);
        }
    }
    
    /**
     * Lấy IP address từ request
     */
    public String getIpAddress(jakarta.servlet.http.HttpServletRequest request) {
        String ipAddress = request.getHeader("X-FORWARDED-FOR");
        if (ipAddress == null || ipAddress.isEmpty()) {
            ipAddress = request.getRemoteAddr();
        }
        return ipAddress;
    }
    
    /**
     * Parse response code từ VNPay
     */
    public String getResponseMessage(String responseCode) {
        return switch (responseCode) {
            case "00" -> "Giao dịch thành công";
            case "07" -> "Trừ tiền thành công. Giao dịch bị nghi ngờ (liên quan tới lừa đảo, giao dịch bất thường).";
            case "09" -> "Giao dịch không thành công do: Thẻ/Tài khoản của khách hàng chưa đăng ký dịch vụ InternetBanking tại ngân hàng.";
            case "10" -> "Giao dịch không thành công do: Khách hàng xác thực thông tin thẻ/tài khoản không đúng quá 3 lần";
            case "11" -> "Giao dịch không thành công do: Đã hết hạn chờ thanh toán. Xin quý khách vui lòng thực hiện lại giao dịch.";
            case "12" -> "Giao dịch không thành công do: Thẻ/Tài khoản của khách hàng bị khóa.";
            case "13" -> "Giao dịch không thành công do Quý khách nhập sai mật khẩu xác thực giao dịch (OTP). Xin quý khách vui lòng thực hiện lại giao dịch.";
            case "24" -> "Giao dịch không thành công do: Khách hàng hủy giao dịch";
            case "51" -> "Giao dịch không thành công do: Tài khoản của quý khách không đủ số dư để thực hiện giao dịch.";
            case "65" -> "Giao dịch không thành công do: Tài khoản của Quý khách đã vượt quá hạn mức giao dịch trong ngày.";
            case "75" -> "Ngân hàng thanh toán đang bảo trì.";
            case "79" -> "Giao dịch không thành công do: KH nhập sai mật khẩu thanh toán quá số lần quy định. Xin quý khách vui lòng thực hiện lại giao dịch";
            default -> "Giao dịch không thành công";
        };
    }
}
