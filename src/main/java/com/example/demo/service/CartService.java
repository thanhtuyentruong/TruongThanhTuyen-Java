package com.example.demo.service;

import com.example.demo.entity.Cart;
import com.example.demo.entity.CartItem;
import com.example.demo.entity.User;
import com.example.demo.repository.CartItemRepository;
import com.example.demo.repository.CartRepository;
import com.example.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CartService {

    private final CartRepository cartRepo;
    private final CartItemRepository cartItemRepo;
    private final UserRepository userRepo;

    public List<Cart> findAll() {
        return cartRepo.findAll();
    }

    public Cart findById(Integer id) {
        return cartRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy giỏ hàng ID: " + id));
    }

    public Cart findOrCreateByUserId(Integer userId) {
        User user = userRepo.findById(userId.longValue())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng ID: " + userId));

        return cartRepo.findByUser(user)
                .orElseGet(() -> {
                    Cart newCart = new Cart();
                    newCart.setUser(user);
                    newCart.setTotal(0.0);
                    return cartRepo.save(newCart);
                });
    }

    // ✅ thêm mới: tìm/tạo theo username (dùng cho /carts/my nếu JWT trả về username)
    public Cart findOrCreateByUsername(String username) {
        User user = userRepo.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy tài khoản username: " + username));

        return cartRepo.findByUser(user)
                .orElseGet(() -> {
                    Cart newCart = new Cart();
                    newCart.setUser(user);
                    newCart.setTotal(0.0);
                    return cartRepo.save(newCart);
                });
    }

    public Cart findByUserId(Integer userId) {
        User user = userRepo.findById(userId.longValue())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng ID: " + userId));

        return cartRepo.findByUser(user)
                .orElseThrow(() -> new RuntimeException("Người dùng này chưa có giỏ hàng."));
    }

    public Cart findByUserEmail(String email) {
        User user = userRepo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy tài khoản với email: " + email));

        return cartRepo.findByUser(user)
                .orElseThrow(() -> new RuntimeException("Tài khoản này chưa có giỏ hàng."));
    }

    public Cart create(Cart cart) {
        if (cart.getUser() == null) {
            throw new RuntimeException("Giỏ hàng phải gắn với một người dùng!");
        }

        if (cart.getUser().getId() != null) {
            User user = userRepo.findById(cart.getUser().getId().longValue())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng ID: " + cart.getUser().getId()));
            cart.setUser(user);
        }

        cart.setTotal(0.0);
        return cartRepo.save(cart);
    }

    public Cart update(Integer id, Cart cartUpdate) {
        Cart cart = findById(id);

        if (cartUpdate.getUser() != null && cartUpdate.getUser().getId() != null) {
            User user = userRepo.findById(cartUpdate.getUser().getId().longValue())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng ID: " + cartUpdate.getUser().getId()));
            cart.setUser(user);
        }

        if (cartUpdate.getTotal() != null) {
            cart.setTotal(cartUpdate.getTotal());
        }

        return cartRepo.save(cart);
    }

    public void delete(Integer id) {
        Cart cart = findById(id);
        cartRepo.delete(cart);
    }

    @Transactional
    public void clearCart(Integer cartId) {
        List<CartItem> items = cartItemRepo.findByCart_Id(cartId);
        cartItemRepo.deleteAll(items);

        Cart cart = findById(cartId);
        cart.setTotal(0.0);
        cartRepo.save(cart);
    }

    @Transactional
    public void updateTotal(Integer cartId) {
        Cart cart = findById(cartId);
        List<CartItem> items = cartItemRepo.findByCart_Id(cartId);

        double total = items.stream()
                .mapToDouble(CartItem::getSubtotal)
                .sum();

        cart.setTotal(total);
        cartRepo.save(cart);
    }
}
