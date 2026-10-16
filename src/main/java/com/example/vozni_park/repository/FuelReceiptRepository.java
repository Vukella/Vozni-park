package com.example.vozni_park.repository;

import com.example.vozni_park.entity.FuelReceipt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FuelReceiptRepository extends JpaRepository<FuelReceipt, Long> {

    Optional<FuelReceipt> findByReceiptNumber(String receiptNumber);

    boolean existsByReceiptNumber(String receiptNumber);
}
