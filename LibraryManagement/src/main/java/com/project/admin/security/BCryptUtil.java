package com.project.admin.security;
import org.mindrot.jbcrypt.BCrypt;

public class BCryptUtil {

    private BCryptUtil() {
        // Chặn tạo instance
    }

    // Mã hóa mật khẩu
    public static String hashPassword(String password) {
        return BCrypt.hashpw(password, BCrypt.gensalt(12));
    }

    // Kiểm tra mật khẩu nhập vào với mật khẩu đã mã hóa
    public static boolean checkPassword(String plainPassword, String hashedPassword) {
        return BCrypt.checkpw(plainPassword, hashedPassword);
    }
}

