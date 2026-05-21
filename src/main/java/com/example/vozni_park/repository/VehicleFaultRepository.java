package com.example.vozni_park.repository;

import com.example.vozni_park.entity.VehicleFault;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VehicleFaultRepository extends JpaRepository<VehicleFault, Long> {

    /**
     * Returns true if the vehicle has at least one active, unresolved fault
     * where vehicle_inoperable = 1 and resolution_date IS NULL.
     * This is the exact condition that sets vehicle status to IN_FAULT (code 5).
     */
    @Query("SELECT COUNT(f) > 0 FROM VehicleFault f " +
           "WHERE f.vehicleId = :vehicleId " +
           "AND f.vehicleInoperable = 1 " +
           "AND f.resolutionDate IS NULL")
    boolean hasActiveInoperableFault(@Param("vehicleId") Long vehicleId);

    /**
     * Fetch all active (unresolved, inoperable) faults for a vehicle.
     */
    @Query("SELECT f FROM VehicleFault f " +
           "WHERE f.vehicleId = :vehicleId " +
           "AND f.vehicleInoperable = 1 " +
           "AND f.resolutionDate IS NULL")
    List<VehicleFault> findActiveInoperableFaults(@Param("vehicleId") Long vehicleId);

    List<VehicleFault> findByVehicleIdOrderByReportDateDesc(Long vehicleId);
}
