package com.attraction.quanlinhahang.util;

import org.springframework.dao.DataIntegrityViolationException;

public class ExceptionUtils {

    public static String getFriendlyMessage(Exception e) {
        if (e instanceof DataIntegrityViolationException) {
            String msg = e.getMessage() != null ? e.getMessage().toLowerCase() : "";
            if (msg.contains("data truncated")) {
                return "Dữ liệu không hợp lệ hoặc quá dài so với quy định.";
            }
            if (msg.contains("foreign key constraint") || msg.contains("constraint fails")) {
                return "Không thể xóa hoặc thay đổi dữ liệu này vì đang có liên kết ràng buộc (ví dụ: tài khoản hoặc món ăn đã từng tạo đơn hàng, thanh toán...).";
            }
            if (msg.contains("duplicate entry")) {
                return "Dữ liệu đã tồn tại trong hệ thống. Vui lòng kiểm tra lại sự trùng lặp (ví dụ: email, tên đăng nhập, số bàn).";
            }
            return "Không thể thực hiện thao tác do có ràng buộc dữ liệu. Vui lòng kiểm tra lại!";
        }
        return e.getMessage() != null ? e.getMessage() : "Lỗi hệ thống";
    }
}
