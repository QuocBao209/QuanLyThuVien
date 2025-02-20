package com.project.demo.service;

import com.project.demo.entity.User;
import com.project.demo.repository.UserRepository;
import com.project.demo.security.BCryptUtil;
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
        users.forEach(user -> {
            if (!isPasswordHashed(user.getPassword())) {
                user.setPassword(BCryptUtil.hashPassword(user.getPassword()));
            }
        });
        userRepository.saveAll(users);
    }

    // Phương thức kiểm tra mật khẩu đã được mã hóa hay chưa
    private boolean isPasswordHashed(String password) {
        return password != null && password.startsWith("$2a$");
    }


    // Đăng ký người dùng mới
    public User registerUser(User user) {
        user.setPassword(BCryptUtil.hashPassword(user.getPassword()));
        return userRepository.save(user);
    }

    // Xác thực người dùng khi đăng nhập
    public Optional<User> authenticateUser(String username, String password) {
        Optional<User> userOptional = userRepository.findByUsername(username);
        if (userOptional.isPresent() && BCryptUtil.checkPassword(password, userOptional.get().getPassword())) {
            return userOptional; // Trả về user nếu xác thực thành công
        }
        return Optional.empty(); // Trả về Optional rỗng nếu không hợp lệ
    }

    // Kiểm tra xem username đã tồn tại trong database chưa
    public boolean existsByUsername(String username) {
        return userRepository.findByUsername(username).isPresent();
    }

    public boolean resetPassword(String email, String newPassword) {
        Optional<User> userOptional = userRepository.findByEmail(email);
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            user.setPassword(BCryptUtil.hashPassword(newPassword));
            userRepository.save(user);
            return true;
        }
        return false;
    }
    
    // Tìm người dùng bằng username
    public Optional<User> getUserByUsername(String username) {
        return userRepository.findByUsername(username);
    }

}
