package com.example.vozni_park.repository;

import com.example.vozni_park.entity.DriverPermit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DriverPermitRepository extends JpaRepository<DriverPermit, Long> {
}
