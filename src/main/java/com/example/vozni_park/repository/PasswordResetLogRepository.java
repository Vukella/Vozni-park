package com.example.vozni_park.repository;

import com.example.vozni_park.entity.PasswordResetLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PasswordResetLogRepository extends JpaRepository<PasswordResetLog, Long> {

    List<PasswordResetLog> findByUserIdOrderByResetTimeDesc(Long userId);
}
