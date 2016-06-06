package org.openhab.binding.silvercrestwifisocket.exceptions;

/**
 * Exception throwed when one Mac address is not valid.
 *
 * @author Jaime Vaz - Initial contribution
 *
 */
public class MacAddressNotValidException extends Exception {

    private static final long serialVersionUID = 6131138252323778017L;
    private final String macAddress;

    /**
     * Default constructor.
     *
     * @param message the error message
     * @param macAddress the wrong mac address.
     */
    public MacAddressNotValidException(final String message, final String macAddress) {
        super(message);
        this.macAddress = macAddress;
    }

    // SETTERS AND GETTERS

    public String getMacAddress() {
        return this.macAddress;
    }

}
