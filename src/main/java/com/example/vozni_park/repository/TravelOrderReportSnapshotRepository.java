package com.example.vozni_park.repository;

import com.example.vozni_park.entity.TravelOrderReportSnapshot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TravelOrderReportSnapshotRepository extends JpaRepository<TravelOrderReportSnapshot, Long> {

    Optional<TravelOrderReportSnapshot> findByTravelOrderId(Long travelOrderId);

    boolean existsByTravelOrderId(Long travelOrderId);
}
