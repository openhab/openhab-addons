package org.openhab.binding.boschshc.internal.exceptions;


/** 
 * Thrown if the pairing failed multiple times
 * 
 * @author Gerd Zanker
 */
@SuppressWarnings("serial")
public class PairingFailedException extends BoschSHCException {
    public PairingFailedException() {
    }

    public PairingFailedException(String message) {
        super(message);
    }
}
