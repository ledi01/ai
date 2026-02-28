package com.example.membersystem.controller;

import com.example.membersystem.entity.User;
import com.example.membersystem.repository.UserRepository;
import com.example.membersystem.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AuthService authService;

    @GetMapping("/users")
    public ResponseEntity<?> getUsers() {
        List<User> users = userRepository.findAll();
        return ResponseEntity.ok(users);
    }

    @GetMapping("/users/detail")
    public ResponseEntity<?> getUsersDetail() {
        List<User> users = userRepository.findAll();
        List<Map<String, Object>> userDetails = new ArrayList<>();
        
        for (User user : users) {
            Map<String, Object> detail = new HashMap<>();
            detail.put("id", user.getId());
            detail.put("username", user.getUsername());
            detail.put("email", user.getEmail());
            detail.put("role", user.getRole());
            
            if (user.getMember() != null) {
                detail.put("startDate", user.getMember().getStartDate());
                detail.put("endDate", user.getMember().getEndDate());
                
                // 计算剩余天数
                LocalDateTime now = LocalDateTime.now();
                LocalDateTime endDate = user.getMember().getEndDate();
                long daysRemaining = ChronoUnit.DAYS.between(now, endDate);
                detail.put("daysRemaining", daysRemaining > 0 ? daysRemaining : 0);
                detail.put("isActive", user.getMember().isActive());
            } else {
                detail.put("startDate", null);
                detail.put("endDate", null);
                detail.put("daysRemaining", 0);
                detail.put("isActive", false);
            }
            
            userDetails.add(detail);
        }
        
        return ResponseEntity.ok(userDetails);
    }

    @PostMapping("/extend-membership")
    public ResponseEntity<?> extendMembership(@RequestBody Map<String, Object> request) {
        try {
            Long userId = Long.parseLong(request.get("userId").toString());
            int days = Integer.parseInt(request.get("days").toString());

            authService.extendMembership(userId, days);
            return ResponseEntity.ok("Membership extended successfully");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/update-user")
    public ResponseEntity<?> updateUser(@RequestBody Map<String, Object> request) {
        try {
            Long userId = Long.parseLong(request.get("userId").toString());
            String email = request.get("email") != null ? request.get("email").toString() : null;

            User user = authService.updateUser(userId, email);
            return ResponseEntity.ok("User updated successfully");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/update-member")
    public ResponseEntity<?> updateMember(@RequestBody Map<String, Object> request) {
        try {
            Long userId = Long.parseLong(request.get("userId").toString());
            String startDateStr = request.get("startDate") != null ? request.get("startDate").toString() : null;
            String endDateStr = request.get("endDate") != null ? request.get("endDate").toString() : null;

            LocalDateTime startDate = null;
            LocalDateTime endDate = null;

            if (startDateStr != null && !startDateStr.isEmpty()) {
                startDate = LocalDateTime.parse(startDateStr);
            }
            if (endDateStr != null && !endDateStr.isEmpty()) {
                endDate = LocalDateTime.parse(endDateStr);
            }

            authService.updateMember(userId, startDate, endDate);
            return ResponseEntity.ok("Member updated successfully");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Invalid date format");
        }
    }

}
