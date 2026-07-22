package com.attraction.quanlinhahang.config;

import com.attraction.quanlinhahang.model.User;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Tiện ích để lấy User entity hiện tại từ Spring Security context.
 * Thay thế cho việc đọc từ HttpSession.getAttribute("currentUser").
 */
public class SecurityUtils {

    /**
     * Lấy User entity của người dùng đang đăng nhập.
     * Trả về null nếu chưa xác thực.
     */
    public static User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth.getPrincipal().equals("anonymousUser")) {
            return null;
        }
        if (auth.getPrincipal() instanceof CustomUserDetailsService.CustomUserDetails details) {
            return details.getUser();
        }
        return null;
    }

    /**
     * Lấy User entity, throw exception nếu chưa đăng nhập.
     */
    public static User requireCurrentUser() {
        User user = getCurrentUser();
        if (user == null) {
            throw new IllegalStateException("Chưa đăng nhập!");
        }
        return user;
    }
}
