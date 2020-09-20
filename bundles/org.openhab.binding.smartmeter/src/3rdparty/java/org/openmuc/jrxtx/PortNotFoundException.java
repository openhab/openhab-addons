package org.openmuc.jrxtx;

/**
 * Signals that the provided serial port name provided via {@link SerialPortBuilder#newBuilder(String)},
 * {@link SerialPortBuilder#setPortName(String)} doesn't exist on the host system.
 */
public class PortNotFoundException extends SerialPortException {

    private static final long serialVersionUID = 2766015292714524756L;

    /**
     * Constructs a new PortNotFoundException with the specified detail message.
     * 
     * @param message
     *            the detail message.
     */
    public PortNotFoundException(String message) {
        super(message);
    }

}
