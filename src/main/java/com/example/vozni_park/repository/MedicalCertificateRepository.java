package com.example.vozni_park.repository;

import com.example.vozni_park.entity.MedicalCertificate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MedicalCertificateRepository extends JpaRepository<MedicalCertificate, Long> {
}
