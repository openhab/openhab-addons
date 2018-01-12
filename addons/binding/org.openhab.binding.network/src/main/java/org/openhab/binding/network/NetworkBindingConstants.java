/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.network;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
 * The {@link NetworkBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Marc Mettke - Initial contribution
 * @author David Gräff - 2016, Add dhcp listen
 */
public class NetworkBindingConstants {

    public static final String BINDING_ID = "network";

    // List of all Thing Type UIDs
    public static final ThingTypeUID BACKWARDS_COMPATIBLE_DEVICE = new ThingTypeUID(BINDING_ID, "device");
    public static final ThingTypeUID PING_DEVICE = new ThingTypeUID(BINDING_ID, "pingdevice");
    public static final ThingTypeUID SERVICE_DEVICE = new ThingTypeUID(BINDING_ID, "servicedevice");

    // List of all Channel ids
    public static final String CHANNEL_ONLINE = "online";
    public static final String CHANNEL_LATENCY = "latency";
    public static final String CHANNEL_DEPRECATED_TIME = "time";
    public static final String CHANNEL_LASTSEEN = "lastseen";

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
    public static final String PROPERTY_IOS_WAKEUP = "uses_ios_wakeup";

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = new HashSet<>();

    static {
        SUPPORTED_THING_TYPES_UIDS.add(PING_DEVICE);
        SUPPORTED_THING_TYPES_UIDS.add(SERVICE_DEVICE);
        SUPPORTED_THING_TYPES_UIDS.add(BACKWARDS_COMPATIBLE_DEVICE);
    }

}
