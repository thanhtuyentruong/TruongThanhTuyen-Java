package com.example.demo.controller;

import com.example.demo.entity.Banner;
import com.example.demo.service.BannerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/banners")
@RequiredArgsConstructor
public class BannerController {

    private final BannerService bannerService;

    // ✅ Xem tất cả banner (public)
    @GetMapping
    public List<Banner> getAll() {
        return bannerService.findAll();
    }

    // ✅ Xem chi tiết
    @GetMapping("/{id}")
    public Banner getById(@PathVariable Integer id) {
        return bannerService.findById(id);
    }

    // ✅ Tạo mới (chỉ ADMIN)
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<?> create(
            @RequestParam String title,
            @RequestParam(required = false) String link,
            @RequestParam(required = false) MultipartFile image) {
        try {
            Banner b = bannerService.create(title, link, image);
            return ResponseEntity.ok(b);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // ✅ Cập nhật banner (chỉ ADMIN)
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<?> update(
            @PathVariable Integer id,
            @RequestParam String title,
            @RequestParam(required = false) String link,
            @RequestParam(required = false) MultipartFile image,
            @RequestParam(required = false) Boolean active) {
        try {
            Banner b = bannerService.update(id, title, link, image, active);
            return ResponseEntity.ok(b);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // ✅ Xóa banner (chỉ ADMIN)
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Integer id) {
        bannerService.delete(id);
        return ResponseEntity.ok(Map.of("message", "Đã xóa banner id = " + id));
    }
}
