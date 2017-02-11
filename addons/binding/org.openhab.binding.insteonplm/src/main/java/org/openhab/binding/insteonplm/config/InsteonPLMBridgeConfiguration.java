package org.openhab.binding.insteonplm.config;

public class InsteonPLMBridgeConfiguration {
    /** The serial port to communicate over. */
    public String serialPort;
    public String host;
    public Integer port;
    public String user;
    public String password;

    public Integer pollTime;

    public enum PortType {
        Hub,
        SerialPort,
        Tcp
    }

    public PortType portType;
}
