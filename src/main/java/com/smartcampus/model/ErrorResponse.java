package com.smartcampus.model;

import java.time.Instant;

/**
 * Standard JSON error envelope returned by all exception mappers.
 */
public class ErrorResponse {
    private int    statusCode;
    private String errorType;
    private String detail;
    private String timestamp;

    public ErrorResponse() {}

    public ErrorResponse(int statusCode, String errorType, String detail) {
        this.statusCode = statusCode;
        this.errorType  = errorType;
        this.detail     = detail;
        this.timestamp  = Instant.now().toString();
    }

    public int    getStatusCode()              { return statusCode; }
    public void   setStatusCode(int s)         { this.statusCode = s; }

    public String getErrorType()               { return errorType; }
    public void   setErrorType(String e)       { this.errorType = e; }

    public String getDetail()                  { return detail; }
    public void   setDetail(String d)          { this.detail = d; }

    public String getTimestamp()               { return timestamp; }
    public void   setTimestamp(String ts)      { this.timestamp = ts; }
}
