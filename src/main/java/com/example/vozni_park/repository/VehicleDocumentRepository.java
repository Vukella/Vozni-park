package com.example.vozni_park.repository;

import com.example.vozni_park.entity.VehicleDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VehicleDocumentRepository extends JpaRepository<VehicleDocument, Long> {

    List<VehicleDocument> findByVehicleIdOrderByUploadDateDesc(Long vehicleId);

    List<VehicleDocument> findByVehicleIdAndDocumentTypeOrderByUploadDateDesc(
            Long vehicleId, String documentType);
}
