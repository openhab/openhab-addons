/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.serial.internal;

/**
 * Class describing the serial bridge user configuration
 *
 * @author Mike Major - Initial contribution
 */
public class SerialBridgeConfiguration {
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

    @Override
    public String toString() {
        return "SerialBridgeConfiguration [serialPort=" + serialPort + ", Baudrate=" + baudrate + ", Databits="
                + databits + ", Parity=" + parity + ", Stopbits=" + stopbits + "]";
    }
}
