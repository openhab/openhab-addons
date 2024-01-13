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
package org.openhab.binding.dscalarm.internal.config;

/**
 * Configuration class for the TCP Server bridge, used to connect to the DSC Alarm system.
 *
 * @author Russell Stephens - Initial contribution
 */

public class TCPServerBridgeConfiguration {

    // TCP Server Bridge Thing constants
    public static final String IP_ADDRESS = "ipAddress";
    public static final String PORT = "port";
    public static final String PASSWORD = "password";
    public static final String CONNECTION_TIMEOUT = "connectionTimeout";
    public static final String POLL_PERIOD = "pollPeriod";
    public static final String PROTOCOL = "protocol";

    /**
     * The IP address of the TCP Server
     */
    public String ipAddress;

    /**
     * The port number of the TCP Server
     */
    public Integer port;

    /**
     * The Socket connection timeout for the TCP Server
     */
    public Integer connectionTimeout;

    /**
     * The Panel Poll Period. Can be set in range 1-15 minutes. Default is 1 minute;
     */
    public Integer pollPeriod;

    /**
     * The Protocol Type - 1 for IT-100 API or 2 for Envisalink TPI.
     */
    public Integer protocol;
}
