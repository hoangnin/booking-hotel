package com.lenin.hotel.authentication.repository;

import com.lenin.hotel.authentication.enumuration.ERole;
import com.lenin.hotel.authentication.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByName(ERole role);
}
