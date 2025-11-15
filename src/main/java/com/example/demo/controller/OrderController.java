package com.example.demo.controller;

import com.example.demo.entity.Order;
import com.example.demo.entity.ShippingAddress;
import com.example.demo.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    /**
     * ✅ API: Lấy tất cả đơn hàng (ADMIN)
     */
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public List<Order> getAllOrders() {
        return orderService.findAll();
    }

    /**
     * ✅ API: Lấy đơn hàng theo ID
     */
    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    @GetMapping("/{id}")
    public Order getOrderById(@PathVariable Integer id) {
        return orderService.findById(id);
    }

    /**
     * ✅ API: Lấy danh sách đơn hàng theo userId
     * - Dùng cho trang OrderPage.tsx
     */
    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    @GetMapping("/user/{userId}")
    public List<Order> getOrdersByUser(@PathVariable Integer userId) {
        return orderService.findByUserId(userId);
    }

    /**
     * ✅ API: Lấy danh sách đơn hàng của user đang đăng nhập (qua token)
     */
    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    @GetMapping("/my")
    public List<Order> getMyOrders(Principal principal) {
        return orderService.findByUserEmail(principal.getName());
    }

    /**
     * ✅ API: Thanh toán (Checkout)
     */
    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    @PostMapping("/checkout")
    public Order checkout(
            @RequestParam Integer userId,
            @RequestParam Integer cartId,
            @RequestParam(defaultValue = "COD") String paymentMethod,
            @RequestBody ShippingAddress address) {

        return orderService.checkout(userId, cartId, address, paymentMethod);
    }

    /**
     * ✅ API: Cập nhật trạng thái đơn hàng (ADMIN)
     */
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}/status")
    public Order updateStatus(@PathVariable Integer id, @RequestParam String status) {
        return orderService.updateStatus(id, status);
    }

    /**
     * ✅ API: Xóa đơn hàng (ADMIN)
     */
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public void deleteOrder(@PathVariable Integer id) {
        orderService.delete(id);
    }
    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    @PostMapping("/buy-now")
    public Order buyNow(
            @RequestParam Integer userId,
            @RequestParam Integer productId,
            @RequestParam Double price, // ✅ nhận giá đã giảm từ frontend
            @RequestParam(defaultValue = "1") Integer quantity,
            @RequestParam(defaultValue = "COD") String paymentMethod,
            @RequestBody ShippingAddress address) {

        return orderService.buyNow(userId, productId, price, quantity, address, paymentMethod);
    }

}
