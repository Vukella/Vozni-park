package com.example.vozni_park.enums;

/**
 * Travel order lifecycle status.
 *
 * Transitions:
 *
 *   ACTIVE (1)    — created, date_to is in the future, no ending mileage entered
 *       ↓ (date_to passes with no ending mileage — daily refresh job)
 *   UNFINISHED (2) — date_to has passed and no ending mileage entered
 *       ↓ (ending mileage entered at any point)
 *   COMPLETED (3)  — ending mileage entered; terminal state
 *
 *   ACTIVE (1)    → CANCELLED (0) — manual cancellation; only allowed from ACTIVE
 *
 * Rules:
 *   - COMPLETED and CANCELLED are terminal states — no further transitions.
 *   - UNFINISHED → COMPLETED is allowed (late closure).
 *   - Vehicles and drivers assigned to a CANCELLED order have their
 *     status recalculated from scratch (not just set to AVAILABLE).
 */
public enum TravelOrderStatus {

    CANCELLED(0, "CANCELLED"),
    ACTIVE(1, "ACTIVE"),
    UNFINISHED(2, "UNFINISHED"),
    COMPLETED(3, "COMPLETED");

    private final int code;
    private final String label;

    TravelOrderStatus(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public int getCode() {
        return code;
    }

    public String getLabel() {
        return label;
    }

    public boolean isTerminal() {
        return this == COMPLETED || this == CANCELLED;
    }

    public boolean allowsAssignment() {
        // Travel orders that can still have vehicles/drivers added or removed
        return this == ACTIVE;
    }

    public static TravelOrderStatus fromCode(int code) {
        for (TravelOrderStatus s : values()) {
            if (s.code == code) return s;
        }
        throw new IllegalArgumentException("Unknown travel order status code: " + code);
    }
}
