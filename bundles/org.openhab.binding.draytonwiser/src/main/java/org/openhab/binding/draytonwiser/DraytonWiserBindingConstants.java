/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.draytonwiser;

import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.thing.ThingTypeUID;

import com.google.common.collect.Sets;

/**
 * The {@link DraytonWiserBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Andrew Schofield - Initial contribution
 */
@NonNullByDefault
public class DraytonWiserBindingConstants {

    public static final String BINDING_ID = "draytonwiser";

    public static final String ADDRESS = "ADDR";

    public static final String AUTH_TOKEN = "AUTHTOKEN";

    public static final String REFRESH_INTERVAL = "REFRESH";

    // Web Service Endpoints
    public static final String DEVICE_ENDPOINT = "data/domain/Device/";
    public static final String ROOMSTATS_ENDPOINT = "data/domain/RoomStat/";
    public static final String TRVS_ENDPOINT = "data/domain/SmartValve/";
    public static final String ROOMS_ENDPOINT = "data/domain/Room/";
    public static final String SCHEDULES_ENDPOINT = "data/domain/Schedule/";
    public static final String HEATCHANNELS_ENDPOINT = "data/domain/HeatingChannel/";
    public static final String SYSTEM_ENDPOINT = "data/domain/System/";

    // bridge
    public static final ThingTypeUID THING_TYPE_BRIDGE = new ThingTypeUID(BINDING_ID, "heathub");

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_ROOM = new ThingTypeUID(BINDING_ID, "room");
    public static final ThingTypeUID THING_TYPE_ROOMSTAT = new ThingTypeUID(BINDING_ID, "roomstat");
    public static final ThingTypeUID THING_TYPE_ITRV = new ThingTypeUID(BINDING_ID, "itrv");

    // List of all Channel ids
    public static final String CHANNEL_CURRENT_TEMPERATURE = "currentTemperature";
    public static final String CHANNEL_CURRENT_HUMIDITY = "currentHumidity";
    public static final String CHANNEL_CURRENT_SETPOINT = "currentSetPoint";
    public static final String CHANNEL_CURRENT_BATTERY_VOLTAGE = "currentBatteryVoltage";
    public static final String CHANNEL_CURRENT_BATTERY_LEVEL = "currentBatteryLevel";
    public static final String CHANNEL_CURRENT_DEMAND = "currentDemand";
    public static final String CHANNEL_HEAT_REQUEST = "heatRequest";
    public static final String CHANNEL_CURRENT_SIGNAL_RSSI = "currentSignalRSSI";
    public static final String CHANNEL_CURRENT_SIGNAL_STRENGTH = "currentSignalStrength";

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Sets.newHashSet(THING_TYPE_ROOM,
            THING_TYPE_ROOMSTAT, THING_TYPE_BRIDGE, THING_TYPE_ITRV);

}
