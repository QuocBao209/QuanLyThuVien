package com.project.admin.service;

import com.project.admin.entity.PasswordReset;
import com.project.admin.repository.PasswordResetRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Random;

@Service
public class PasswordResetService {
    @Autowired
    private PasswordResetRepository passwordResetRepository;

    public String generateOTP() {
        Random rand = new Random();
        return String.format("%06d", rand.nextInt(1000000));
    }

    public void saveOTP(String email, String otp) {
        passwordResetRepository.deleteByEmail(email);

        PasswordReset passwordReset = new PasswordReset();
        passwordReset.setEmail(email);
        passwordReset.setOtp(otp);
        passwordReset.setCreatedAt(LocalDateTime.now());
        passwordReset.setExpiredAt(LocalDateTime.now().plusMinutes(5));

        passwordResetRepository.save(passwordReset);
    }

    public boolean verifyOTP(String email, String otp) {
        Optional<PasswordReset> record = passwordResetRepository.findByEmailAndOtp(email, otp);
        return record.isPresent() && record.get().getExpiredAt().isAfter(LocalDateTime.now());
    }
}
