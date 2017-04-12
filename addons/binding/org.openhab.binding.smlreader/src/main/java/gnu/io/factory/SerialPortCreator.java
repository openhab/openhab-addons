package gnu.io.factory;

import gnu.io.NoSuchPortException;
import gnu.io.PortInUseException;
import gnu.io.SerialPort;
import gnu.io.UnsupportedCommOperationException;

public interface SerialPortCreator<T extends SerialPort> {

    final static String LOCAL = "local";

    /**
     * Gets whether this {@link SerialPortCreator} is applicable to create and open the given port.
     * 
     * @param portName The ports name.
     * @return Whether the port can be created and opened by this creator.
     */
    public boolean isApplicable(String portName, Class<T> epectedClass);

    /**
     * Creates the {@link SerialPort} and opens it for communication.
     * 
     * @param portName The ports name.
     * @return The created {@link SerialPort}.
     * @throws NoSuchPortException If the serial port does not exist.
     * @throws UnsupportedCommOperationException
     * @throws PortInUseException
     */
    public T createPort(String portName)
            throws NoSuchPortException, UnsupportedCommOperationException, PortInUseException;

    /**
     * Gets the protocol type of the Port to create.
     * 
     * @return The protocol type.
     */
    public String getProtocol();
}
