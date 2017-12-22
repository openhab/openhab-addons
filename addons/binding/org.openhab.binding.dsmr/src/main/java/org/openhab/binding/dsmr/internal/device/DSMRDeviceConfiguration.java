/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.dsmr.internal.device;

import java.util.Objects;

/**
 * Class described the DSMRDeviceConfiguration.
 *
 * This class is supporting the Configuration.as functionality from {@link Configuration}
 *
 * @author M. Volaart - Initial contribution
 */
public class DSMRDeviceConfiguration {
    /**
     * Portname
     */
    public String serialPort;

    /**
     * Serial port baud rate
     */
    public Integer serialPortBaudrate;

    /**
     * Serial port data bits
     */
    public Integer serialPortDatabits;

    /**
     * Serial port parity
     */
    public String serialPortParity;

    /**
     * Serial port stop bits
     */
    public String serialPortStopbits;

    /**
     * Serial port auto detection flag
     */
    public Boolean serialPortDisableAutoDetection;

    /**
     * The DSMR Device can work in a lenient mode.
     * This means the binding is less strict during communication errors and will ignore the CRC-check
     * Data that is available will be communicated to the OpenHAB2 system and recoverable communication errors
     * won't be logged.
     * This can be needed for devices handling the serial port not fast enough (e.g. embedded devices)
     */
    public Boolean lenientMode;

    @Override
    public String toString() {
        return "DSMRDeviceConfiguration(portName:" + serialPort + ", baudrate:" + serialPortBaudrate + ", data bits:"
                + serialPortDatabits + ", parity:" + serialPortParity + ", stop bits:" + serialPortStopbits
                + ", auto detection disabled:" + serialPortDisableAutoDetection + ", lenientMode:" + lenientMode + ")";
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

        return serialPort.equals(o.serialPort) && serialPortBaudrate.equals(o.serialPortBaudrate)
                && serialPortDatabits.equals(o.serialPortDatabits) && serialPortParity.equals(o.serialPortParity)
                && serialPortStopbits.equals(o.serialPortStopbits) && lenientMode == o.lenientMode
                && serialPortDisableAutoDetection == o.serialPortDisableAutoDetection;
    }

    @Override
    public int hashCode() {
        return Objects.hash(serialPort, serialPortBaudrate, serialPortDatabits, serialPortParity, serialPortStopbits,
                lenientMode, serialPortDisableAutoDetection);
    }
}
