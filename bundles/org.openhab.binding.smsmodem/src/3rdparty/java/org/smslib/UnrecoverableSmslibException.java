package org.smslib;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 *
 * Exception class for internal SMSLib unrecoverable error
 *
 * @author Gwendal ROULLEAU - Initial contribution
 */
@NonNullByDefault
public class UnrecoverableSmslibException extends RuntimeException {

    private static final long serialVersionUID = 7649578885702261759L;

    public UnrecoverableSmslibException(String message) {
        super(message);
    }

    public UnrecoverableSmslibException(String message, Exception cause) {
        super(message, cause);
    }
}
