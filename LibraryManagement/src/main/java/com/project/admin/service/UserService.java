package com.project.admin.service;

import com.project.admin.entity.User;
import com.project.admin.repository.UserRepository;
import com.project.admin.security.BCryptUtil;
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

    // Tìm user theo ID
    public Optional<User> findById(Long userId) {
        return userRepository.findById(userId);
    }

    // Lấy danh sách độc giả (role: USER)
    public List<User> getAllUsersWithRoleUser() {
        return userRepository.findAllByRole("USER");
    }

    // Tìm kiếm user theo tên hoặc email
    public List<User> searchUsers(String keyword) {
        return userRepository.findByRoleAndNameContainingOrRoleAndEmailContaining("USER", keyword, "USER", keyword);
    }

    // Lưu danh sách người dùng vào database (Kiểm tra hash password trước khi lưu)
    public void transferData(List<User> users) {
        users.forEach(user -> {
            if (!isPasswordHashed(user.getPassword())) {
                user.setPassword(BCryptUtil.hashPassword(user.getPassword()));
            }
        });
        userRepository.saveAll(users);
    }

    // Lưu thông tin một người dùng
    public void save(User user) {
        userRepository.save(user);
    }

    // Kiểm tra mật khẩu đã được hash hay chưa
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
            return userOptional; // Xác thực thành công
        }
        return Optional.empty(); // Sai tài khoản hoặc mật khẩu
    }

    // Kiểm tra xem username đã tồn tại chưa
    public boolean existsByUsername(String username) {
        return userRepository.findByUsername(username).isPresent();
    }

    // Đặt lại mật khẩu người dùng
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

    // Cập nhật trạng thái của độc giả
    public void updateStatus(Long userId, String newStatus) {
        Optional<User> userOptional = userRepository.findById(userId);
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            user.setStatus(newStatus);
            userRepository.save(user);
        }
    }

    // Xác định vai trò tài khoản dựa trên email
    public String getUserRole(String email) {
        return userRepository.findByEmail(email)
                .map(User::getRole)
                .orElseThrow(() -> new IllegalArgumentException("Email không tồn tại trong hệ thống!"));
    }
   
    // Lấy danh sách người dùng có vi phạm
    public List<User> getUsersWithViolations() {
        return userRepository.findUsersWithViolations();
    }

    // Tìm kiếm người dùng có vi phạm theo từ khóa (tên hoặc email)
    public List<User> searchUsersWithViolations(String keyword) {
        return userRepository.searchUsersWithViolations(keyword);
    }
}
