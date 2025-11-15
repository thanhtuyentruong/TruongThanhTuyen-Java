package com.example.demo.service;

import com.example.demo.entity.Role;
import com.example.demo.entity.User;
import com.example.demo.entity.Cart;
import com.example.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepo;
    private final PasswordEncoder encoder;
    private final CartService cartService; // ✅ thêm CartService

    // ✅ Đăng ký user thường (mặc định ROLE_USER)
    public User register(String username, String email, String password) {
        if (userRepo.existsByUsername(username)) {
            throw new RuntimeException("⚠️ Tên người dùng đã tồn tại!");
        }
        if (userRepo.existsByEmail(email)) {
            throw new RuntimeException("⚠️ Email đã được sử dụng!");
        }

        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(encoder.encode(password));
        user.setRole(com.example.demo.entity.Role.USER);

        User saved = userRepo.save(user);

        // ✅ Tạo giỏ hàng cho user ngay sau khi đăng ký
        Cart cart = new Cart();
        cart.setUser(saved);
        cart.setTotal(0.0);
        cartService.create(cart);

        return saved;
    }

    // ✅ Đăng ký admin
    public User registerAdmin(String username, String email, String password) {
        if (userRepo.existsByUsername(username)) {
            throw new RuntimeException("⚠️ Tên người dùng đã tồn tại!");
        }
        if (userRepo.existsByEmail(email)) {
            throw new RuntimeException("⚠️ Email đã được sử dụng!");
        }

        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(encoder.encode(password));
        user.setRole(com.example.demo.entity.Role.ADMIN);

        User saved = userRepo.save(user);

        // ✅ Tạo giỏ hàng cho admin luôn (để test)
        Cart cart = new Cart();
        cart.setUser(saved);
        cart.setTotal(0.0);
        cartService.create(cart);

        return saved;
    }

    public List<User> findAll() {
        return userRepo.findAll();
    }

    public User findById(Long id) {
        return userRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng với ID: " + id));
    }

    public User findByUsername(String username) {
        return userRepo.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng với username: " + username));
    }

    public boolean existsByUsername(String username) {
        return userRepo.existsByUsername(username);
    }

    public boolean existsByEmail(String email) {
        return userRepo.existsByEmail(email);
    }

    public User updateUser(Long id, User updated) {
        User existing = findById(id);

        if (updated.getUsername() != null && !updated.getUsername().isBlank()) {
            existing.setUsername(updated.getUsername());
        }

        if (updated.getEmail() != null && !updated.getEmail().isBlank()) {
            if (!updated.getEmail().equals(existing.getEmail()) && userRepo.existsByEmail(updated.getEmail())) {
                throw new RuntimeException("⚠️ Email này đã được sử dụng!");
            }
            existing.setEmail(updated.getEmail());
        }

        if (updated.getRole() != null) {
            existing.setRole(updated.getRole());
        }

        if (updated.getPassword() != null && !updated.getPassword().isBlank()) {
            existing.setPassword(encoder.encode(updated.getPassword()));
        }

        return userRepo.save(existing);
    }

    public void deleteUser(Long id) {
        if (!userRepo.existsById(id)) {
            throw new RuntimeException("Không tìm thấy user để xoá!");
        }
        userRepo.deleteById(id);
    }

    public User save(User user) {
        return userRepo.save(user);
    }
}
