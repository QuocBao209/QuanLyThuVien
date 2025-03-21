package com.project.demo.repository;

import com.project.demo.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    List<User> findAllByRole(String role); // Sửa lại
    List<User> findByRoleAndNameContainingOrRoleAndEmailContaining(String role1, String name, String role2, String email);


}


