package com.example.vozni_park.repository;

import com.example.vozni_park.entity.UserLocation;
import com.example.vozni_park.entity.embeddable.UserLocationId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserLocationRepository extends JpaRepository<UserLocation, UserLocationId> {
    
    // Custom query methods
    List<UserLocation> findByIdUserId(Long userId);
    
    List<UserLocation> findByIdLocationId(Long locationId);
    
    boolean existsByIdUserIdAndIdLocationId(Long userId, Long locationId);
}
