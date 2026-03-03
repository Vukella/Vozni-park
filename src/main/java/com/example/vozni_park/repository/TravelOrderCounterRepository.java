package com.example.vozni_park.repository;

import com.example.vozni_park.entity.TravelOrderCounter;
import com.example.vozni_park.entity.embeddable.TravelOrderCounterId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jakarta.persistence.LockModeType;
import java.util.Optional;

@Repository
public interface TravelOrderCounterRepository extends JpaRepository<TravelOrderCounter, TravelOrderCounterId> {
    
    // Custom query methods
    Optional<TravelOrderCounter> findByIdLocationIdAndIdYear(Long locationId, Integer year);
    
    // Pessimistic lock for counter increment (prevents race conditions)
    @Query(
            value = """
      SELECT *
      FROM travel_order_counter
      WHERE location_id = :locationId AND year = :year
      FOR UPDATE
      """,
            nativeQuery = true
    )
    Optional<TravelOrderCounter> findByLocationAndYearForUpdate(
            @Param("locationId") Long locationId,
            @Param("year") Integer year
    );
}
