package com.attraction.quanlinhahang.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // ── CSRF ──────────────────────────────────────────────────
            // Tắt CSRF cho QR order (public API) – giữ CSRF cho tất cả form khác
            .csrf(csrf -> csrf
                .ignoringRequestMatchers("/qr/order/*/request")
            )

            // ── Authorization Rules ───────────────────────────────────
            .authorizeHttpRequests(auth -> auth
                // Public pages
                .requestMatchers("/login", "/register", "/css/**", "/js/**", "/images/**").permitAll()
                // QR order: public (guest không cần đăng nhập)
                .requestMatchers("/qr/order/**").permitAll()
                // Admin pages
                .requestMatchers("/admin/dashboard", "/admin/users/**", "/admin/menu/**", "/admin/inventory/**").hasRole("ADMIN")
                // POS pages (Cashier only)
                .requestMatchers("/admin/pos/**").hasRole("CASHIER")
                // Kitchen pages
                .requestMatchers("/kitchen/**").hasAnyRole("ADMIN", "KITCHEN")
                // Waiter pages
                .requestMatchers("/tables/**", "/order/**").hasAnyRole("ADMIN", "WAITER")
                // Customer pages
                .requestMatchers("/customer/**", "/user/**").hasRole("USER")
                // All other requests require authentication
                .anyRequest().authenticated()
            )

            // ── Form Login ────────────────────────────────────────────
            .formLogin(form -> form
                .loginPage("/login")
                .loginProcessingUrl("/login")
                .usernameParameter("username")
                .passwordParameter("password")
                .defaultSuccessUrl("/", true)
                .failureUrl("/login?error=true")
                .permitAll()
            )

            // ── Logout ────────────────────────────────────────────────
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout=true")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
                .permitAll()
            )

            // ── Session Management ────────────────────────────────────
            .sessionManagement(session -> session
                .maximumSessions(1)
                .expiredUrl("/login?expired=true")
            );

        return http.build();
    }
}
