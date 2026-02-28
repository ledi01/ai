package com.example.membersystem.service;

import com.example.membersystem.entity.Member;
import com.example.membersystem.entity.User;
import com.example.membersystem.repository.MemberRepository;
import com.example.membersystem.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.boot.CommandLineRunner;

import java.time.LocalDateTime;

@Service
public class AuthService implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public User register(String username, String password, String email) {
        // 检查用户名是否已存在
        if (userRepository.findByUsername(username).isPresent()) {
            throw new RuntimeException("Username already exists");
        }

        System.out.println("AuthService.register - before: username=" + username + ", password=" + password + ", email=" + email);

        // 创建新用户
        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        user.setEmail(email != null ? email : username + "@example.com"); // 如果email为null，使用默认值
        user.setRole("ROLE_USER"); // 默认角色为普通用户
        
        System.out.println("AuthService.register - after: user.username=" + user.getUsername() + ", user.email=" + user.getEmail() + ", user.role=" + user.getRole());

        // 保存用户
        User savedUser = userRepository.save(user);

        // 为普通用户创建会员信息，初始会员时间为0（已过期），需要管理员手动设置
        if ("ROLE_USER".equals(savedUser.getRole())) {
            Member member = new Member();
            member.setUser(savedUser);
            // 将会员开始时间和结束时间都设为当前时间，这样剩余天数为0
            member.setStartDate(LocalDateTime.now());
            member.setEndDate(LocalDateTime.now());
            memberRepository.save(member);
            // 设置用户的会员信息
            savedUser.setMember(member);
            userRepository.save(savedUser);
        }

        return savedUser;
    }

    public User registerAdmin(String username, String password, String email) {
        // 检查用户名是否已存在
        if (userRepository.findByUsername(username).isPresent()) {
            throw new RuntimeException("Username already exists");
        }

        // 检查邮箱是否已存在
        if (userRepository.findByEmail(email).isPresent()) {
            throw new RuntimeException("Email already exists");
        }

        // 创建管理员用户
        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        user.setEmail(email);
        user.setRole("ROLE_ADMIN"); // 管理员角色

        // 保存用户（管理员不需要会员信息）
        return userRepository.save(user);
    }

    public Member extendMembership(Long userId, int days) {
        Member member = memberRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Member not found"));

        LocalDateTime newEndDate = member.getEndDate().plusDays(days);
        member.setEndDate(newEndDate);
        return memberRepository.save(member);
    }

    public User updateUser(Long userId, String email) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        if (email != null && !email.isEmpty()) {
            user.setEmail(email);
        }
        
        return userRepository.save(user);
    }

    public Member updateMember(Long userId, LocalDateTime startDate, LocalDateTime endDate) {
        Member member = memberRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Member not found"));

        if (startDate != null) {
            member.setStartDate(startDate);
        }
        if (endDate != null) {
            member.setEndDate(endDate);
        }
        
        return memberRepository.save(member);
    }

    // 系统启动时初始化管理员用户
    @Override
    public void run(String... args) throws Exception {
        // 检查是否已存在管理员用户
        if (!userRepository.findByUsername("1234").isPresent()) {
            // 创建管理员用户，用户名1234，密码123
            User adminUser = new User();
            adminUser.setUsername("1234");
            adminUser.setPassword(passwordEncoder.encode("123"));
            adminUser.setEmail("admin@example.com");
            adminUser.setRole("ROLE_ADMIN");
            userRepository.save(adminUser);
            System.out.println("Admin user initialized: username=1234, password=123");
        }
    }

}
