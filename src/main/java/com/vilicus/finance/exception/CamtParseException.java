package com.vilicus.finance.exception;

/**
 * CamtParseException — Thrown when CAMT.052 XML parsing fails.
 *
 * Reasons:
 * - Invalid XML structure
 * - Missing required fields
 * - Unsupported CAMT version
 * - Encoding issues
 * - Malformed amounts or dates
 */
public class CamtParseException extends RuntimeException {

    public CamtParseException(String message) {
        super(message);
    }

    public CamtParseException(String message, Throwable cause) {
        super(message, cause);
    }
}
