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
     * Redirect về trang chủ
     */
    @ExceptionHandler(NotFoundException.class)
    public String handleNotFoundException(NotFoundException ex, Model model) {
        log.error("NotFoundException: {}", ex.getMessage());
        return "redirect:/";
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