package com.example.membersystem.controller;

import com.example.membersystem.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthService authService;
    
    @Autowired
    private AuthenticationManager authenticationManager;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Map<String, String> request) {
        try {
            String username = request.get("username");
            String password = request.get("password");
            String email = request.get("email");
            
            // 检查username和password是否为null
            if (username == null || password == null) {
                throw new RuntimeException("Username and password are required");
            }
            
            System.out.println("Registering user: " + username + ", email: " + email);

            authService.register(username, password, email);
            return ResponseEntity.ok("Registration successful");
        } catch (RuntimeException e) {
            System.out.println("Registration error: " + e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/register-admin")
    public ResponseEntity<?> registerAdmin(@RequestBody Map<String, String> request) {
        try {
            String username = request.get("username");
            String password = request.get("password");
            String email = request.get("email");

            authService.registerAdmin(username, password, email);
            return ResponseEntity.ok("Admin registration successful");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> request) {
        try {
            System.out.println("Login request received: " + request);
            String username = request.get("username");
            String password = request.get("password");
            
            // 检查username和password是否为null
            if (username == null || password == null) {
                System.out.println("Username or password is null");
                return ResponseEntity.badRequest().body("Username and password are required");
            }
            
            System.out.println("Attempting to authenticate user: " + username);
            // 尝试认证
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(username, password)
            );
            
            // 将认证信息保存到SecurityContext中，建立session
            SecurityContextHolder.getContext().setAuthentication(authentication);
            
            System.out.println("Authentication successful for user: " + username);
            // 认证成功
            return ResponseEntity.ok("Login successful");
        } catch (AuthenticationException e) {
            // 认证失败
            System.out.println("Authentication failed: " + e.getMessage());
            return ResponseEntity.badRequest().body("Invalid username or password");
        } catch (Exception e) {
            // 其他错误
            System.out.println("Login error: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Login failed: " + e.getMessage());
        }
    }
    
    @GetMapping("/login-success")
    public ResponseEntity<?> loginSuccess() {
        // 登录成功后的处理
        return ResponseEntity.ok("Login successful");
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout() {
        // 登出由Spring Security处理
        return ResponseEntity.ok("Logout successful");
    }

}
