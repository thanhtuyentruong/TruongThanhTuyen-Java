package com.example.demo.service;

import com.example.demo.entity.Cart;
import com.example.demo.entity.CartItem;
import com.example.demo.entity.Product;
import com.example.demo.repository.CartItemRepository;
import com.example.demo.repository.CartRepository;
import com.example.demo.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CartItemService {

    private final CartItemRepository cartItemRepo;
    private final CartRepository cartRepo;
    private final ProductRepository productRepo;
    private final CartService cartService;

    public List<CartItem> findAll() {
        return cartItemRepo.findAll();
    }

    public CartItem findById(Integer id) {
        return cartItemRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm trong giỏ ID: " + id));
    }

    /** Thêm sản phẩm vào giỏ bằng cartId (API cũ) */
    public CartItem addToCart(Integer cartId, Integer productId, Integer quantity) {
        Cart cart = cartRepo.findById(cartId)
                .orElseThrow(() -> new RuntimeException("Giỏ hàng không tồn tại!"));
        Product product = productRepo.findById(productId)
                .orElseThrow(() -> new RuntimeException("Sản phẩm không tồn tại!"));

        Optional<CartItem> existingOpt =
                cartItemRepo.findByCart_IdAndProduct_Id(cartId, productId);

        CartItem item;
        if (existingOpt.isPresent()) {
            item = existingOpt.get();
            int newQuantity = item.getQuantity() + quantity;
            item.setQuantity(newQuantity);
            item.setSubtotal(product.getPrice() * newQuantity);
        } else {
            item = CartItem.builder()
                    .cart(cart)
                    .product(product)
                    .quantity(quantity)
                    .subtotal(product.getPrice() * quantity)
                    .build();
        }

        CartItem saved = cartItemRepo.save(item);
        cartService.updateTotal(cartId);
        return saved;
    }

    /** ✅ Thêm sản phẩm bằng userId (API mới) */
    public CartItem addByUserId(Integer userId, Integer productId, Integer quantity) {
        // Nếu cart đã được tạo sẵn khi đăng ký, vẫn dùng findOrCreate để an toàn
        Cart cart = cartService.findOrCreateByUserId(userId);
        return addToCart(cart.getId(), productId, quantity);
    }

    /** Cập nhật số lượng một item */
    public CartItem updateQuantity(Integer id, Integer newQuantity) {
        CartItem item = findById(id);
        Product product = item.getProduct();
        item.setQuantity(newQuantity);
        item.setSubtotal(product.getPrice() * newQuantity);
        CartItem updated = cartItemRepo.save(item);
        cartService.updateTotal(item.getCart().getId());
        return updated;
    }

    /** Xoá item khỏi giỏ */
    public void remove(Integer id) {
        CartItem item = findById(id);
        Integer cartId = item.getCart().getId();
        cartItemRepo.delete(item);
        cartService.updateTotal(cartId);
    }
}
