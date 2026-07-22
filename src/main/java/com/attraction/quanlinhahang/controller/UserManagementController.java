package com.attraction.quanlinhahang.controller;

import com.attraction.quanlinhahang.config.SecurityUtils;
import com.attraction.quanlinhahang.model.User;
import com.attraction.quanlinhahang.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/users")
public class UserManagementController {

    @Autowired
    private UserService userService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public String listUsers(Model model) {
        model.addAttribute("users", userService.getAllUsers());
        model.addAttribute("user", SecurityUtils.requireCurrentUser());
        return "admin/users";
    }

    @PostMapping("/{userId}/toggle-active")
    @PreAuthorize("hasRole('ADMIN')")
    public String toggleUserActive(@PathVariable Long userId, 
                                   @RequestParam(required = false) String reason, 
                                   RedirectAttributes attrs) {
        User currentUser = SecurityUtils.requireCurrentUser();
        try {
            userService.toggleUserActiveStatus(userId, reason, currentUser.getId());
            attrs.addFlashAttribute("success", "Cập nhật trạng thái tài khoản thành công!");
        } catch (Exception e) {
            attrs.addFlashAttribute("error", com.attraction.quanlinhahang.util.ExceptionUtils.getFriendlyMessage(e));
        }
        return "redirect:/admin/users";
    }

    @PostMapping("/{userId}/delete")
    @PreAuthorize("hasRole('ADMIN')")
    public String deleteUserAccount(@PathVariable Long userId, RedirectAttributes attrs) {
        User currentUser = SecurityUtils.requireCurrentUser();
        try {
            userService.deleteUser(userId, currentUser.getId());
            attrs.addFlashAttribute("success", "Xóa tài khoản thành công!");
        } catch (org.springframework.dao.DataIntegrityViolationException e) {
            attrs.addFlashAttribute("error", "Không thể xóa nhân viên này vì họ đã có dữ liệu lịch sử (Đơn hàng, Hóa đơn). Vui lòng sử dụng tính năng Khóa tài khoản thay thế!");
        } catch (Exception e) {
            attrs.addFlashAttribute("error", com.attraction.quanlinhahang.util.ExceptionUtils.getFriendlyMessage(e));
        }
        return "redirect:/admin/users";
    }
}
