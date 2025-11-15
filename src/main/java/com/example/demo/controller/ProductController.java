package com.example.demo.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.example.demo.entity.Product;
import com.example.demo.service.ProductService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    // ✅ Lấy tất cả sản phẩm
    @GetMapping
    public ResponseEntity<?> getAll() {
        return ResponseEntity.ok(productService.findAll());
    }

    // ✅ Lấy sản phẩm theo ID
    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable Integer id) {
        return ResponseEntity.ok(productService.findById(id));
    }

    // ✅ Lấy sản phẩm theo danh mục
    @GetMapping("/by-category/{categoryId}")
    public ResponseEntity<?> getByCategory(@PathVariable Integer categoryId) {
        return ResponseEntity.ok(productService.findByCategory(categoryId));
    }

    // ✅ API lọc nâng cao
    @GetMapping("/filter")
    public ResponseEntity<?> filterProducts(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer categoryId,
            @RequestParam(defaultValue = "default") String sort,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size) {
        return ResponseEntity.ok(productService.filterProducts(keyword, categoryId, sort, page, size));
    }

    // ✅ Tạo mới sản phẩm (ADMIN)
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<?> create(
            @RequestParam String name,
            @RequestParam(required = false) String description,
            @RequestParam double price,
            @RequestParam int quantity,
            @RequestParam Integer categoryId,
            @RequestParam(required = false) MultipartFile image) {
        try {
            Product p = new Product();
            p.setName(name);
            p.setDescription(description);
            p.setPrice(price);
            p.setQuantity(quantity);

            return ResponseEntity.ok(productService.create(p, categoryId, image));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // ✅ Cập nhật sản phẩm (ADMIN)
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<?> update(
            @PathVariable Integer id,
            @RequestParam String name,
            @RequestParam(required = false) String description,
            @RequestParam double price,
            @RequestParam int quantity,
            @RequestParam Integer categoryId,
            @RequestParam(required = false) MultipartFile image) {
        try {
            Product p = new Product();
            p.setName(name);
            p.setDescription(description);
            p.setPrice(price);
            p.setQuantity(quantity);

            return ResponseEntity.ok(productService.update(id, p, categoryId, image));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // ✅ Cập nhật ảnh riêng
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}/image")
    public ResponseEntity<?> updateImage(
            @PathVariable Integer id,
            @RequestParam MultipartFile image) {
        try {
            return ResponseEntity.ok(productService.updateImage(id, image));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // ✅ Giảm tồn kho khi đặt hàng
    @PutMapping("/{id}/update-stock")
    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    public ResponseEntity<?> updateStock(
            @PathVariable Integer id,
            @RequestParam int quantity) {
        try {
            productService.reduceStock(id, quantity);
            return ResponseEntity.ok(Map.of("message", "Đã trừ " + quantity + " sản phẩm khỏi kho"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // ✅ Xóa sản phẩm
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Integer id) {
        try {
            productService.delete(id);
            return ResponseEntity.ok(Map.of("message", "Đã xóa sản phẩm id = " + id));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // ✅ Lấy sản phẩm liên quan
    @GetMapping("/{id}/related")
    public ResponseEntity<?> getRelated(@PathVariable Integer id) {
        try {
            return ResponseEntity.ok(productService.findRelatedProducts(id));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
