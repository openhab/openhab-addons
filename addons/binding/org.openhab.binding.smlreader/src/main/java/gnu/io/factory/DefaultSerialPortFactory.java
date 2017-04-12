package gnu.io.factory;

import gnu.io.NoSuchPortException;
import gnu.io.PortInUseException;
import gnu.io.SerialPort;
import gnu.io.UnsupportedCommOperationException;

public class DefaultSerialPortFactory implements SerialPortFactory {

    private SerialPortRegistry portRegistry;

    /**
     * Constructor
     */
    public DefaultSerialPortFactory() {
        this.portRegistry = new SerialPortRegistry();
    }

    /*
     * (non-Javadoc)
     * 
     * @see gnu.io.factory.SerialPortFactory#createSerialPort(java.lang.String)
     */
    @Override
    public <T extends SerialPort> T createSerialPort(String portName, Class<T> expectedClass)
            throws PortInUseException, NoSuchPortException, UnsupportedCommOperationException {
        SerialPortCreator<T> portCreator = this.portRegistry.getPortCreatorForPortName(portName, expectedClass);
        if (portCreator != null) {
            return portCreator.createPort(portName);
        }
        throw new NoSuchPortException(/* portName + " can not be opened." */);
    }

    @Override
    public SerialPort createSerialPort(String portName)
            throws PortInUseException, NoSuchPortException, UnsupportedCommOperationException {
        SerialPortCreator<SerialPort> portCreator = this.portRegistry.getPortCreatorForPortName(portName,
                SerialPort.class);
        if (portCreator != null) {
            return portCreator.createPort(portName);
        }
        throw new NoSuchPortException(/* portName + " can not be opened." */);
    }

    /**
     * Gets the {@link SerialPortRegistry} to register/unregister {@link SerialPortCreator}s.
     * 
     * @return
     */
    public SerialPortRegistry getPortRegistry() {
        return portRegistry;
    }
}
