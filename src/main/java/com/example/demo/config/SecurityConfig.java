package com.example.demo.config;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Configuration
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // ✅ Cho phép frontend React truy cập
                .cors(cors -> cors.configurationSource(request -> {
                    CorsConfiguration config = new CorsConfiguration();
                    config.setAllowedOrigins(List.of("http://localhost:5173")); // Đúng domain frontend
                    config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
                    config.setAllowedHeaders(List.of("*")); // ✅ Cho phép mọi header, kể cả Authorization
                    config.setExposedHeaders(List.of("*")); // ✅ Cho phép đọc mọi header trả về
                    config.setAllowCredentials(true);
                    return config;
                }))
                .csrf(csrf -> csrf.disable()) // ✅ Tắt CSRF cho REST API
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // ✅ Cho phép các endpoint public không cần token
                        .requestMatchers(
                                "/api/auth/**",
                                "/api/register",
                                "/api/banners/**",
                                "/api/categories/**",
                                "/api/products/**",
                                "/api/flash-sales/**",
                                "/uploads/**",
                                "/api/uploads/**"
                        ).permitAll()

                        // ✅ Chỉ ADMIN mới được truy cập /api/users/**
                        .requestMatchers("/api/users/**").hasRole("ADMIN")

                        // ✅ Các request khác yêu cầu có token JWT hợp lệ
                        .anyRequest().authenticated()
                )
                // ✅ Khi token sai hoặc chưa có
                .exceptionHandling(eh -> eh.authenticationEntryPoint(unauthorizedHandler()))
                // ✅ Thêm JWT filter vào trước UsernamePasswordAuthenticationFilter
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    // ✅ Khi token không hợp lệ hoặc chưa đăng nhập
    @Bean
    public AuthenticationEntryPoint unauthorizedHandler() {
        return (request, response, authException) -> {
            response.setContentType("application/json;charset=UTF-8");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("{\"error\":\"Unauthorized or invalid token\"}");
        };
    }

    // ✅ Dùng BCrypt để mã hóa mật khẩu
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
