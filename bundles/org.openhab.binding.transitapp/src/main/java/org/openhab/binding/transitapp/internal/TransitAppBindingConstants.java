/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.transitapp.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

@NonNullByDefault
public class TransitAppBindingConstants {
    public static final String BINDING_ID = "transitapp";

    public static final ThingTypeUID THING_TYPE_BRIDGE = new ThingTypeUID(BINDING_ID, "bridge");
    public static final ThingTypeUID THING_TYPE_STOP = new ThingTypeUID(BINDING_ID, "stop");
    public static final ThingTypeUID THING_TYPE_ROUTE_DETAILS = new ThingTypeUID(BINDING_ID, "routedetails");
    public static final ThingTypeUID THING_TYPE_TRIP_DETAILS = new ThingTypeUID(BINDING_ID, "tripdetails");

    // Configuration parameter keys
    public static final String CONFIG_GLOBAL_STOP_ID = "globalStopId";

    // Transit API
    public static final String API_BASE_URL = "https://external.transitapp.com/v4/public/";
    public static final String API_KEY_HEADER = "apiKey";
    public static final int API_TIMEOUT_SECONDS = 10;

    // Channel groups
    public static final String CHANNEL_GROUP_DEPARTURE_PREFIX = "depart";
    public static final String CHANNEL_GROUP_ROUTE = "route";
    public static final String CHANNEL_GROUP_TRIP = "trip";
    public static final String CHANNEL_GROUP_STOP_PREFIX = "stop";
    public static final int MAX_CHANNEL_GROUPS = 10;

    // Channels
    public static final String CHANNEL_ROUTE_LONG_NAME = "route-long-name";
    public static final String CHANNEL_ROUTE_SHORT_NAME = "route-short-name";
    public static final String CHANNEL_ROUTE_COLOR = "route-color";
    public static final String CHANNEL_ACTIVE_ALERTS_COUNT = "active-alerts-count";
    public static final String CHANNEL_DEPARTURE_TIME = "departure-time";
    public static final String CHANNEL_MINUTES_UNTIL_DEPARTURE = "minutes-until-departure";
    public static final String CHANNEL_DELAY_MINUTES = "delay-minutes";
    public static final String CHANNEL_WHEELCHAIR_ACCESSIBLE = "wheelchair-accessible";
    public static final String CHANNEL_IS_CANCELLED = "is-cancelled";
    public static final String CHANNEL_STOP_NAME = "stop-name";
    public static final String CHANNEL_TIME_TO_TARGET = "time-to-target";

    // Default API Performance & Caching Parameters (can be overridden via bridge config)
    public static final long DEFAULT_CACHE_TIME_MS = 30_000;
    public static final int DEFAULT_RETRY_AFTER_SECONDS = 60;
}
