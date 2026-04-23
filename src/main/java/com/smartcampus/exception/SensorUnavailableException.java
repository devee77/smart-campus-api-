package com.smartcampus.exception;

/** Thrown when a POST reading targets a sensor in MAINTENANCE status. Maps to HTTP 403. */
public class SensorUnavailableException extends RuntimeException {
    public SensorUnavailableException(String message) { super(message); }
}
