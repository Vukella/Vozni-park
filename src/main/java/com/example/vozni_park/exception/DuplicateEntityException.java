package com.example.vozni_park.exception;

public class DuplicateEntityException extends RuntimeException {

    private final String entityType;
    private final String field;
    private final Object value;

    public DuplicateEntityException(String entityType, String field, Object value) {
        super(entityType + " already exists with " + field + ": " + value);
        this.entityType = entityType;
        this.field = field;
        this.value = value;
    }

    public String getEntityType() {
        return entityType;
    }

    public String getField() {
        return field;
    }

    public Object getValue() {
        return value;
    }
}
