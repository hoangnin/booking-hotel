package com.lenin.hotel.authentication.repository;

import com.lenin.hotel.authentication.model.User;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    User findByUsername(String username);
    Optional<User> getByUsername(String username);
    Optional<User> getByEmail(String email);
    Optional<User> getById(long id);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    User findByEmail(@NotBlank @Size(max = 50) @Email String email);
}