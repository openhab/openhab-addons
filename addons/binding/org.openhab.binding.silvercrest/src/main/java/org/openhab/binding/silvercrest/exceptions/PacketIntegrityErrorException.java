package org.openhab.binding.silvercrest.exceptions;

/**
 * Exception throwed when some packet has one integrity error.
 *
 * @author Jaime Vaz - Initial contribution
 *
 */
public class PacketIntegrityErrorException extends Exception {

    private static final long serialVersionUID = -8531181654734497851L;

    /**
     * Default constructor.
     *
     * @param message the error message
     */
    public PacketIntegrityErrorException(final String message) {
        super(message);
    }
}
