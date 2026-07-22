package com.attraction.quanlinhahang.service;

import com.attraction.quanlinhahang.model.User;
import com.attraction.quanlinhahang.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * Tạo tài khoản mới với đầy đủ thông tin cá nhân.
     * fullName và phone là bắt buộc; email và address là tùy chọn.
     * Mật khẩu được hash bằng BCrypt trước khi lưu.
     */
    @Transactional
    public User createUser(String username, String password, User.Role role,
                           String fullName, String phone, String email, String address) {

        // Kiểm tra username trùng
        if (userRepository.findByUsername(username).isPresent()) {
            throw new IllegalArgumentException("Tên tài khoản đã tồn tại, vui lòng chọn tên khác!");
        }

        // Kiểm tra fullName không được rỗng (phòng trường hợp bypass JS)
        if (fullName == null || fullName.isBlank()) {
            throw new IllegalArgumentException("Họ và tên không được để trống!");
        }

        // Kiểm tra phone hợp lệ và không trùng
        if (phone == null || phone.isBlank()) {
            throw new IllegalArgumentException("Số điện thoại không được để trống!");
        }
        if (!phone.matches("\\d{10,11}")) {
            throw new IllegalArgumentException("Số điện thoại phải có 10–11 chữ số!");
        }
        if (userRepository.findByPhone(phone).isPresent()) {
            throw new IllegalArgumentException("Số điện thoại đã được sử dụng.");
        }

        // Kiểm tra email trùng (chỉ khi người dùng có nhập)
        String trimmedEmail = (email != null && !email.isBlank()) ? email.trim() : null;
        if (trimmedEmail != null && userRepository.findByEmail(trimmedEmail).isPresent()) {
            throw new IllegalArgumentException("Email đã được sử dụng.");
        }

        User user = User.builder()
                .username(username.trim())
                .password(passwordEncoder.encode(password))
                .role(role)
                .fullName(fullName.trim())
                .phone(phone.trim())
                .email(trimmedEmail)
                .address((address != null && !address.isBlank()) ? address.trim() : null)
                .build();

        return userRepository.save(user);
    }

    /**
     * Overload giữ nguyên tương thích ngược cho DataInitializer (tạo staff).
     * Mật khẩu cũng được hash bằng BCrypt.
     */
    public User createUser(String username, String password, User.Role role) {
        if (userRepository.findByUsername(username).isPresent()) {
            throw new IllegalArgumentException("Username already exists");
        }
        User user = User.builder()
                .username(username)
                .password(passwordEncoder.encode(password))
                .role(role)
                .build();
        return userRepository.save(user);
    }

    /**
     * Phương thức login giữ lại để tương thích.
     * Xác thực chính thức được xử lý bởi Spring Security (CustomUserDetailsService).
     */
    public Optional<User> login(String username, String password) {
        Optional<User> optUser = userRepository.findByUsername(username)
                .filter(user -> passwordEncoder.matches(password, user.getPassword()));
        if (optUser.isPresent()) {
            User user = optUser.get();
            if (user.getActive() != null && !user.getActive()) {
                throw new IllegalStateException("Tài khoản của bạn đã bị khóa! Lý do: " + (user.getLockReason() != null ? user.getLockReason() : "Không có lý do cụ thể."));
            }
        }
        return optUser;
    }

    @Transactional(readOnly = true)
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Transactional
    public void toggleUserActiveStatus(Long userId, String reason, Long adminId) {
        if (userId.equals(adminId)) {
            throw new IllegalArgumentException("Không thể tự khoá tài khoản của chính mình!");
        }
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Người dùng không tồn tại!"));
        boolean newActive = user.getActive() == null ? false : !user.getActive();
        user.setActive(newActive);
        if (!newActive) {
            user.setLockReason(reason);
        } else {
            user.setLockReason(null);
        }
        userRepository.save(user);
    }

    @Transactional
    public void deleteUser(Long userId, Long adminId) {
        if (userId.equals(adminId)) {
            throw new IllegalArgumentException("Không thể tự xoá tài khoản của chính mình!");
        }
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Người dùng không tồn tại!"));
        userRepository.delete(user);
    }

    public User updateProfile(Long userId, String fullName, String phone, String email, String address) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Người dùng không tồn tại!"));

        if (fullName == null || fullName.isBlank()) {
            throw new IllegalArgumentException("Họ và tên không được để trống!");
        }

        if (phone == null || phone.isBlank()) {
            throw new IllegalArgumentException("Số điện thoại không được để trống!");
        }
        if (!phone.matches("\\d{10,11}")) {
            throw new IllegalArgumentException("Số điện thoại phải có 10–11 chữ số!");
        }

        // Kiểm tra phone trùng nếu thay đổi
        if (!phone.equals(user.getPhone())) {
            if (userRepository.findByPhone(phone).isPresent()) {
                throw new IllegalArgumentException("Số điện thoại đã được sử dụng.");
            }
        }

        // Kiểm tra email trùng nếu thay đổi
        String trimmedEmail = (email != null && !email.isBlank()) ? email.trim() : null;
        if (trimmedEmail != null && !trimmedEmail.equals(user.getEmail())) {
            if (userRepository.findByEmail(trimmedEmail).isPresent()) {
                throw new IllegalArgumentException("Email đã được sử dụng.");
            }
        }

        user.setFullName(fullName.trim());
        user.setPhone(phone.trim());
        user.setEmail(trimmedEmail);
        user.setAddress((address != null && !address.isBlank()) ? address.trim() : null);

        return userRepository.save(user);
    }

    @Transactional
    public void changePassword(Long userId, String oldPassword, String newPassword) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Người dùng không tồn tại!"));

        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new IllegalArgumentException("Mật khẩu cũ không chính xác!");
        }

        if (newPassword == null || newPassword.length() < 4) {
            throw new IllegalArgumentException("Mật khẩu mới phải có ít nhất 4 ký tự!");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }
}

