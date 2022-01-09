/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.serial.internal.handler;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Class describing the serial bridge user configuration
 *
 * @author Mike Major - Initial contribution
 */
@NonNullByDefault
public class SerialBridgeConfiguration {
    /**
     * Serial port name
     */
    public @Nullable String serialPort;

    /**
     * Serial port baud rate
     */
    public int baudRate = 9600;

    /**
     * Serial port data bits
     */
    public int dataBits = 8;

    /**
     * Serial port parity
     */
    public String parity = "N";

    /**
     * Serial port stop bits
     */
    public String stopBits = "1";

    /**
     * Charset
     */
    public @Nullable String charset;

    @Override
    public String toString() {
        return "SerialBridgeConfiguration [serialPort=" + serialPort + ", Baudrate=" + baudRate + ", Databits="
                + dataBits + ", Parity=" + parity + ", Stopbits=" + stopBits + ", charset=" + charset + "]";
    }
}
