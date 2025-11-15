package com.example.demo.repository;

import com.example.demo.entity.Cart;
import org.springframework.data.jpa.repository.JpaRepository;
import com.example.demo.entity.User;
import java.util.Optional;
public interface CartRepository extends JpaRepository<Cart, Integer> {
     Optional<Cart> findByUser(User user); // 👈 cần có
}
