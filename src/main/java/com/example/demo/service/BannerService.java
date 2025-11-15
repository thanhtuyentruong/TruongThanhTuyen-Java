package com.example.demo.service;

import com.example.demo.entity.Banner;
import com.example.demo.repository.BannerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BannerService {

    private final BannerRepository bannerRepo;
    private static final String UPLOAD_DIR = System.getProperty("user.dir") + "/uploads/banners/";

    // 📍 đường dẫn thư mục lưu ảnh

    public List<Banner> findAll() {
        return bannerRepo.findAll();
    }

    public Banner findById(Integer id) {
        return bannerRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy banner id = " + id));
    }

    public Banner create(String title, String link, MultipartFile image) {
        if (bannerRepo.existsByTitle(title)) {
            throw new RuntimeException("Tiêu đề banner đã tồn tại!");
        }

        String fileName = saveImage(image);

        Banner b = new Banner();
        b.setTitle(title);
        b.setLink(link);
        b.setImageUrl(fileName);
        b.setActive(true);
        return bannerRepo.save(b);
    }

    public Banner update(Integer id, String title, String link, MultipartFile image, Boolean active) {
        Banner existing = findById(id);

        if (!existing.getTitle().equalsIgnoreCase(title) && bannerRepo.existsByTitle(title)) {
            throw new RuntimeException("Tiêu đề banner '" + title + "' đã tồn tại!");
        }

        existing.setTitle(title);
        existing.setLink(link);
        if (active != null)
            existing.setActive(active);

        if (image != null && !image.isEmpty()) {
            String fileName = saveImage(image);
            existing.setImageUrl(fileName);
        }

        return bannerRepo.save(existing);
    }

    public void delete(Integer id) {
        Banner banner = findById(id);
        bannerRepo.delete(banner);
    }

    // 📁 Lưu ảnh vào thư mục uploads/banners/
    private String saveImage(MultipartFile image) {
        if (image == null || image.isEmpty())
            return null;

        try {
            File uploadDir = new File(UPLOAD_DIR);
            if (!uploadDir.exists())
                uploadDir.mkdirs(); // tạo thư mục nếu chưa có

            String fileName = UUID.randomUUID() + "_" + image.getOriginalFilename();
            File dest = new File(uploadDir, fileName);
            image.transferTo(dest);

            return "/uploads/banners/" + fileName;
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Không thể lưu ảnh banner!");
        }
    }

}
