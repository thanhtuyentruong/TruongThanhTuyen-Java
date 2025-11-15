package com.example.demo.controller;

import com.example.demo.entity.CartItem;
import com.example.demo.service.CartItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/cart-items")
@RequiredArgsConstructor
public class CartItemController {

    private final CartItemService cartItemService;

    // ADMIN: xem toàn bộ cart items
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public ResponseEntity<List<CartItem>> getAll() {
        return ResponseEntity.ok(cartItemService.findAll());
    }

    // USER/ADMIN: xem chi tiết 1 item
    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable Integer id) {
        try {
            return ResponseEntity.ok(cartItemService.findById(id));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // USER/ADMIN: thêm item bằng cartId (giữ nguyên API cũ)
    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    @PostMapping
    public ResponseEntity<?> addItem(
            @RequestParam Integer cartId,
            @RequestParam Integer productId,
            @RequestParam Integer quantity) {
        try {
            CartItem item = cartItemService.addToCart(cartId, productId, quantity);
            return ResponseEntity.ok(item);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // ✅ USER/ADMIN: thêm item CHỈ CẦN userId (API mới – dùng khi cart tạo sẵn lúc đăng ký)
    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    @PostMapping("/add-by-user")
    public ResponseEntity<?> addByUser(
            @RequestParam Integer userId,
            @RequestParam Integer productId,
            @RequestParam Integer quantity) {
        try {
            CartItem item = cartItemService.addByUserId(userId, productId, quantity);
            return ResponseEntity.ok(item);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // USER/ADMIN: cập nhật số lượng 1 item
    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    @PutMapping("/{id}")
    public ResponseEntity<?> updateQuantity(
            @PathVariable Integer id,
            @RequestParam Integer quantity) {
        try {
            return ResponseEntity.ok(cartItemService.updateQuantity(id, quantity));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // USER/ADMIN: xoá item
    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    @DeleteMapping("/{id}")
    public ResponseEntity<?> remove(@PathVariable Integer id) {
        try {
            cartItemService.remove(id);
            return ResponseEntity.ok(Map.of("message", "Đã xóa sản phẩm khỏi giỏ hàng (item id = " + id + ")"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
