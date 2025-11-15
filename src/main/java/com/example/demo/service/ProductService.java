package com.example.demo.service;

import java.io.File;
import java.io.IOException;
import java.util.*;

import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.example.demo.entity.Category;
import com.example.demo.entity.Product;
import com.example.demo.repository.CategoryRepository;
import com.example.demo.repository.ProductRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepo;
    private final CategoryRepository categoryRepo;

    private static final String UPLOAD_DIR = System.getProperty("user.dir") + "/uploads/products/";

    // ✅ Lấy tất cả sản phẩm
    public List<Product> findAll() {
        return productRepo.findAll();
    }

    // ✅ Lấy theo ID
    public Product findById(Integer id) {
        return productRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm có id = " + id));
    }

    // ✅ Lấy theo danh mục
    public List<Product> findByCategory(Integer categoryId) {
        return productRepo.findByCategoryId(categoryId);
    }

    // ✅ Lọc sản phẩm có tìm kiếm, danh mục, sắp xếp, phân trang
    public Map<String, Object> filterProducts(String keyword, Integer categoryId, String sort, int page, int size) {
        Sort sortOrder = switch (sort) {
            case "price-asc" -> Sort.by("price").ascending();
            case "price-desc" -> Sort.by("price").descending();
            case "name-asc" -> Sort.by("name").ascending();
            case "name-desc" -> Sort.by("name").descending();
            case "newest" -> Sort.by("id").descending();
            default -> Sort.unsorted();
        };

        Pageable pageable = PageRequest.of(page, size, sortOrder);
        Page<Product> resultPage;

        if (keyword != null && !keyword.isBlank() && categoryId != null) {
            resultPage = productRepo.findByNameContainingIgnoreCaseAndCategoryId(keyword, categoryId, pageable);
        } else if (keyword != null && !keyword.isBlank()) {
            resultPage = productRepo.findByNameContainingIgnoreCase(keyword, pageable);
        } else if (categoryId != null) {
            resultPage = productRepo.findByCategoryId(categoryId, pageable);
        } else {
            resultPage = productRepo.findAll(pageable);
        }

        Map<String, Object> response = new HashMap<>();
        response.put("products", resultPage.getContent());
        response.put("currentPage", resultPage.getNumber());
        response.put("totalPages", resultPage.getTotalPages());
        response.put("totalItems", resultPage.getTotalElements());
        return response;
    }

    // ✅ Tạo mới sản phẩm
    public Product create(Product p, Integer categoryId, MultipartFile image) {
        if (p.getName() == null || p.getName().trim().isEmpty()) {
            throw new RuntimeException("Tên sản phẩm không được để trống!");
        }

        if (productRepo.existsByName(p.getName())) {
            throw new RuntimeException("Tên sản phẩm '" + p.getName() + "' đã tồn tại!");
        }

        Category cate = categoryRepo.findById(categoryId)
                .orElseThrow(() -> new RuntimeException("Danh mục không tồn tại!"));
        p.setCategory(cate);

        if (image != null && !image.isEmpty()) {
            p.setImg(saveImage(image));
        }

        return productRepo.save(p);
    }

    // ✅ Cập nhật sản phẩm
    public Product update(Integer id, Product p, Integer categoryId, MultipartFile image) {
        Product existing = findById(id);

        if (!existing.getName().equalsIgnoreCase(p.getName())
                && productRepo.existsByName(p.getName())) {
            throw new RuntimeException("Tên sản phẩm '" + p.getName() + "' đã tồn tại!");
        }

        Category cate = categoryRepo.findById(categoryId)
                .orElseThrow(() -> new RuntimeException("Danh mục không tồn tại!"));

        existing.setName(p.getName());
        existing.setDescription(p.getDescription());
        existing.setPrice(p.getPrice());
        existing.setQuantity(p.getQuantity());
        existing.setCategory(cate);

        if (image != null && !image.isEmpty()) {
            existing.setImg(saveImage(image));
        }

        return productRepo.save(existing);
    }

    // ✅ Cập nhật riêng ảnh
    public Product updateImage(Integer id, MultipartFile image) {
        Product existing = findById(id);

        if (image == null || image.isEmpty()) {
            throw new RuntimeException("Vui lòng chọn ảnh để tải lên!");
        }

        existing.setImg(saveImage(image));
        return productRepo.save(existing);
    }

    // ✅ Xóa sản phẩm
    public void delete(Integer id) {
        Product existing = findById(id);
        productRepo.delete(existing);
    }

    // ✅ Hàm lưu ảnh
    private String saveImage(MultipartFile image) {
        try {
            File uploadDir = new File(UPLOAD_DIR);
            if (!uploadDir.exists())
                uploadDir.mkdirs();

            String fileName = UUID.randomUUID() + "_" + image.getOriginalFilename();
            File dest = new File(uploadDir, fileName);
            image.transferTo(dest);

            return "/uploads/products/" + fileName;
        } catch (IOException e) {
            throw new RuntimeException("❌ Lỗi khi lưu ảnh: " + e.getMessage());
        }
    }

    // ✅ Lấy sản phẩm liên quan cùng danh mục
    public List<Product> findRelatedProducts(Integer productId) {
        Product product = productRepo.findById(productId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm ID: " + productId));

        if (product.getCategory() == null) {
            throw new RuntimeException("Sản phẩm này chưa có danh mục!");
        }

        return productRepo.findByCategory_Id(product.getCategory().getId())
                .stream()
                .filter(p -> !p.getId().equals(productId))
                .limit(8)
                .toList();
    }

    // ✅ Giảm số lượng tồn kho khi đặt hàng
    public void reduceStock(Integer productId, int quantity) {
        Product product = findById(productId);

        if (product.getQuantity() < quantity) {
            throw new RuntimeException("❌ Sản phẩm '" + product.getName() + "' không đủ hàng trong kho!");
        }

        product.setQuantity(product.getQuantity() - quantity);
        productRepo.save(product);
    }

}
