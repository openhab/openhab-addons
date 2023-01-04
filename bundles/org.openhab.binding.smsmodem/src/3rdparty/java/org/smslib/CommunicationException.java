package org.smslib;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Wrapper for communication exception
 */
@NonNullByDefault
public class CommunicationException extends Exception {

    private static final long serialVersionUID = -5175636461754717860L;

    public CommunicationException(String message, Exception cause) {
        super(message, cause);
    }

    public CommunicationException(String message) {
        super(message);
    }
}
