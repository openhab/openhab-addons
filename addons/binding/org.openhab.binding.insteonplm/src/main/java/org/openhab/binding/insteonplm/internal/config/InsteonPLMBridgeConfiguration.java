package org.openhab.binding.insteonplm.internal.config;

public class InsteonPLMBridgeConfiguration {
    /** The serial port to communicate over. */
    private String serialPort;
    private String host;
    private Integer port;
    private String user;
    private String password;

    private Integer pollTime;

    public enum PortType {
        Hub,
        SerialPort,
        Tcp
    }

    private String portType;
    private PortType portTypeConvert;

    public String getSerialPort() {
        return serialPort;
    }

    public String getHost() {
        return host;
    }

    public Integer getPort() {
        return port;
    }

    public String getUser() {
        return user;
    }

    public String getPassword() {
        return password;
    }

    public Integer getPollTime() {
        return pollTime;
    }

    public PortType getPortType() {
        if (portTypeConvert == null && portType != null) {
            portTypeConvert = PortType.valueOf(portType);
        }
        return portTypeConvert;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("InsteonPLMBridgeConfiguration [");
        if (serialPort != null) {
            builder.append("serialPort=");
            builder.append(serialPort);
            builder.append(", ");
        }
        if (host != null) {
            builder.append("host=");
            builder.append(host);
            builder.append(", ");
        }
        if (port != null) {
            builder.append("port=");
            builder.append(port);
            builder.append(", ");
        }
        if (user != null) {
            builder.append("user=");
            builder.append(user);
            builder.append(", ");
        }
        if (password != null) {
            builder.append("password=");
            builder.append(password);
            builder.append(", ");
        }
        if (pollTime != null) {
            builder.append("pollTime=");
            builder.append(pollTime);
            builder.append(", ");
        }
        if (portType != null) {
            builder.append("portType=");
            builder.append(portType);
        }
        builder.append("]");
        return builder.toString();
    }
}
