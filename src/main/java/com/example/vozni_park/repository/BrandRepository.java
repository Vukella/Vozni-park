package com.example.vozni_park.repository;

import com.example.vozni_park.entity.Brand;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BrandRepository extends JpaRepository<Brand, Long> {
    
    // Custom query methods
    Optional<Brand> findByName(String name);
    
    boolean existsByName(String name);
}
