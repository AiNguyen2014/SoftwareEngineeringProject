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
            // Mặc định hoặc xử lý lỗi nếu cần
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
        Account account = accountRepository.findByUsername(req.getUsername())
                .orElseThrow(() -> new RuntimeException("Tài khoản hoặc mật khẩu không đúng.")); 
        
        if (!passwordEncoder.matches(req.getPassword(), account.getPassword())) {
            throw new RuntimeException("Tài khoản hoặc mật khẩu không đúng.");
        }

        if (!account.isEnabled()) {
            throw new RuntimeException("Tài khoản chưa được kích hoạt. Vui lòng kiểm tra email.");
        }

        return userRepository.findByAccount_Username(req.getUsername())
                .orElseThrow(() -> new RuntimeException("Lỗi dữ liệu: Không tìm thấy thông tin người dùng."));
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