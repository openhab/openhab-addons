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
package org.openhab.binding.onebusaway.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link OneBusAwayBinding} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Shawn Wilsher - Initial contribution
 */
@NonNullByDefault
public class OneBusAwayBindingConstants {

    public static final String BINDING_ID = "onebusaway";

    // Things
    public static final ThingTypeUID THING_TYPE_API = new ThingTypeUID(BINDING_ID, "api");
    public static final ThingTypeUID THING_TYPE_ROUTE = new ThingTypeUID(BINDING_ID, "route");
    public static final ThingTypeUID THING_TYPE_STOP = new ThingTypeUID(BINDING_ID, "stop");

    // Channel IDs
    public static final String CHANNEL_ID_ARRIVAL = "arrival";
    public static final String CHANNEL_ID_DEPARTURE = "departure";
    public static final String CHANNEL_ID_UPDATE = "update";

    // Events
    public static final String EVENT_ARRIVAL = "ARRIVAL";
    public static final String EVENT_DEPARTURE = "DEPARTURE";

    // Event channel IDs
    public static final String EVENT_CHANNEL_ID_ARRIVAL = "arrivalDeparture#event";

    // Channel configs
    public static final String CHANNEL_CONFIG_OFFSET = "offset";

    // API configs
    public static final String API_CONFIG_API_KEY = "apiKey";
    public static final String API_CONFIG_API_SERVER = "apiServer";

    // Route configs
    public static final String ROUTE_CONFIG_ROUTE_ID = "routeId";

    // Stop configs
    public static final String STOP_CONFIG_ID = "stopId";
    public static final String STOP_CONFIG_INTERVAL = "interval";

    // Route properties
    public static final String ROUTE_PROPERTY_HEADSIGN = "headsign";
    public static final String ROUTE_PROPERTY_LONG_NAME = "longName";
    public static final String ROUTE_PROPERTY_SHORT_NAME = "shortName";
}
