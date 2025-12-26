package ecommerce.shoestore.auth;

import ecommerce.shoestore.auth.account.Account;
import ecommerce.shoestore.auth.account.AccountRepository;
import ecommerce.shoestore.auth.account.UserRole;
import ecommerce.shoestore.auth.address.Address;
import ecommerce.shoestore.auth.dto.*;
import ecommerce.shoestore.auth.email.EmailService;
import ecommerce.shoestore.auth.user.User;
import ecommerce.shoestore.auth.user.UserGender;
import ecommerce.shoestore.auth.user.UserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Random;

@Service
public class AuthService {

    @Autowired private UserRepository userRepository;
    @Autowired private AccountRepository accountRepository;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private EmailService emailService;

    @Transactional
    public void register(RegisterRequest req) {
        if (userRepository.existsByEmail(req.getEmail())) {
            throw new RuntimeException("Email đã tồn tại trong hệ thống.");
        }
        if (accountRepository.existsByUsername(req.getUsername())) {
            throw new RuntimeException("Username đã được sử dụng.");
        }

        Address address = new Address(null, req.getProvince(), req.getDistrict(), req.getCommune(), req.getStreetDetail());

        Account account = new Account();
        account.setUsername(req.getUsername());
        account.setPassword(passwordEncoder.encode(req.getPassword()));
        account.setRole(UserRole.CUSTOMER);
        account.setEnabled(false); 
        
        String code = String.valueOf(new Random().nextInt(900000) + 100000); 
        account.setVerificationCode(code);
        account.setVerificationCodeExpiry(System.currentTimeMillis() + 60 * 1000); 

        User user = new User();
        user.setFullname(req.getFullname());
        user.setEmail(req.getEmail());
        user.setPhone(req.getPhone());
        user.setDateOfBirth(req.getDateOfBirth());
        try {
            user.setGender(UserGender.valueOf(req.getGender()));
        } catch (Exception e) {
        }
        user.setAddress(address);
        user.setAccount(account);

        userRepository.save(user);

        emailService.sendEmail(req.getEmail(), "Xác thực tài khoản", "Mã xác thực của bạn là: " + code);
    }

    public void verifyEmail(VerifyEmailRequest req) {
        User user = userRepository.findByEmail(req.getEmail())
                .orElseThrow(() -> new RuntimeException("Email không tồn tại."));
        Account account = user.getAccount();

        if (account.isEnabled()) {
            throw new RuntimeException("Tài khoản này đã được kích hoạt trước đó.");
        }
        if (System.currentTimeMillis() > account.getVerificationCodeExpiry()) {
            throw new RuntimeException("Mã xác thực đã hết hạn.");
        }
        if (!account.getVerificationCode().equals(req.getCode())) {
            throw new RuntimeException("Mã xác thực không chính xác.");
        }

        account.setEnabled(true);
        account.setVerificationCode(null);
        accountRepository.save(account);
    }

    public User login(LoginRequest req) {
        System.out.println("============== BẮT ĐẦU DEBUG LOGIN ==============");
        System.out.println("1. Username nhập vào: [" + req.getUsername() + "]");
        System.out.println("2. Password nhập vào: [" + req.getPassword() + "]");

        Account account = accountRepository.findByUsername(req.getUsername())
                .orElse(null);
        
        if (account == null) {
            System.out.println(">>> LỖI: Không tìm thấy Username trong bảng Account!");
            throw new RuntimeException("Tài khoản không tồn tại.");
        }
        System.out.println("3. Tìm thấy Account ID: " + account.getAccountId()); // Hoặc getId() tùy bạn đặt tên
        System.out.println("   - Hash trong DB: " + account.getPassword());
        System.out.println("   - Enabled: " + account.isEnabled());

        boolean isMatch = passwordEncoder.matches(req.getPassword(), account.getPassword());
        System.out.println("4. Kết quả so khớp Pass: " + isMatch);
        
        if (!isMatch) {
            System.out.println(">>> LỖI: Mật khẩu không khớp!");
            throw new RuntimeException("Sai mật khẩu.");
        }

        if (!account.isEnabled()) {
            System.out.println(">>> LỖI: Tài khoản chưa kích hoạt (Enabled = false)!");
            throw new RuntimeException("Tài khoản chưa kích hoạt.");
        }

        System.out.println("5. Đang tìm User Info theo Account Username...");
        User user = userRepository.findByAccount_Username(req.getUsername())
                .orElse(null);

        if (user == null) {
            System.out.println(">>> LỖI CHẾT NGƯỜI: Có Account nhưng không tìm thấy User tương ứng!");
            System.out.println("    Nguyên nhân: Cột account_id trong bảng Users bị Null hoặc không khớp ID bên bảng Account.");
            throw new RuntimeException("Lỗi dữ liệu hệ thống (Missing User Profile).");
        }

        System.out.println("6. Thành công! User ID: " + user.getUserId()); 
        System.out.println("============== KẾT THÚC DEBUG LOGIN ==============");
        
        return user;
    }
    
    public void forgotPassword(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Email chưa được đăng ký trong hệ thống."));
        
        Account account = user.getAccount();
        String code = String.valueOf(new Random().nextInt(900000) + 100000);
        account.setVerificationCode(code);
        account.setVerificationCodeExpiry(System.currentTimeMillis() + 120 * 1000); // 120s
        accountRepository.save(account);

        emailService.sendEmail(email, "Reset Mật khẩu", "Mã xác thực để đặt lại mật khẩu: " + code);
    }

    public void resetPassword(ResetPasswordRequest req) {
        User user = userRepository.findByEmail(req.getEmail())
                .orElseThrow(() -> new RuntimeException("Người dùng không tồn tại."));
        Account account = user.getAccount();

        if (System.currentTimeMillis() > account.getVerificationCodeExpiry()) {
            throw new RuntimeException("Mã xác thực đã hết hạn.");
        }
        if (!account.getVerificationCode().equals(req.getCode())) {
            throw new RuntimeException("Mã xác thực không đúng.");
        }

        account.setPassword(passwordEncoder.encode(req.getNewPassword()));
        account.setVerificationCode(null);
        accountRepository.save(account);
    }
}