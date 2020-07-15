package org.openhab.binding.boschshc.internal.exceptions;

@SuppressWarnings("serial")
public class BoschSHCException extends Exception {
    public BoschSHCException() {
    }

    public BoschSHCException(String message) {
        super(message);
    }
}
