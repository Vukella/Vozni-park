package com.example.vozni_park.repository;

import com.example.vozni_park.entity.NisCard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface NisCardRepository extends JpaRepository<NisCard, Long> {

    Optional<NisCard> findByCardNumber(String cardNumber);

    boolean existsByCardNumber(String cardNumber);
}
