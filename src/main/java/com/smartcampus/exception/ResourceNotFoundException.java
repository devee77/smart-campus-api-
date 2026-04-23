package com.smartcampus.exception;

/** Thrown when a path parameter targets a resource that does not exist. Maps to HTTP 404. */
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) { super(message); }
}
