package com.example.vozni_park.enums;

/**
 * Driver operational status.
 *
 * Priority order (highest first):
 *
 * Priority 1 (highest) — SUSPENDED:           license_revoked flag is true (disciplinary/legal).
 *                                              Source of truth: license_revocation table
 *                                              where restoration_date IS NULL.
 * Priority 2           — INVALID_DOCUMENTS:   Any driver document has expired
 *                                              (medical certificate, driver permit,
 *                                              driving license, tachograph card).
 * Priority 3           — ON_DUTY:             Has an ACTIVE travel order.
 * Priority 4 (default) — AVAILABLE:           None of the above conditions are met.
 *
 * SUSPENDED and INVALID_DOCUMENTS are intentionally distinct statuses —
 * they represent different real-world situations with different resolution paths:
 *   SUSPENDED       → requires administrative/legal action to restore
 *   INVALID_DOCUMENTS → requires renewal of expired document(s)
 *
 * Both prevent the driver from being assigned to a new travel order.
 */
public enum DriverStatus {

    AVAILABLE(1, "AVAILABLE"),
    ON_DUTY(2, "ON_DUTY"),
    INVALID_DOCUMENTS(3, "INVALID_DOCUMENTS"),
    SUSPENDED(4, "SUSPENDED");

    private final int code;
    private final String label;

    DriverStatus(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public int getCode() {
        return code;
    }

    public String getLabel() {
        return label;
    }

    public boolean preventsAssignment() {
        return this == SUSPENDED || this == INVALID_DOCUMENTS;
    }

    public static DriverStatus fromCode(int code) {
        for (DriverStatus s : values()) {
            if (s.code == code) return s;
        }
        throw new IllegalArgumentException("Unknown driver status code: " + code);
    }
}
