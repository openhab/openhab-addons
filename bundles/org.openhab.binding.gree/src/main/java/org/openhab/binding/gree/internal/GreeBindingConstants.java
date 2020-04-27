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

import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
 * The {@link GreeBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author John Cunha - Initial contribution
 * @author Markus Michels - Refactoring, adapted to OH 2.5x
 */
// @NonNullByDefault
public class GreeBindingConstants {

    public static final String BINDING_ID = "gree";

    public static final ThingTypeUID THING_TYPE_GREEAIRCON = new ThingTypeUID(BINDING_ID, "airconditioner");
    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Collections.singleton(THING_TYPE_GREEAIRCON);

    /**
     * Contains the Port that is used to communicate using UDP with Gree Airconditioners. .
     */
    public static final int GREE_PORT = 7000;

    /**
     * Contains the character set to be used to communicate with Gree Airconditioners.
     */
    public static final String CHARSET = "UTF-8";

    /**
     * Contains the refresh rate to be used for retrieving data from the Gree Airconditioner.
     */
    public static final String REFRESH_TIME = "refresh";

    /*
     * The minimum refresh time in milliseconds. Any REFRESH command send to a Thing, before
     * this time has expired, will not trigger an attempt to request status data from the
     * Gree Airconditioner.
     **/
    public static final int MINIMUM_REFRESH_TIME = 1000;

    /*
     * The default refresh time in milliseconds to be used for Discovery when the user has no option to set the value.
     **/
    public static final int DEFAULT_REFRESH_TIME = 1000;

    /*
     * The timeout for the Datagram socket used to communicate with Gree Airconditioners.
     * This is particularly important when scanning for devices because this will effectively
     * be the amount of time spent scanning.
     **/
    public static final int DATAGRAM_SOCKET_TIMEOUT = 5000;

    public static final int DISCOVERY_TIMEOUT_MS = 7000;

    /*
     * The IP Address used to used to send Scan Datagram to Gree Airconditioners.
     **/
    // public static final String DATAGRAM_BROADCAST_IP_ADDRESS = "192.168.1.255";

    // List of all Thing Type UIDs
    public static final ThingTypeUID GREE_THING_TYPE = new ThingTypeUID(BINDING_ID, "airconditioner");

    /**
     * Contains the IP network address of the Gree Airconditioner.
     */
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

}
