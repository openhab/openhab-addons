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
package org.openhab.binding.clearone.internal.config;

/**
 * Configuration class for the ClearOne XAP Stack.
 *
 * @author Garry Mitchell - Initial contribution
 */

public class StackConfiguration {

    // Bridge Thing constants
    public static final String SERIAL_PORT = "serialPort";
    public static final String BAUD = "baud";
    public static final String POLL_PERIOD = "pollPeriod";

    /**
     * Port name for a serial connection. Valid values are e.g. COM1 for Windows and /dev/ttyS0 or
     * /dev/ttyUSB0 for Linux.
     */
    public String serialPort;

    /**
     * Baud rate for serial connections. Valid values are 9600, 19200, 38400 (default), 57600, and 115200.
     */
    public Integer baud;

    /**
     * The Stack Poll Period. Can be set in range 1-1800 seconds. Default is 10 seconds;
     */
    public Integer pollPeriod;
}
