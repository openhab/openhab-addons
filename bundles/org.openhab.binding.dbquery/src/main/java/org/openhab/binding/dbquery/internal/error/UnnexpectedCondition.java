package org.openhab.binding.dbquery.internal.error;

/**
 * Represents an unnexpected condtion aka bug
 */
public class UnnexpectedCondition extends RuntimeException {
    public UnnexpectedCondition(String message) {
        super(message);
    }
}
