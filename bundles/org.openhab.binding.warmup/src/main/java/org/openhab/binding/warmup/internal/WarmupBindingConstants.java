/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.warmup.internal;

import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link WarmupBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author James Melville - Initial contribution
 */
@NonNullByDefault
public class WarmupBindingConstants {

    private static final String BINDING_ID = "warmup";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_BRIDGE = new ThingTypeUID(BINDING_ID, "my-warmup");
    public static final ThingTypeUID THING_TYPE_ROOM = new ThingTypeUID(BINDING_ID, "room");

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Set.of(THING_TYPE_BRIDGE, THING_TYPE_ROOM);
    public static final Set<ThingTypeUID> DISCOVERABLE_THING_TYPES_UIDS = Set.of(THING_TYPE_ROOM);

    // Room Channel Ids
    public static final String CHANNEL_CURRENT_TEMPERATURE = "currentTemperature";
    public static final String CHANNEL_TARGET_TEMPERATURE = "targetTemperature";
    public static final String CHANNEL_OVERRIDE_DURATION = "overrideRemaining";
    public static final String CHANNEL_RUN_MODE = "runMode";
    public static final String CHANNEL_FROST_PROTECTION_MODE = "frostProtectionMode";
    public static final String CHANNEL_HEATING_TARGET = "heatingTarget";
    public static final String CHANNEL_AIR_TEMPERATURE = "airTemperature";
    public static final String CHANNEL_FLOOR_TEMPERATURE = "floorTemperature";

    public static final String FROST_PROTECTION_MODE = "anti_frost";

    // Property Labels
    public static final String PROPERTY_ROOM_ID = "Id";
    public static final String PROPERTY_ROOM_NAME = "Name";
    public static final String PROPERTY_LOCATION_ID = "LocationId";
    public static final String PROPERTY_LOCATION_NAME = "Location";

    // Web Service Endpoints
    public static final String APP_ENDPOINT = "https://api.warmup.com/apps/app/v1";
    public static final String QUERY_ENDPOINT = "https://apil.warmup.com/graphql";

    // Web Service Constants
    public static final String USER_AGENT = "WARMUP_APP";
    public static final String APP_TOKEN = "M=;He<Xtg\"$}4N%5k{$:PD+WA\"]D<;#PriteY|VTuA>_iyhs+vA\"4lic{6-LqNM:";

    public static final String AUTH_METHOD = "userLogin";
    public static final String AUTH_APP_ID = "WARMUP-APP-V001";
}
