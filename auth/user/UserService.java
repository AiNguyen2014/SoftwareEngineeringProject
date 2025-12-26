package ecommerce.shoestore.auth.user;

import ecommerce.shoestore.auth.address.Address;
import ecommerce.shoestore.auth.dto.UserProfileDto;
import ecommerce.shoestore.auth.user.User;
import ecommerce.shoestore.auth.user.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    // 1. Lấy thông tin hiển thị lên Form
    public UserProfileDto getUserProfile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy User"));

        UserProfileDto dto = new UserProfileDto();
        dto.setFullname(user.getFullname());
        dto.setEmail(user.getEmail());
        dto.setPhone(user.getPhone());
        dto.setDateOfBirth(user.getDateOfBirth());
        dto.setGender(user.getGender());

        // Map địa chỉ (nếu có)
        if (user.getAddress() != null) {
            dto.setProvince(user.getAddress().getProvince());
            dto.setDistrict(user.getAddress().getDistrict());
            dto.setCommune(user.getAddress().getCommune());
            dto.setStreetDetail(user.getAddress().getStreetDetail());
        }
        return dto;
    }

    // 2. Cập nhật thông tin
    @Transactional
    public void updateUserProfile(Long userId, UserProfileDto req) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User không tồn tại"));

        // Cập nhật thông tin cơ bản (KHÔNG cập nhật Email)
        user.setFullname(req.getFullname());
        user.setPhone(req.getPhone());
        user.setDateOfBirth(req.getDateOfBirth());
        user.setGender(req.getGender());

        // Cập nhật địa chỉ
        Address address = user.getAddress();
        if (address == null) {
            address = new Address(); // Nếu chưa có thì tạo mới
            user.setAddress(address);
        }
        address.setProvince(req.getProvince());
        address.setDistrict(req.getDistrict());
        address.setCommune(req.getCommune());
        address.setStreetDetail(req.getStreetDetail());

        userRepository.save(user);
    }
}