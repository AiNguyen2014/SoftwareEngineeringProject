package ecommerce.shoestore.auth.user;

import ecommerce.shoestore.auth.account.Account;
import ecommerce.shoestore.auth.account.AccountRepository;
import ecommerce.shoestore.auth.address.Address;
import ecommerce.shoestore.auth.user.dto.UpdateProfileRequest;
import ecommerce.shoestore.auth.user.dto.ChangePasswordRequest;
import ecommerce.shoestore.auth.image.FileUploadService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;


import java.io.IOException;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;
    private final FileUploadService fileUploadService;

    public User getCurrentUser(String Email) {
        return userRepository.findByEmail(Email).orElseThrow(() -> new RuntimeException("User không tồn tại: " + Email));
    }

    public Account getCurrentAccount(String Username) {
        return accountRepository.findByUsername(Username).orElseThrow(() -> new RuntimeException("Tài khoản không tồn tại: " + Username));
    }

    public UpdateProfileRequest getProfileForDisplay(String email) {
        User user = getCurrentUser(email);

        UpdateProfileRequest dto = new UpdateProfileRequest();

        // Map thông tin cơ bản
        dto.setEmail(user.getEmail());
        dto.setFullname(user.getFullname());
        dto.setPhone(user.getPhone());
        dto.setDateOfBirth(user.getDateOfBirth());
        dto.setGender(user.getGender());
        dto.setAvatar(user.getAvatar());

        // Map địa chỉ
        if (user.getAddress() != null) {
            dto.setProvince(user.getAddress().getProvince());
            dto.setDistrict(user.getAddress().getDistrict());
            dto.setCommune(user.getAddress().getCommune());
            dto.setStreetDetail(user.getAddress().getStreetDetail());
        } else {
            dto.setProvince("");
            dto.setDistrict("");
            dto.setCommune("");
            dto.setStreetDetail("");
        }
        return dto;
    }

    @Transactional
    public void updateProfile(String email, UpdateProfileRequest request) throws IOException {
        User user = getCurrentUser(email);

        user.setFullname(request.getFullname());
        user.setPhone(request.getPhone());
        user.setGender(request.getGender());
        user.setDateOfBirth(request.getDateOfBirth());

        if (user.getAddress() == null) {
            user.setAddress(new Address());
        }
        Address address = user.getAddress();
        address.setProvince(request.getProvince());
        address.setDistrict(request.getDistrict());
        address.setCommune(request.getCommune());
        address.setStreetDetail(request.getStreetDetail());

        if (request.getAvatarFile() != null && !request.getAvatarFile().isEmpty()) {
            String avatarUrl = fileUploadService.uploadFile(request.getAvatarFile());

            user.setAvatar(avatarUrl);
        }

        userRepository.save(user);
    }

    public void changePassword(String email, ChangePasswordRequest request) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy user"));

        Account account = user.getAccount();
        if (account == null) throw new RuntimeException("Tài khoản không hợp lệ!");

        if (!passwordEncoder.matches(request.getOldPassword(), account.getPassword())) {
             throw new RuntimeException("Mật khẩu cũ không đúng!");
        }
        account.setPassword(passwordEncoder.encode(request.getNewPassword()));

        userRepository.save(user);
    }
}

