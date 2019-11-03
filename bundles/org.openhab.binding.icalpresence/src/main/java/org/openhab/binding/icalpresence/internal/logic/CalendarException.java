package org.openhab.binding.icalpresence.internal.logic;

/**
 * Exception Class to encapsulate Exception data for binding.
 *
 * @author Michael Wodniok - Initial contribution
 */
public class CalendarException extends Exception {

    private static final long serialVersionUID = -2071400154241449096L;

    public CalendarException(String message) {
        super(message);
    }

    public CalendarException(String message, Exception source) {
        super(message, source);
    }

    public CalendarException(Exception source) {
        super("Internal exception occured", source);
    }

}
