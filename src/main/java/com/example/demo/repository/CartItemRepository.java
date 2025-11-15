package com.example.demo.repository;

import com.example.demo.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.List;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Integer> {

    // ✅ Thêm dòng này để fix lỗi
    List<CartItem> findByCart_Id(Integer cartId);

    Optional<CartItem> findByCart_IdAndProduct_Id(Integer cartId, Integer productId);
}
