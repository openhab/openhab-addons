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
package org.openhab.binding.max.internal.config;

import org.openhab.binding.max.internal.MaxBindingConstants;

/**
 * Configuration class for {@link MaxBindingConstants} bridge used to connect to the
 * maxCube device.
 *
 * @author Marcel Verpaalen - Initial contribution
 */
public class MaxCubeBridgeConfiguration {

    /** The IP address of the MAX! Cube LAN gateway */
    public String ipAddress;

    /**
     * The port of the MAX! Cube LAN gateway as provided at
     * <a href="http://www.elv.de/controller.aspx?cid=824&detail=10&detail2=3484">
     * http://www.elv.de/controller.aspx?cid=824&amp;detail=10&amp;detail2=3484</a>
     */
    public Integer port;

    /** The refresh interval in seconds which is used to poll given MAX! Cube */
    public Integer refreshInterval;

    /** The unique serial number for a device */
    public String serialNumber;

    /**
     * If set to true, the binding will leave the connection to the cube open
     * and just request new informations. This allows much higher poll rates and
     * causes less load than the non-exclusive polling but has the drawback that
     * no other apps (i.E. original software) can use the cube while this
     * binding is running.
     */
    public boolean exclusive = false;

    /**
     * in exclusive mode, how many requests are allowed until connection is
     * closed and reopened
     */
    public Integer maxRequestsPerConnection;

    public Integer cubeReboot;

    /** NTP Server 1 hostname */
    public String ntpServer1;

    /** NTP Server 2 hostname */
    public String ntpServer2;
}
