package com.example.vozni_park.service;

import com.example.vozni_park.entity.AppUser;
import com.example.vozni_park.entity.Vehicle;
import com.example.vozni_park.entity.VehicleDocument;
import com.example.vozni_park.exception.BusinessRuleViolationException;
import com.example.vozni_park.exception.EntityNotFoundException;
import com.example.vozni_park.repository.AppUserRepository;
import com.example.vozni_park.repository.VehicleDocumentRepository;
import com.example.vozni_park.repository.VehicleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * Manages metadata records for vehicle documents.
 *
 * This service handles only the database side of document management.
 * Actual file storage (upload to MinIO / Azure Blob, download, deletion)
 * will be added in a separate phase when the storage integration is implemented.
 *
 * Document types: TRAFFIC_LICENSE, CERTIFICATE, EXPERT_FINDING
 *
 * The storage_path field holds the object store key that will be used
 * by the storage service to retrieve the binary. Format:
 *   vehicle-documents/{vehicleId}/{documentType}/{date}_{fileName}
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class VehicleDocumentService {

    private final VehicleDocumentRepository vehicleDocumentRepository;
    private final VehicleRepository vehicleRepository;
    private final AppUserRepository appUserRepository;
    private final LocationFilterService locationFilterService;

    private static final List<String> VALID_DOCUMENT_TYPES =
            List.of("TRAFFIC_LICENSE", "CERTIFICATE", "EXPERT_FINDING");

    public List<VehicleDocument> getDocumentsByVehicle(Long vehicleId) {
        vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new EntityNotFoundException("Vehicle", vehicleId));
        return vehicleDocumentRepository.findByVehicleIdOrderByUploadDateDesc(vehicleId);
    }

    public List<VehicleDocument> getDocumentsByVehicleAndType(Long vehicleId, String documentType) {
        validateDocumentType(documentType);
        vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new EntityNotFoundException("Vehicle", vehicleId));
        return vehicleDocumentRepository
                .findByVehicleIdAndDocumentTypeOrderByUploadDateDesc(vehicleId, documentType);
    }

    public VehicleDocument getDocumentById(Long documentId) {
        return vehicleDocumentRepository.findById(documentId)
                .orElseThrow(() -> new EntityNotFoundException("VehicleDocument", documentId));
    }

    /**
     * Creates a metadata record for an uploaded file.
     *
     * The caller is responsible for uploading the binary to the object store first
     * and passing the resulting storage path here. This keeps the DB transaction
     * short — it does not wait for a file upload to complete.
     *
     * In the MinIO integration phase, a dedicated upload endpoint will:
     *   1. Stream the file to MinIO → get back the storage path
     *   2. Call this method to save the metadata record
     */
    @Transactional
    public VehicleDocument saveDocumentMetadata(Long vehicleId, VehicleDocument document,
                                                String username) {
        Vehicle vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new EntityNotFoundException("Vehicle", vehicleId));

        if (!locationFilterService.isSuperAdmin() && vehicle.getVehicleLocation() != null) {
            locationFilterService.validateLocationAccess(
                    vehicle.getVehicleLocation().getLocationUnit().getIdLocationUnit());
        }

        validateDocumentType(document.getDocumentType());

        if (document.getFileName() == null || document.getFileName().isBlank()) {
            throw new BusinessRuleViolationException("MISSING_FILE_NAME",
                    "Document file name is required");
        }
        if (document.getStoragePath() == null || document.getStoragePath().isBlank()) {
            throw new BusinessRuleViolationException("MISSING_STORAGE_PATH",
                    "Storage path is required — upload the file to the object store first");
        }

        AppUser uploader = appUserRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("AppUser", username));

        document.setVehicle(vehicle);
        document.setUploadedByUser(uploader);
        document.setUploadDate(LocalDate.now());

        VehicleDocument saved = vehicleDocumentRepository.save(document);
        log.info("Document metadata saved for vehicle {} — type: {}, file: {}",
                vehicleId, document.getDocumentType(), document.getFileName());
        return saved;
    }

    /**
     * Deletes a document metadata record.
     *
     * In the MinIO integration phase, this must also delete the binary
     * from the object store using the storage_path.
     */
    @Transactional
    public void deleteDocument(Long documentId) {
        VehicleDocument document = vehicleDocumentRepository.findById(documentId)
                .orElseThrow(() -> new EntityNotFoundException("VehicleDocument", documentId));

        // TODO (MinIO phase): delete binary from object store
        // storageService.delete(document.getStoragePath());

        vehicleDocumentRepository.delete(document);
        log.info("Document metadata deleted: {} (storage path: {})",
                documentId, document.getStoragePath());
    }

    /**
     * Builds the canonical storage path for a new document.
     * Call this before uploading to the object store to get a consistent key.
     * Format: vehicle-documents/{vehicleId}/{documentType}/{date}_{sanitizedFileName}
     */
    public String buildStoragePath(Long vehicleId, String documentType, String fileName) {
        String sanitized = fileName.replaceAll("[^a-zA-Z0-9._-]", "_");
        return String.format("vehicle-documents/%d/%s/%s_%s",
                vehicleId, documentType, LocalDate.now(), sanitized);
    }

    private void validateDocumentType(String documentType) {
        if (!VALID_DOCUMENT_TYPES.contains(documentType)) {
            throw new BusinessRuleViolationException("INVALID_DOCUMENT_TYPE",
                    "Document type must be one of: " + String.join(", ", VALID_DOCUMENT_TYPES));
        }
    }
}
