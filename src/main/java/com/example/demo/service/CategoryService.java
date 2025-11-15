package com.example.demo.service;

import com.example.demo.entity.Category;
import com.example.demo.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepo;

    public List<Category> findAll() {
        return categoryRepo.findAll();
    }

    public Category findById(Integer id) {
        return categoryRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy danh mục có id = " + id));
    }

    public Category create(Category c) {
        // 🔹 Kiểm tra trùng tên trước khi thêm mới
        if (categoryRepo.existsByName(c.getName())) {
            throw new RuntimeException("Tên danh mục '" + c.getName() + "' đã tồn tại!");
        }
        return categoryRepo.save(c);
    }

    public Category update(Integer id, Category c) {
        Category existing = findById(id);

        // 🔹 Nếu tên mới khác tên cũ → kiểm tra xem có trùng ai khác không
        if (!existing.getName().equalsIgnoreCase(c.getName())
                && categoryRepo.existsByName(c.getName())) {
            throw new RuntimeException("Tên danh mục '" + c.getName() + "' đã tồn tại!");
        }

        existing.setName(c.getName());
        existing.setDescription(c.getDescription());
        return categoryRepo.save(existing);
    }

    public void delete(Integer id) {
        categoryRepo.deleteById(id);
    }
}
