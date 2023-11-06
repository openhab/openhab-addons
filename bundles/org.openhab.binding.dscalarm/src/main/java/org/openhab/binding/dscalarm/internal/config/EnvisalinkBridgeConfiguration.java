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
 * Configuration class for the EyezOn Envisalink 3/2DS Ethernet TCP interface bridge, used to connect to the DSC Alarm
 * system.
 *
 * @author Russell Stephens - Initial contribution
 */

public class EnvisalinkBridgeConfiguration {

    // Envisalink Bridge Thing constants
    public static final String IP_ADDRESS = "ipAddress";
    public static final String PORT = "port";
    public static final String PASSWORD = "password";
    public static final String CONNECTION_TIMEOUT = "connectionTimeout";
    public static final String POLL_PERIOD = "pollPeriod";

    /**
     * The IP address of the Envisalink Ethernet 3/2DS TCP interface
     */
    public String ipAddress;

    /**
     * The port number of the Envisalink Ethernet 3/2DS TCP interface
     */
    public Integer port;

    /**
     * The password of the Envisalink Ethernet 3/2DS TCP interface
     */
    public String password;

    /**
     * The Socket connection timeout for the Envisalink Ethernet 3/2DS TCP interface
     */
    public Integer connectionTimeout;

    /**
     * The Panel Poll Period. Can be set in range 1-15 minutes. Default is 1 minute;
     */
    public Integer pollPeriod;
}
