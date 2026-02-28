package com.example.membersystem.service;

import com.example.membersystem.entity.User;
import com.example.membersystem.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
        // 手动加载关联的Member对象，确保isEnabled()方法能正确检查会员状态
        if (user.getMember() != null) {
            // 触发懒加载
            user.getMember().isActive();
        }
        System.out.println("Loaded user: " + user.getUsername() + ", role: " + user.getRole() + ", enabled: " + user.isEnabled());
        return user;
    }

}
