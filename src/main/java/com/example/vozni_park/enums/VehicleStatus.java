package com.example.vozni_park.enums;

/**
 * Vehicle operational status.
 *
 * Priority order (highest first) determines which status wins when multiple
 * conditions are true simultaneously. The status calculation service always
 * evaluates conditions in this priority order and returns the first match.
 *
 * Priority 1 (highest) — IN_FAULT:      Active unresolved fault with vehicle_inoperable=true
 * Priority 2           — INVALID:        Any linked document has expired
 * Priority 3           — UNAVAILABLE:    Has an UNFINISHED (overdue) travel order
 * Priority 4           — ON_DUTY:        Has an ACTIVE travel order
 * Priority 5 (default) — AVAILABLE:      None of the above conditions are met
 *
 * DECOMMISSIONED is not a status code — it is a boolean flag (decommissioned=true)
 * that removes the vehicle from all operational queries entirely.
 */
public enum VehicleStatus {

    AVAILABLE(1, "AVAILABLE"),
    ON_DUTY(2, "ON_DUTY"),
    UNAVAILABLE(3, "UNAVAILABLE"),
    INVALID(4, "INVALID"),
    IN_FAULT(5, "IN_FAULT");

    private final int code;
    private final String label;

    VehicleStatus(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public int getCode() {
        return code;
    }

    public String getLabel() {
        return label;
    }

    public static VehicleStatus fromCode(int code) {
        for (VehicleStatus s : values()) {
            if (s.code == code) return s;
        }
        throw new IllegalArgumentException("Unknown vehicle status code: " + code);
    }
}
