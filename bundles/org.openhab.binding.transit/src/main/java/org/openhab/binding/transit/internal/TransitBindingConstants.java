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
package org.openhab.binding.transit.internal;

import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link TransitBindingConstants} class defines common constants used across the whole binding.
 *
 * @author Michael - Initial contribution
 */
@NonNullByDefault
public class TransitBindingConstants {
    public static final String BINDING_ID = "transit";

    public static final ThingTypeUID THING_TYPE_BRIDGE = new ThingTypeUID(BINDING_ID, "bridge");
    public static final ThingTypeUID THING_TYPE_STOP = new ThingTypeUID(BINDING_ID, "stop");
    public static final ThingTypeUID THING_TYPE_ROUTE_DETAILS = new ThingTypeUID(BINDING_ID, "routedetails");
    public static final ThingTypeUID THING_TYPE_TRIP_DETAILS = new ThingTypeUID(BINDING_ID, "tripdetails");
    public static final ThingTypeUID THING_TYPE_NETWORK_ALERTS = new ThingTypeUID(BINDING_ID, "networkalerts");
    public static final ThingTypeUID THING_TYPE_JOURNEY = new ThingTypeUID(BINDING_ID, "journey");
    public static final ThingTypeUID THING_TYPE_VEHICLES = new ThingTypeUID(BINDING_ID, "vehicles");

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES = Set.of(THING_TYPE_BRIDGE, THING_TYPE_STOP,
            THING_TYPE_ROUTE_DETAILS, THING_TYPE_TRIP_DETAILS, THING_TYPE_NETWORK_ALERTS, THING_TYPE_JOURNEY,
            THING_TYPE_VEHICLES);

    // Configuration parameter keys
    public static final String CONFIG_GLOBAL_STOP_ID = "globalStopId";

    // Transit API
    public static final String API_BASE_URL = "https://external.transitapp.com/v4/public/";
    // the vehicles endpoint is not part of the /v4/public/ path
    public static final String API_VEHICLES_URL = "https://external.transitapp.com/v4/vehicles";
    public static final String API_KEY_HEADER = "apiKey";
    public static final int API_TIMEOUT_SECONDS = 10;

    // Channel groups
    public static final String CHANNEL_GROUP_DEPARTURE_PREFIX = "depart";
    public static final String CHANNEL_GROUP_ROUTE = "route";
    public static final String CHANNEL_GROUP_TRIP = "trip";
    public static final String CHANNEL_GROUP_STOP_PREFIX = "stop";
    public static final String CHANNEL_GROUP_ALERTS = "alerts";
    public static final String CHANNEL_GROUP_JOURNEY = "journey";
    public static final String CHANNEL_GROUP_VEHICLES = "vehicles";
    public static final String CHANNEL_GROUP_VEHICLE_PREFIX = "vehicle";
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

    // Channels added with the extended departure, alert, journey and vehicle support
    public static final String CHANNEL_HEADSIGN = "headsign";
    public static final String CHANNEL_IS_REAL_TIME = "is-real-time";
    public static final String CHANNEL_IS_LAST = "is-last";
    public static final String CHANNEL_TRANSPORT_MODE = "transport-mode";
    public static final String CHANNEL_GLOBAL_STOP_ID = "global-stop-id";
    public static final String CHANNEL_GLOBAL_ROUTE_ID = "global-route-id";
    public static final String CHANNEL_TRIP_SEARCH_KEY = "trip-search-key";
    public static final String CHANNEL_ALERT_TITLE = "alert-title";
    public static final String CHANNEL_ALERT_DESCRIPTION = "alert-description";
    public static final String CHANNEL_ALERT_SEVERITY = "alert-severity";
    public static final String CHANNEL_ALERT_EFFECT = "alert-effect";
    public static final String CHANNEL_ALERT_CAUSE = "alert-cause";
    public static final String CHANNEL_ARRIVAL_TIME = "arrival-time";
    public static final String CHANNEL_DURATION = "duration";
    public static final String CHANNEL_TRANSFERS = "transfers";
    public static final String CHANNEL_LINES = "lines";
    public static final String CHANNEL_VEHICLE_COUNT = "vehicle-count";
    public static final String CHANNEL_LOCATION = "location";
    public static final String CHANNEL_LAST_UPDATE = "last-update";
    public static final String CHANNEL_DIRECTION_ID = "direction-id";
    public static final String CHANNEL_OCCUPANCY_STATUS = "occupancy-status";
    public static final String CHANNEL_VEHICLE_LABEL = "vehicle-label";

    // Default API Performance & Caching Parameters (can be overridden via bridge config)
    public static final long DEFAULT_CACHE_TIME_MS = 30_000;
    public static final int DEFAULT_RETRY_AFTER_SECONDS = 60;

    // Minimum refresh interval for all polling Things
    public static final long MIN_REFRESH_INTERVAL_SECONDS = 30;
}
