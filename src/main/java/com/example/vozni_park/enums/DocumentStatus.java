package com.example.vozni_park.enums;

/**
 * Status for all expiry-tracked documents.
 *
 * Used by: Registration, CascoInsurance, TachographDevice, FireExtinguisher,
 *          FirstAidKit, MedicalCertificate, DriverPermit, DriversLicense,
 *          TachographCard.
 *
 * The status calculation service evaluates expiry using a live date comparison
 * (LocalDate.now().isAfter(dateTo)) — never relying on the stored status_code
 * alone for decisions that affect vehicle or driver status. The stored value
 * is for display and for the daily refresh to update in bulk.
 */
public enum DocumentStatus {

    EXPIRED(0, "EXPIRED"),
    VALID(1, "VALID");

    private final int code;
    private final String label;

    DocumentStatus(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public int getCode() {
        return code;
    }

    public String getLabel() {
        return label;
    }

    public static DocumentStatus fromCode(int code) {
        for (DocumentStatus s : values()) {
            if (s.code == code) return s;
        }
        throw new IllegalArgumentException("Unknown document status code: " + code);
    }
}
