package com.example.vozni_park.repository;

import com.example.vozni_park.entity.RegistrationChangeLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RegistrationChangeLogRepository extends JpaRepository<RegistrationChangeLog, Long> {

    List<RegistrationChangeLog> findByVehicleIdOrderByChangeDateDesc(Long vehicleId);
}
