package org.openmuc.jrxtx;

import java.io.IOException;

/**
 * Signals that an I/O exception with the SerialPort occurred.
 * 
 * @see SerialPort
 */
public class SerialPortException extends IOException {

    private static final long serialVersionUID = -4848841747671551647L;

    /**
     * Constructs a new SerialPortException with the specified detail message.
     * 
     * @param message
     *            the detail message.
     */
    public SerialPortException(String message) {
        super(message);
    }

}
