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
package org.openhab.binding.gree.internal;

import java.util.Collections;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
 * The {@link GreeBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author John Cunha - Initial contribution
 * @author Markus Michels - Refactoring, adapted to OH 2.5x
 */
@NonNullByDefault
public class GreeBindingConstants {

    public static final String BINDING_ID = "gree";

    public static final ThingTypeUID THING_TYPE_GREEAIRCON = new ThingTypeUID(BINDING_ID, "airconditioner");
    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Collections.singleton(THING_TYPE_GREEAIRCON);

    // List of all Thing Type UIDs
    public static final ThingTypeUID GREE_THING_TYPE = new ThingTypeUID(BINDING_ID, "airconditioner");

    // Thing configuration items
    public static final String PROPERTY_IP = "ipAddress";
    public static final String PROPERTY_BROADCAST = "broadcastAddress";

    // List of all Channel ids
    public static final String POWER_CHANNEL = "power";
    public static final String MODE_CHANNEL = "mode";
    public static final String TURBO_CHANNEL = "turbo";
    public static final String LIGHT_CHANNEL = "light";
    public static final String TEMP_CHANNEL = "temperature";
    public static final String SWINGV_CHANNEL = "swingvertical";
    public static final String WINDSPEED_CHANNEL = "windspeed";
    public static final String AIR_CHANNEL = "air";
    public static final String DRY_CHANNEL = "dry";
    public static final String HEALTH_CHANNEL = "health";
    public static final String PWRSAV_CHANNEL = "powersave";

    // UDPPort used to communicate using UDP with GREE Airconditioners. .
    public static final int GREE_PORT = 7000;

    /*
     * The timeout for the Datagram socket used to communicate with Gree Airconditioners.
     * This is particularly important when scanning for devices because this will effectively
     * be the amount of time spent scanning.
     */
    public static final int DATAGRAM_SOCKET_TIMEOUT = 3000;
    public static final int MINIMUM_REFRESH_TIME_MS = 1000;
    public static final int DEFAULT_REFRESH_TIME_MS = 1000;

    public static final int DISCOVERY_TIMEOUT_MS = 5000;
    public static final int MAX_SCAN_CYCLES = 3;

    public static final int DIGITS_TEMP = 1;

    public static final String CHARSET = "UTF-8";
}
