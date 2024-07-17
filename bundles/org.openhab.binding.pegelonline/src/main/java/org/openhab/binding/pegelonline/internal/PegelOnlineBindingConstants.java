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
package org.openhab.binding.pegelonline.internal;

import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.library.types.PointType;
import org.openhab.core.thing.ThingTypeUID;

import com.google.gson.Gson;

/**
 * The {@link PegelOnlineBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public class PegelOnlineBindingConstants {

    private static final String BINDING_ID = "pegelonline";

    // List of all Thing Type UIDs
    public static final ThingTypeUID STATION_THING = new ThingTypeUID(BINDING_ID, "station");
    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Set.of(STATION_THING);

    // List of all Channel ids
    public static final String TIMESTAMP_CHANNEL = "timestamp";
    public static final String LEVEL_CHANNEL = "level";
    public static final String TREND_CHANNEL = "trend";
    public static final String WARNING_CHANNEL = "warning";

    public static final int NO_WARNING = 0;
    public static final int WARN_LEVEL_1 = 1;
    public static final int WARN_LEVEL_2 = 2;
    public static final int WARN_LEVEL_3 = 3;
    public static final int HQ10 = 4;
    public static final int HQ100 = 5;
    public static final int HQ_EXTREME = 6;

    public static final Gson GSON = new Gson();

    public static final String STATIONS_URI = "https://www.pegelonline.wsv.de/webservices/rest-api/v2/stations";
    public static final double DISCOVERY_RADIUS = 50;
    public static final PointType UNDEF_LOCATION = PointType.valueOf("-1,-1");

    public static final String SPACE = " ";
    public static final String UNDERLINE = "_";
    public static final String HYPHEN = " - ";
    public static final String EMPTY = "";
    public static final String UNKNOWN = "Unknown";
}
