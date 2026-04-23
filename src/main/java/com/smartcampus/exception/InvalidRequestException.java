package com.smartcampus.exception;

/** Signals a malformed client request that should return HTTP 400. */
public class InvalidRequestException extends RuntimeException {
    public InvalidRequestException(String message) {
        super(message);
    }
}
