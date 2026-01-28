package com.example.vozni_park.repository;

import com.example.vozni_park.entity.TravelOrderCounter;
import com.example.vozni_park.entity.embeddable.TravelOrderCounterId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import jakarta.persistence.LockModeType;
import java.util.Optional;

@Repository
public interface TravelOrderCounterRepository extends JpaRepository<TravelOrderCounter, TravelOrderCounterId> {
    
    // Custom query methods
    Optional<TravelOrderCounter> findByIdLocationIdAndIdYear(Long locationId, Integer year);
    
    // Pessimistic lock for counter increment (prevents race conditions)
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT toc FROM TravelOrderCounter toc WHERE toc.id.locationId = :locationId AND toc.id.year = :year")
    Optional<TravelOrderCounter> findByLocationAndYearForUpdate(Long locationId, Integer year);
}
