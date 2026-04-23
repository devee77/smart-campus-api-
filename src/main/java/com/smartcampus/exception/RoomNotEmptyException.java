package com.smartcampus.exception;

/** Thrown when DELETE /rooms/{id} is attempted while sensors are still assigned. Maps to HTTP 409. */
public class RoomNotEmptyException extends RuntimeException {
    public RoomNotEmptyException(String message) { super(message); }
}
