package ecommerce.shoestore.common;

/**
 * Custom exception khi không tìm thấy resource
 */
public class NotFoundException extends RuntimeException {

    public NotFoundException(String message) {
        super(message);
    }

    public NotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}