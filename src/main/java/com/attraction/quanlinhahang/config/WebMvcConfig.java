package com.attraction.quanlinhahang.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // Interceptor toàn cục tự động chèn HTTP Headers chống Cache
        // Trình duyệt sẽ buộc phải gửi request thật lên server thay vì dùng cache cũ khi người dùng ấn nút Back.
        registry.addInterceptor(new HandlerInterceptor() {
            @Override
            public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
                response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate"); // HTTP 1.1
                response.setHeader("Pragma", "no-cache"); // HTTP 1.0
                response.setDateHeader("Expires", 0); // Proxies
                return true;
            }
        });
    }
}
