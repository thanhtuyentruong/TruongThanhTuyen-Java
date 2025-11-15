package com.example.demo.config;

import java.io.IOException;
import java.util.List;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.example.demo.entity.User;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.JwtService;

import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserRepository userRepo;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {

        String uri = request.getRequestURI();
        String method = request.getMethod();

        // 👉 Log để debug
        System.out.println("🔹 [JwtAuthFilter] " + method + " " + uri);

        // ✅ Bỏ qua preflight CORS (OPTIONS)
        if ("OPTIONS".equalsIgnoreCase(method)) {
            filterChain.doFilter(request, response);
            return;
        }

        final String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            System.out.println("⚠️ Không có Authorization header hợp lệ → Bỏ qua filter.");
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7);

        try {
            String username = jwtService.extractUsername(token);
            System.out.println("✅ Token username: " + username);

            if (username != null && jwtService.validate(token)
                    && SecurityContextHolder.getContext().getAuthentication() == null) {

                // ✅ Lấy user bằng username (có thể là email hoặc username tuỳ hệ thống)
                User user = userRepo.findByUsername(username)
                        .orElseGet(() -> userRepo.findByEmail(username).orElse(null));

                if (user != null) {
                    String role = "ROLE_" + user.getRole().name();
                    var authorities = List.of(new SimpleGrantedAuthority(role));

                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(user, null,
                            authorities);
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                    SecurityContextHolder.getContext().setAuthentication(authToken);
                    System.out.println("✅ Authenticated: " + user.getUsername() + " (" + role + ")");
                } else {
                    System.out.println("❌ Không tìm thấy user trong DB với username/email: " + username);
                }
            }
        } catch (JwtException e) {
            System.out.println("❌ JWT invalid: " + e.getMessage());
        }

        filterChain.doFilter(request, response);
    }
}
