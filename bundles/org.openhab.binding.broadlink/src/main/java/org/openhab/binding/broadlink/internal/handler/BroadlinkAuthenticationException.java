package org.openhab.binding.broadlink.internal.handler;

public class BroadlinkAuthenticationException extends Exception {

    public BroadlinkAuthenticationException(String message) {
        super(message);
    }

    public BroadlinkAuthenticationException(String message, Exception e) {
        super(message, e);
    }

}
