package com.example.vozni_park.repository;

import com.example.vozni_park.entity.TireMarking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TireMarkingRepository extends JpaRepository<TireMarking, Long> {
}
