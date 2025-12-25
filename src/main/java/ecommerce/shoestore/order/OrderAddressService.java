package ecommerce.shoestore.order;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderAddressService {
    
    private final OrderAddressRepository orderAddressRepository;
    
    public List<OrderAddress> getUserAddresses(Long userId) {
        return orderAddressRepository.findByUserIdOrderByIsDefaultDescCreatedAtDesc(userId);
    }
    
    public OrderAddress getDefaultAddress(Long userId) {
        return orderAddressRepository.findByUserIdAndIsDefaultTrue(userId).orElse(null);
    }
    
    @Transactional
    public OrderAddress saveAddress(OrderAddress address) {
        // If this is set as default, remove default from other addresses
        if (address.getIsDefault()) {
            orderAddressRepository.findByUserIdAndIsDefaultTrue(address.getUserId())
                .ifPresent(existingDefault -> {
                    if (!existingDefault.getAddressId().equals(address.getAddressId())) {
                        existingDefault.setIsDefault(false);
                        orderAddressRepository.save(existingDefault);
                    }
                });
        }
        
        // If this is the first address, make it default
        if (orderAddressRepository.countByUserId(address.getUserId()) == 0) {
            address.setIsDefault(true);
        }
        
        return orderAddressRepository.save(address);
    }
    
    @Transactional
    public void setDefaultAddress(Long addressId, Long userId) {
        // Remove default from all addresses of this user
        orderAddressRepository.findByUserIdAndIsDefaultTrue(userId)
            .ifPresent(existingDefault -> {
                existingDefault.setIsDefault(false);
                orderAddressRepository.save(existingDefault);
            });
        
        // Set new default
        orderAddressRepository.findById(addressId).ifPresent(address -> {
            if (address.getUserId().equals(userId)) {
                address.setIsDefault(true);
                orderAddressRepository.save(address);
            }
        });
    }
    
    @Transactional
    public void deleteAddress(Long addressId, Long userId) {
        orderAddressRepository.findById(addressId).ifPresent(address -> {
            if (address.getUserId().equals(userId)) {
                boolean wasDefault = address.getIsDefault();
                orderAddressRepository.delete(address);
                
                // If deleted address was default, set another address as default
                if (wasDefault) {
                    List<OrderAddress> remaining = getUserAddresses(userId);
                    if (!remaining.isEmpty()) {
                        remaining.get(0).setIsDefault(true);
                        orderAddressRepository.save(remaining.get(0));
                    }
                }
            }
        });
    }
    
    public OrderAddress getAddressById(Long addressId) {
        return orderAddressRepository.findById(addressId).orElse(null);
    }
}
