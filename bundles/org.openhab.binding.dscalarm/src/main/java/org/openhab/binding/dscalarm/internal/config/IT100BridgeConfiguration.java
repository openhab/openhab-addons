/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.dscalarm.internal.config;

/**
 * Configuration class for the DSC IT100 RS232 Serial interface bridge, used to connect to the DSC Alarm system.
 *
 * @author Russell Stephens - Initial contribution
 */

public class IT100BridgeConfiguration {

    // IT-100 Bridge Thing constants
    public static final String SERIAL_PORT = "serialPort";
    public static final String BAUD = "baud";
    public static final String POLL_PERIOD = "pollPeriod";

    /**
     * DSC IT100 port name for a serial connection. Valid values are e.g. COM1 for Windows and /dev/ttyS0 or
     * /dev/ttyUSB0 for Linux.
     */
    public String serialPort;

    /**
     * DSC IT100 baud rate for serial connections. Valid values are 9600 (default), 19200, 38400, 57600, and 115200.
     */
    public Integer baud;

    /**
     * The Panel Poll Period. Can be set in range 1-15 minutes. Default is 1 minute;
     */
    public Integer pollPeriod;
}
