package com.example.vozni_park.repository;

import com.example.vozni_park.entity.TachographCard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TachographCardRepository extends JpaRepository<TachographCard, Long> {
}
