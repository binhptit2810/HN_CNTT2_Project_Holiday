package com.attraction.quanlinhahang.controller;

import com.attraction.quanlinhahang.config.SecurityUtils;
import com.attraction.quanlinhahang.model.User;
import com.attraction.quanlinhahang.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AuthController {

    @Autowired
    private UserService userService;

    // ── ROOT REDIRECT ──────────────────────────────────────────────

    @GetMapping("/")
    public String index() {
        User user = SecurityUtils.getCurrentUser();
        if (user == null) {
            return "redirect:/login";
        }
        return switch (user.getRole()) {
            case ADMIN -> "redirect:/admin/dashboard";
            case KITCHEN -> "redirect:/kitchen";
            case CASHIER -> "redirect:/admin/pos";
            case USER -> "redirect:/customer/select-table";
            default -> "redirect:/tables";
        };
    }

    // ── AUTH (LOGIN/REGISTER) ──────────────────────────────────────

    @GetMapping("/login")
    public String loginPage(@RequestParam(required = false) String error,
                            @RequestParam(required = false) String logout,
                            Model model) {
        if (error != null) {
            model.addAttribute("error", "Sai tài khoản hoặc mật khẩu!");
        }
        if (logout != null) {
            model.addAttribute("success", "Đã đăng xuất thành công!");
        }
        return "login";
    }

    @GetMapping("/register")
    public String registerPage() {
        return "register";
    }

    @PostMapping("/register")
    public String doRegister(
            @RequestParam String username,
            @RequestParam String password,
            @RequestParam String fullName,
            @RequestParam String phone,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String address,
            RedirectAttributes attrs) {

        // Lưu lại dữ liệu form để điền lại khi có lỗi (không lưu password vì lý do bảo mật)
        attrs.addFlashAttribute("prevUsername", username  != null ? username  : "");
        attrs.addFlashAttribute("prevFullName", fullName  != null ? fullName  : "");
        attrs.addFlashAttribute("prevPhone",    phone     != null ? phone     : "");
        attrs.addFlashAttribute("prevEmail",    email     != null ? email     : "");
        attrs.addFlashAttribute("prevAddress",  address   != null ? address   : "");

        try {
            userService.createUser(username, password, User.Role.USER, fullName, phone, email, address);
            // Thành công → xóa flash dữ liệu form, chuyển về login
            attrs.addFlashAttribute("prevUsername", "");
            attrs.addFlashAttribute("prevFullName",  "");
            attrs.addFlashAttribute("prevPhone",     "");
            attrs.addFlashAttribute("prevEmail",     "");
            attrs.addFlashAttribute("prevAddress",   "");
            attrs.addFlashAttribute("success", "Đăng ký tài khoản thành công! Hãy đăng nhập.");
            return "redirect:/login";
        } catch (IllegalArgumentException e) {
            attrs.addFlashAttribute("error", e.getMessage());
            return "redirect:/register";
        } catch (Exception e) {
            String rootMsg = e.getMessage();
            Throwable cause = e.getCause();
            while (cause != null) {
                rootMsg = cause.getMessage();
                cause = cause.getCause();
            }
            attrs.addFlashAttribute("error", "Lỗi hệ thống: " + rootMsg);
            return "redirect:/register";
        }
    }
}
