package org.openhab.binding.robonect;

public class RobonectCommunicationException extends RuntimeException {

    public RobonectCommunicationException(String message) {
        super(message);
    }

    public RobonectCommunicationException(String message, Throwable cause) {
        super(message, cause);
    }
}
