/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * @author Ganesh Ingle <ganesh.ingle@asvilabs.com>
 */

package org.openhab.binding.wakeonlan;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
 * The {@link WakeOnLanBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Ganesh Ingle - initial contribution
 */
public class WakeOnLanBindingConstants {

    public static final String BINDING_ID = "wakeonlan";
    public static final String BINDING_LOGGER_NAME = "org.openhab.binding.wakeonlan";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_WOLDEVICE = new ThingTypeUID(BINDING_ID, "wol-device");

    // List of all Channel id's
    public static final String CHANNEL_WAKEUP = "wakeup";
    public static final String CHANNEL_SHUTDOWN = "shutdown";
    public static final String CHANNEL_STATUS = "status";
    public static final String CHANNEL_POWER = "power";

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = new HashSet<ThingTypeUID>(
            Arrays.asList(THING_TYPE_WOLDEVICE));

    public static final String DFLT_BROADCAST_ADDRESS = "255.255.255.255";
    public static final Integer DFLT_UDP_WOL_PORT = 9;
    public static final int MAX_HIGHER_FREQ_PING_COUNT = 16;
    public static final long HIGHER_FREQ_PING_INTERVAL_MILLIS = 15000;

    // List of standard statuses
    public static final String STATUS_PACKET_SENT = "Packet Sent";
    public static final String STATUS_SHUTDOWN_COMMANDS_SENT = "Shutdown Commands Sent";
    public static final String STATUS_READY = "Ready";
    public static final String STATUS_UPDATING = "Updating";
    public static final String STATUS_HOST_ONLINE = "Online";
    public static final String STATUS_HOST_OFFLINE = "Offline";
    public static final String STATUS_SHUTDOWN_COMMANDS_NOT_CONFIGURED = "Shutdown Command Not Configured";
}
