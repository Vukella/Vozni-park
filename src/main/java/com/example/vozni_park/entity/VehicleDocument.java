package com.example.vozni_park.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * Metadata record for a file uploaded as vehicle documentation.
 * The actual file binary is stored in MinIO (local dev) or Azure Blob Storage (production).
 * Only the storage path is kept here.
 *
 * document_type values: TRAFFIC_LICENSE, CERTIFICATE, EXPERT_FINDING
 */
@Entity
@Table(name = "vehicle_document")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class VehicleDocument {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_vehicle_document")
    private Long idVehicleDocument;

    @Column(name = "vehicle_id", insertable = false, updatable = false)
    private Long vehicleId;

    @Column(name = "document_type", length = 30, nullable = false)
    private String documentType;

    @Column(name = "file_name", length = 255, nullable = false)
    private String fileName;

    @Column(name = "content_type", length = 100)
    private String contentType;

    @Column(name = "file_size")
    private Long fileSize;

    /**
     * Full path in the object store (MinIO bucket key or Azure Blob path).
     * Example: "vehicle-documents/42/TRAFFIC_LICENSE/2026-04-20_saobracajna.pdf"
     */
    @Column(name = "storage_path", length = 500, nullable = false)
    private String storagePath;

    @Column(name = "upload_date")
    private LocalDate uploadDate;

    @Column(name = "uploaded_by_user_id", insertable = false, updatable = false)
    private Long uploadedByUserId;

    @Column(name = "notes", length = 300)
    private String notes;

    // Relationships

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vehicle_id")
    private Vehicle vehicle;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "uploaded_by_user_id")
    private AppUser uploadedByUser;
}
