package com.attraction.quanlinhahang.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import com.attraction.quanlinhahang.util.ExceptionUtils;
/**
 * Xử lý exception toàn cục cho ứng dụng.
 * Tránh lộ stack trace ra UI và trả về thông báo lỗi thân thiện.
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * Xử lý IllegalArgumentException — lỗi nghiệp vụ (validation, not found...).
     */
    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseBody
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, Object> handleIllegalArgument(IllegalArgumentException e) {
        log.warn("Business error: {}", e.getMessage());
        Map<String, Object> res = new HashMap<>();
        res.put("success", false);
        res.put("message", e.getMessage());
        return res;
    }

    /**
     * Xử lý IllegalStateException — trạng thái không hợp lệ.
     */
    @ExceptionHandler(IllegalStateException.class)
    @ResponseBody
    @ResponseStatus(HttpStatus.CONFLICT)
    public Map<String, Object> handleIllegalState(IllegalStateException e) {
        log.warn("State error: {}", e.getMessage());
        Map<String, Object> res = new HashMap<>();
        res.put("success", false);
        res.put("message", e.getMessage());
        return res;
    }

    /**
     * Xử lý NoSuchElementException — entity không tồn tại.
     */
    @ExceptionHandler(NoSuchElementException.class)
    @ResponseBody
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Map<String, Object> handleNotFound(NoSuchElementException e) {
        log.warn("Not found: {}", e.getMessage());
        Map<String, Object> res = new HashMap<>();
        res.put("success", false);
        res.put("message", "Không tìm thấy dữ liệu yêu cầu!");
        return res;
    }

    /**
     * Xử lý AccessDeniedException — bị từ chối quyền truy cập.
     */
    @ExceptionHandler(AccessDeniedException.class)
    public String handleAccessDenied(AccessDeniedException e) {
        log.warn("Access denied: {}", e.getMessage());
        return "redirect:/login?error=access_denied";
    }

    /**
     * Bỏ qua lỗi thiếu file tĩnh (như favicon.ico) để tránh rác log.
     */
    @ExceptionHandler(org.springframework.web.servlet.resource.NoResourceFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public void handleNoResourceFound(org.springframework.web.servlet.resource.NoResourceFoundException e) {
        // Không làm gì cả, không in log để console sạch sẽ.
    }

    /**
     * Fallback — xử lý tất cả exception không được bắt cụ thể.
     */
    @ExceptionHandler(Exception.class)
    @ResponseBody
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Map<String, Object> handleGeneral(Exception e) {
        log.error("Unexpected error: ", e);
        Map<String, Object> res = new HashMap<>();
        res.put("success", false);
        res.put("message", ExceptionUtils.getFriendlyMessage(e));
        return res;
    }
}
