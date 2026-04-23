package com.smartcampus.exception;

/** Thrown when a POST body references a roomId that does not exist. Maps to HTTP 422. */
public class LinkedResourceNotFoundException extends RuntimeException {
    public LinkedResourceNotFoundException(String message) { super(message); }
}
