package ecommerce.shoestore.common;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.resource.NoResourceFoundException;

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
     * Xử lý resource tĩnh không tồn tại (favicon, .well-known, ...)
     * Trả về 404 thay vì redirect về trang chủ để tránh vòng lặp 302.
     */
    @ExceptionHandler(NoResourceFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleNoResourceFound(NoResourceFoundException ex, Model model) {
        log.warn("Static resource not found: {}", ex.getResourcePath());
        model.addAttribute("errorMessage", ex.getMessage());
        model.addAttribute("errorCode", "404");
        return "error";
    }

    /**
     * Xử lý tất cả các Exception khác
     * Redirect về trang chủ

    @ExceptionHandler(Exception.class)
    public String handleGeneralException(Exception ex, Model model) {
        log.error("Unexpected error occurred", ex);
        model.addAttribute("errorMessage", ex.getMessage());
        model.addAttribute("errorCode", "500");
        return "error";
    }
    */

    @ExceptionHandler(Exception.class)
    public String handleGeneralException(Exception ex, Model model) {
        log.error("Unexpected error occurred", ex);
        model.addAttribute("errorMessage", ex.getMessage());
        return "error";
    }

}