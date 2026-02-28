package com.example.membersystem.controller;

import com.example.membersystem.entity.Announcement;
import com.example.membersystem.repository.AnnouncementRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/announcements")
public class AnnouncementController {

    @Autowired
    private AnnouncementRepository announcementRepository;

    @GetMapping("/active")
    public ResponseEntity<List<Announcement>> getActiveAnnouncements() {
        List<Announcement> announcements = announcementRepository.findByActiveTrueOrderByCreatedAtDesc();
        return ResponseEntity.ok(announcements);
    }

    @GetMapping("/all")
    public ResponseEntity<List<Announcement>> getAllAnnouncements() {
        List<Announcement> announcements = announcementRepository.findAllByOrderByCreatedAtDesc();
        return ResponseEntity.ok(announcements);
    }

    @PostMapping("/create")
    public ResponseEntity<?> createAnnouncement(@RequestBody Map<String, String> request) {
        try {
            String title = request.get("title");
            String content = request.get("content");
            
            if (title == null || title.trim().isEmpty()) {
                return ResponseEntity.badRequest().body("标题不能为空");
            }
            if (content == null || content.trim().isEmpty()) {
                return ResponseEntity.badRequest().body("内容不能为空");
            }
            
            Announcement announcement = new Announcement(title, content);
            announcementRepository.save(announcement);
            
            return ResponseEntity.ok("公告发布成功");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("发布公告失败: " + e.getMessage());
        }
    }

    @PostMapping("/deactivate/{id}")
    public ResponseEntity<?> deactivateAnnouncement(@PathVariable Long id) {
        try {
            Announcement announcement = announcementRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("公告不存在"));
            announcement.setActive(false);
            announcementRepository.save(announcement);
            return ResponseEntity.ok("公告已停用");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("操作失败: " + e.getMessage());
        }
    }

    @PostMapping("/activate/{id}")
    public ResponseEntity<?> activateAnnouncement(@PathVariable Long id) {
        try {
            Announcement announcement = announcementRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("公告不存在"));
            announcement.setActive(true);
            announcementRepository.save(announcement);
            return ResponseEntity.ok("公告已启用");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("操作失败: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteAnnouncement(@PathVariable Long id) {
        try {
            announcementRepository.deleteById(id);
            return ResponseEntity.ok("公告已删除");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("删除失败: " + e.getMessage());
        }
    }
}
