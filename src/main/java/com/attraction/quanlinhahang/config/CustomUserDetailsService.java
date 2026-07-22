package com.attraction.quanlinhahang.config;

import com.attraction.quanlinhahang.model.User;
import com.attraction.quanlinhahang.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Collections;

/**
 * Kết nối Spring Security với bảng users trong database.
 * Chuyển đổi User entity sang UserDetails để Spring Security xác thực.
 */
@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Tài khoản không tồn tại: " + username));

        return new CustomUserDetails(user);
    }

    /**
     * Wrapper bao quanh User entity, implement UserDetails interface.
     * Giữ reference tới User entity để dùng ở Controller (lấy thông tin cá nhân, role...).
     */
    public static class CustomUserDetails implements UserDetails {
        private final User user;

        public CustomUserDetails(User user) {
            this.user = user;
        }

        public User getUser() {
            return user;
        }

        @Override
        public Collection<? extends GrantedAuthority> getAuthorities() {
            // Spring Security dùng prefix "ROLE_" cho hasRole() check
            return Collections.singletonList(
                new SimpleGrantedAuthority("ROLE_" + user.getRole().name())
            );
        }

        @Override
        public String getPassword() {
            return user.getPassword();
        }

        @Override
        public String getUsername() {
            return user.getUsername();
        }

        @Override
        public boolean isAccountNonExpired() {
            return true;
        }

        @Override
        public boolean isAccountNonLocked() {
            return user.getActive() == null || user.getActive();
        }

        @Override
        public boolean isCredentialsNonExpired() {
            return true;
        }

        @Override
        public boolean isEnabled() {
            return user.getActive() == null || user.getActive();
        }
    }
}
