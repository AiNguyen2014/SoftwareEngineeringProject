package ecommerce.shoestore.common;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

/**
 * Global exception handler cho toàn bộ application
 * Xử lý các exception throw từ Controller
 */
@ControllerAdvice
@Slf4j

public class GlobalExceptionHandler {

    /**
     * Xử lý NotFoundException
     * Hiển thị trang lỗi 404 thay vì redirect về trang chủ
     */
    @ExceptionHandler(NotFoundException.class)
    public String handleNotFoundException(NotFoundException ex, Model model) {
        log.error("NotFoundException: {}", ex.getMessage());
        model.addAttribute("errorMessage", ex.getMessage());
        model.addAttribute("errorCode", "404");
        return "error";  // Trả về trang lỗi để debug, sau này có thể thay đổi
    }

    /**
     * Xử lý tất cả các Exception khác
     * Redirect về trang chủ
     */
    @ExceptionHandler(Exception.class)
    public String handleGeneralException(Exception ex, Model model) {
        log.error("Unexpected error occurred", ex);
        return "redirect:/";
    }
}