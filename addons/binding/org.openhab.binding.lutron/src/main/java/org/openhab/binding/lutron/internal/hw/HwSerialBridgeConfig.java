package org.openhab.binding.lutron.internal.hw;

/**
 * Configuration settings for an {@link org.openhab.binding.lutron.handler.HWSerialBridgeHandler}.
 *
 * @author Andrew Shilliday - Initial contribution
 */
public class HwSerialBridgeConfig {
    public static final String SERIAL_PORT = "serialPort";
    public static final String BAUD = "baudRate";

    public String serialPort;
    public Integer baudRate;
}