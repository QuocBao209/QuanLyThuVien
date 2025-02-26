package com.project.admin.repository;

import com.project.admin.entity.PasswordReset;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PasswordResetRepository extends JpaRepository<PasswordReset, Long> {
    Optional<PasswordReset> findByEmailAndOtp(String email, String otp);
    @Transactional
    void deleteByEmail(String email);
}
