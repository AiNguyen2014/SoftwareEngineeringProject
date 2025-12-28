package ecommerce.shoestore.payment;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class VNPayConfig {
    
    @Value("${vnpay.tmn-code:YZ312VU8}")
    private String vnpTmnCode;
    
    @Value("${vnpay.hash-secret:X4U66DPG2T18ZYWPBSUNOABBP1JFZBF6}")
    private String vnpHashSecret;
    
    @Value("${vnpay.url:https://sandbox.vnpayment.vn/paymentv2/vpcpay.html}")
    private String vnpPayUrl;
    
    @Value("${vnpay.return-url:http://localhost:8080/payment/vnpay-return}")
    private String vnpReturnUrl;
    
    @Value("${vnpay.api-url:https://sandbox.vnpayment.vn/merchant_webapi/api/transaction}")
    private String vnpApiUrl;
    
    public String getVnpTmnCode() {
        return vnpTmnCode;
    }
    
    public String getVnpHashSecret() {
        return vnpHashSecret;
    }
    
    public String getVnpPayUrl() {
        return vnpPayUrl;
    }
    
    public String getVnpReturnUrl() {
        return vnpReturnUrl;
    }
    
    public String getVnpApiUrl() {
        return vnpApiUrl;
    }
}
