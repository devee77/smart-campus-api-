package com.smartcampus.exception;

/** Thrown when a request conflicts with an existing resource. Maps to HTTP 409. */
public class ResourceConflictException extends RuntimeException {
    public ResourceConflictException(String message) { super(message); }
}
