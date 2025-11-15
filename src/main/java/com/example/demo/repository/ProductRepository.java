package com.example.demo.repository;

import com.example.demo.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Integer> {
    boolean existsByName(String name);

    List<Product> findByCategoryId(Integer categoryId);

    Page<Product> findByNameContainingIgnoreCase(String keyword, Pageable pageable);

    Page<Product> findByNameContainingIgnoreCaseAndCategoryId(String keyword, Integer categoryId, Pageable pageable);

    Page<Product> findByCategoryId(Integer categoryId, Pageable pageable);

    List<Product> findByCategory_Id(Integer categoryId);

}
