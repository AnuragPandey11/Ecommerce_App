package com.ecom.repository;

import com.ecom.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByName(String name);  // ✅ Use String parameter
    boolean existsByName(String name);
}
