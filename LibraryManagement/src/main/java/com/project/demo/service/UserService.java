package com.project.demo.service;

import com.project.demo.entity.User;
import com.project.demo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    // Lấy danh sách người dùng
    public List<User> getUsers() {
        return userRepository.findAll();
    }

    // Lưu tất cả người dùng vào cơ sở dữ liệu
    public void transferData(List<User> users) {
        userRepository.saveAll(users);
    }

    // Đăng ký người dùng mới
    public User registerUser(User user) {
        return userRepository.save(user);  // Lưu thông tin người dùng vào cơ sở dữ liệu
    }
    public boolean authenticateUser(String username, String password) {
        Optional<User> userOptional = userRepository.findByUsername(username);
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            return user.getPassword().equals(password);
        }
        return false;
    }
}
