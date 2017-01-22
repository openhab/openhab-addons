package org.openhab.binding.dsmr.device;

import java.util.concurrent.TimeUnit;

/**
 * This class holds all the constants that are applicable for the DSMR Device
 *
 * @author M. Volaart
 * @since 2.0.0
 */
public class DSMRDeviceConstants {
    // Enum describing detailed status for the DSMR Device
    public enum DeviceStateDetail {
        NONE(""),
        PORT_OK("Port opened succesful"),
        PORT_DETECTING_SPEED("Autodetecting port speed"),
        PORT_ERROR("General error while opening port"),
        PORT_READ_OK("Port read successful"),
        PORT_READ_ERROR("Port read error"),
        PORT_READ_CRC_ERROR("CRC checksum failed"),
        PORT_READ_DATA_CORRUPT("Read corrupted data"),
        PORT_IN_USE("Port is already in use"),
        PORT_NOT_COMPATIBLE("Specified port is not compatible"),
        PORT_NOT_OPEN("Port is not opened for reading"),
        PORT_CONFIGURATION_ERROR("Invalid port configuration parameters"),
        PORT_CONFIGURATION_NO_PORT("Configured port does not exists"),
        RECOVER_COMMUNICATION("Recover communication"),
        REINITIALIZE("Reinitialize device and use actual configuration"),
        RUNNING_NORMAL("Device is running normal");

        public final String stateDetails;

        DeviceStateDetail(String stateDetails) {
            this.stateDetails = stateDetails;
        }
    }

    // State definitions
    public enum DeviceState {
        INITIALIZING,
        STARTING,
        ONLINE,
        OFFLINE,
        SHUTDOWN,
        CONFIGURATION_PROBLEM;
    }

    // Serial port read time out (15 seconds)
    public static final int SERIAL_PORT_READ_TIMEOUT = (int) TimeUnit.SECONDS.toMillis(15);
    // Timeout for reopening the serial port (when opening failed)
    public static final int SERIAL_PORT_REOPEN_PERIOD = (int) TimeUnit.SECONDS.toMillis(30);
    // Timeout for detecting the correct serial port settings
    public static final int SERIAL_PORT_AUTO_DETECT_TIMEOUT = (int) TimeUnit.SECONDS.toMillis(30);
    // Timeout for recovery from offline mode
    public static final int OFFLINE_RECOVERY_TIMEOUT = (int) TimeUnit.SECONDS.toMillis(30);
    // Timeout for falling in offline after no data has been read
    public static final int SERIAL_PORT_FAILED_READ_PERIOD = (int) TimeUnit.MINUTES.toMillis(1);
}
