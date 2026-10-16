package com.example.vozni_park.enums;

/** Priority: SUSPENDED > INVALID_DOCUMENTS > ON_DUTY > AVAILABLE. */
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
