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
package org.openhab.binding.network.internal;

import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link NetworkBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Marc Mettke - Initial contribution
 * @author David Gräff - 2016, Add dhcp listen
 */
@NonNullByDefault
public class NetworkBindingConstants {

    public static final String BINDING_ID = "network";

    // List of all Thing Type UIDs
    public static final ThingTypeUID BACKWARDS_COMPATIBLE_DEVICE = new ThingTypeUID(BINDING_ID, "device");
    public static final ThingTypeUID PING_DEVICE = new ThingTypeUID(BINDING_ID, "pingdevice");
    public static final ThingTypeUID SERVICE_DEVICE = new ThingTypeUID(BINDING_ID, "servicedevice");
    public static final ThingTypeUID SPEEDTEST_DEVICE = new ThingTypeUID(BINDING_ID, "speedtest");

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Set.of(BACKWARDS_COMPATIBLE_DEVICE, PING_DEVICE,
            SERVICE_DEVICE, SPEEDTEST_DEVICE);

    // List of all Channel ids
    public static final String CHANNEL_ONLINE = "online";
    public static final String CHANNEL_LATENCY = "latency";
    public static final String CHANNEL_LASTSEEN = "lastseen";
    public static final String CHANNEL_TEST_ISRUNNING = "isRunning";
    public static final String CHANNEL_TEST_PROGRESS = "progress";
    public static final String CHANNEL_RATE_UP = "rateUp";
    public static final String CHANNEL_RATE_DOWN = "rateDown";
    public static final String CHANNEL_TEST_START = "testStart";
    public static final String CHANNEL_TEST_END = "testEnd";

    // List of all Parameters
    public static final String PARAMETER_HOSTNAME = "hostname";
    public static final String PARAMETER_RETRY = "retry";
    public static final String PARAMETER_TIMEOUT = "timeout";
    public static final String PARAMETER_REFRESH_INTERVAL = "refreshInterval";
    public static final String PARAMETER_PORT = "port";

    public static final String PROPERTY_DHCP_STATE = "dhcp_state";
    public static final String PROPERTY_ARP_STATE = "arp_state";
    public static final String PROPERTY_ICMP_STATE = "icmp_state";
    public static final String PROPERTY_PRESENCE_DETECTION_TYPE = "presence_detection_type";
}
