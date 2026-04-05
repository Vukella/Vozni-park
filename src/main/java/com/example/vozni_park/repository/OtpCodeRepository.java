package com.example.vozni_park.repository;

import com.example.vozni_park.entity.OtpCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface OtpCodeRepository extends JpaRepository<OtpCode, Long> {

    @Query("SELECT o FROM OtpCode o WHERE o.email = :email AND o.code = :code AND o.used = 0 AND o.expiresAt > :now")
    Optional<OtpCode> findValidOtp(@Param("email") String email,
                                   @Param("code") String code,
                                   @Param("now") LocalDateTime now);

    @Modifying
    @Query("DELETE FROM OtpCode o WHERE o.email = :email")
    void deleteAllByEmail(@Param("email") String email);

    @Modifying
    @Query("DELETE FROM OtpCode o WHERE o.expiresAt < :now")
    void deleteExpired(@Param("now") LocalDateTime now);
}