/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.network;

import java.util.Collections;
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
    public static final ThingTypeUID THING_TYPE_DEVICE = new ThingTypeUID(BINDING_ID, "device");

    // List of all Channel ids
    public static final String CHANNEL_ONLINE = "online";
    public static final String CHANNEL_TIME = "time";

    // List of all Parameters
    public static final String PARAMETER_HOSTNAME = "hostname";
    public static final String PARAMETER_PORT = "port";
    public static final String PARAMETER_RETRY = "retry";
    public static final String PARAMETER_DHCPLISTEN = "dhcplisten";
    public static final String PARAMETER_TIMEOUT = "timeout";
    public static final String PARAMETER_REFRESH_INTERVAL = "refresh_interval";
    public static final String PARAMETER_USE_SYSTEM_PING = "use_system_ping";

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Collections.singleton(THING_TYPE_DEVICE);

}
