package com.attraction.quanlinhahang.controller;

import com.attraction.quanlinhahang.config.SecurityUtils;
import com.attraction.quanlinhahang.model.User;
import com.attraction.quanlinhahang.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/user/profile")
public class ProfileController {

    @Autowired
    private UserService userService;

    @GetMapping
    @PreAuthorize("hasRole('USER')")
    public String userProfile(Model model) {
        User currentUser = SecurityUtils.requireCurrentUser();
        model.addAttribute("user", currentUser);
        return "user/profile";
    }

    @PostMapping("/update")
    @PreAuthorize("hasRole('USER')")
    public String updateUserProfile(
            @RequestParam String fullName,
            @RequestParam String phone,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String address,
            RedirectAttributes attrs) {
        User currentUser = SecurityUtils.requireCurrentUser();
        try {
            userService.updateProfile(currentUser.getId(), fullName, phone, email, address);
            attrs.addFlashAttribute("success", "Cập nhật thông tin cá nhân thành công!");
        } catch (Exception e) {
            attrs.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/user/profile";
    }

    @PostMapping("/change-password")
    @PreAuthorize("hasRole('USER')")
    public String changeUserPassword(
            @RequestParam String oldPassword,
            @RequestParam String newPassword,
            @RequestParam String confirmPassword,
            RedirectAttributes attrs) {
        User currentUser = SecurityUtils.requireCurrentUser();
        try {
            if (!newPassword.equals(confirmPassword)) {
                throw new IllegalArgumentException("Mật khẩu mới và xác nhận mật khẩu không khớp!");
            }
            userService.changePassword(currentUser.getId(), oldPassword, newPassword);
            attrs.addFlashAttribute("successPass", "Đổi mật khẩu thành công!");
        } catch (Exception e) {
            attrs.addFlashAttribute("errorPass", e.getMessage());
        }
        return "redirect:/user/profile";
    }
}
