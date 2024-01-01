/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.dsmr.internal.device;

import org.openhab.binding.dsmr.internal.DSMRBindingConstants;

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
     * The Luxembourgish/Austria smart meter decryption key
     */
    public String decryptionKey;

    /**
     * Austria smart meter additional decryption key
     */
    public String additionalKey = DSMRBindingConstants.CONFIGURATION_ADDITIONAL_KEY_DEFAULT;

    /**
     * When no message was received after the configured number of seconds action will be taken.
     */
    public int receivedTimeout;

    /**
     * @return true if serial port settings are all set.
     */
    public boolean isSerialFixedSettings() {
        return baudrate > 0 && databits > 0 && !(parity != null && parity.isBlank())
                && !(stopbits != null && stopbits.isBlank());
    }

    @Override
    public String toString() {
        return "DSMRDeviceConfiguration [serialPort=" + serialPort + ", Baudrate=" + baudrate + ", Databits=" + databits
                + ", Parity=" + parity + ", Stopbits=" + stopbits + ", decryptionKey=" + decryptionKey
                + ", additionalKey=" + additionalKey + ", receivedTimeout=" + receivedTimeout + "]";
    }
}
