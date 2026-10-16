package com.example.vozni_park.exception;

public class InvalidStatusTransitionException extends RuntimeException {

    private final String entityType;
    private final String currentStatus;
    private final String attemptedOperation;

    public InvalidStatusTransitionException(String entityType, String currentStatus, String attemptedOperation) {
        super(entityType + " with status '" + currentStatus + "' cannot perform operation: " + attemptedOperation);
        this.entityType = entityType;
        this.currentStatus = currentStatus;
        this.attemptedOperation = attemptedOperation;
    }

    public String getEntityType() {
        return entityType;
    }

    public String getCurrentStatus() {
        return currentStatus;
    }

    public String getAttemptedOperation() {
        return attemptedOperation;
    }
}
