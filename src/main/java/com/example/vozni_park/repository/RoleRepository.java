package com.example.vozni_park.repository;

import com.example.vozni_park.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
    
    // Custom query methods
    Optional<Role> findByName(String name);
    
    boolean existsByName(String name);
}
