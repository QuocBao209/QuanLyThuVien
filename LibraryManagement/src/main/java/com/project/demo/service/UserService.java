package com.project.demo.service;

import com.project.demo.entity.User;
import com.project.demo.repository.UserRepository;
import com.project.demo.security.BCryptUtil;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
    // Lấy danh sách người dùng
    public List<User> getUsers() {
        return userRepository.findAll();
    }

    public List<User> searchUsers(String keyword) {
        return userRepository.findByRoleAndNameContainingOrRoleAndEmailContaining("USER", keyword, "USER", keyword);
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
    
    // Cập nhật status độc giả
    public void updateStatus(Long userId, String newStatus) {
    	Optional<User> userOptional = userRepository.findById(userId);
    	if (userOptional.isPresent()) {
    		User user = userOptional.get();
    		user.setStatus(newStatus);
    		userRepository.save(user);
    	}
    }

    // Xác định role tài khoản theo email (Bảo)
    public String getUserRole(String email) {
    	return userRepository.findByEmail(email)
    			.map(User::getRole)
    			.orElseThrow(() -> new IllegalArgumentException("Email không tồn tại trong hệ thống!"));		
    }

    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }

    public void updateUser(User user) {
        userRepository.save(user);
    }

    public void checkAndLockUser(Long userId) {
        User user = userRepository.findById(userId).orElse(null);
        if (user != null && user.getViolationCount() >= 3) {
            user.setStatus("Khóa");
            userRepository.save(user);
        }
    }

}
