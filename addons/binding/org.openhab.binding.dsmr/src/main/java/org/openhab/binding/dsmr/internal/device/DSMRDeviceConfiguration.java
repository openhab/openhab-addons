/**
 * Copyright (c) 2010-2019 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.dsmr.internal.device;

import org.apache.commons.lang.StringUtils;

/**
 * Class describing the DSMR bridge user configuration
 *
 * @author M. Volaart - Initial contribution
 * @author Hilbrand Bouwkamp - added receivedTimeout configuration
 */
public class DSMRDeviceConfiguration {
    /**
     * Serial port name
     */
    public String serialPort;

    /**
     * Serial port baud rate
     */
    public int baudrate;

    /**
     * Serial port data bits
     */
    public int databits;

    /**
     * Serial port parity
     */
    public String parity;

    /**
     * Serial port stop bits
     */
    public String stopbits;

    /**
     * When no message was received after the configured number of seconds action will be taken.
     */
    public int receivedTimeout;

    /**
     * @return true if serial port settings are all set.
     */
    public boolean isSerialFixedSettings() {
        return baudrate > 0 && databits > 0 && !StringUtils.isBlank(parity) && !StringUtils.isBlank(stopbits);
    }

    @Override
    public String toString() {
        return "DSMRDeviceConfiguration [serialPort=" + serialPort + ", Baudrate=" + baudrate + ", Databits=" + databits
                + ", Parity=" + parity + ", Stopbits=" + stopbits + ", receivedTimeout=" + receivedTimeout + "]";
    }
}
