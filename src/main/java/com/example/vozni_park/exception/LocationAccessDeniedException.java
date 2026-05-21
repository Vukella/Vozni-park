package com.example.vozni_park.exception;

public class LocationAccessDeniedException extends RuntimeException {

    public LocationAccessDeniedException(Long locationId) {
        super("Access denied to location with ID: " + locationId);
    }

    public LocationAccessDeniedException(String message) {
        super(message);
    }
}
