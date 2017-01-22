package org.openhab.binding.dsmr.device;

/**
 * Class described the DSMRDeviceConfiguration.
 *
 * This class is supporting the Configuration.as functionality from {@link Configuration}
 *
 * @author M. Volaart
 * @since 2.0.0
 */
public class DSMRDeviceConfiguration {
    // Portname
    public String serialPort;

    // Serial port settings (having these will disable autodetect mode)
    public String serialPortSettings;

    /*
     * The DSMR Device can work in a lenient mode.
     * This means the binding is less strict during communication errors and will ignore the CRC-check
     * Data that is available will be communicated to the OpenHAB2 system and recoverable communication errors
     * won't be logged.
     * This can be needed for devices handling the serial port not fast enough (e.g. embedded devices)
     */
    public Boolean lenientMode;

    @Override
    public String toString() {
        return "DSMRDeviceConfiguration(portName:" + serialPort + ", fixedPortSettings:" + serialPortSettings
                + ", lenientMode:" + lenientMode;
    }

    /**
     * Returns if this DSMRDeviceConfiguration is equal to the other DSMRDeviceConfiguration.
     * Evaluation is done based on all the parameters
     *
     * @param other the other DSMRDeviceConfiguration to check
     * @return true if both are equal, false otherwise or if other == null
     */
    @Override
    public boolean equals(Object other) {
        if (other == null || !(other instanceof DSMRDeviceConfiguration)) {
            return false;
        }
        DSMRDeviceConfiguration o = (DSMRDeviceConfiguration) other;

        return serialPort.equals(o.serialPort) && serialPortSettings.equals(o.serialPortSettings)
                && lenientMode == o.lenientMode;
    }
}
