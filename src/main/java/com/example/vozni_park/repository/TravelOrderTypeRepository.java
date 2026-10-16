package com.example.vozni_park.repository;

import com.example.vozni_park.entity.TravelOrderType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface TravelOrderTypeRepository extends JpaRepository<TravelOrderType, Long> {
    Optional<TravelOrderType> findByName(String name);
}
