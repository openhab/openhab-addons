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
package org.openhab.binding.renault.internal;

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link RenaultBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Doug Culnane - Initial contribution
 */
@NonNullByDefault
public class RenaultBindingConstants {

    private static final String BINDING_ID = "renault";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_CAR = new ThingTypeUID(BINDING_ID, "car");

    // List of all Channel ids
    public static final String CHANNEL_BATTERY_AVAILABLE_ENERGY = "batteryavailableenergy";
    public static final String CHANNEL_BATTERY_LEVEL = "batterylevel";
    public static final String CHANNEL_BATTERY_STATUS_UPDATED = "batterystatusupdated";
    public static final String CHANNEL_CHARGING_MODE = "chargingmode";
    public static final String CHANNEL_PAUSE = "pause";
    public static final String CHANNEL_CHARGING_STATUS = "chargingstatus";
    public static final String CHANNEL_CHARGING_REMAINING_TIME = "chargingremainingtime";
    public static final String CHANNEL_ESTIMATED_RANGE = "estimatedrange";
    public static final String CHANNEL_EXTERNAL_TEMPERATURE = "externaltemperature";
    public static final String CHANNEL_HVAC_STATUS = "hvacstatus";
    public static final String CHANNEL_HVAC_TARGET_TEMPERATURE = "hvactargettemperature";
    public static final String CHANNEL_IMAGE = "image";
    public static final String CHANNEL_LOCATION = "location";
    public static final String CHANNEL_LOCATION_UPDATED = "locationupdated";
    public static final String CHANNEL_LOCKED = "locked";
    public static final String CHANNEL_ODOMETER = "odometer";
    public static final String CHANNEL_PLUG_STATUS = "plugstatus";

    public static final List<String> HVAC_CHANNELS = List.of(CHANNEL_HVAC_STATUS, CHANNEL_HVAC_TARGET_TEMPERATURE);
    public static final List<String> BATTERY_CHANNELS = List.of(CHANNEL_BATTERY_AVAILABLE_ENERGY, CHANNEL_BATTERY_LEVEL,
            CHANNEL_BATTERY_STATUS_UPDATED, CHANNEL_CHARGING_MODE, CHANNEL_CHARGING_STATUS,
            CHANNEL_CHARGING_REMAINING_TIME);
    public static final List<String> ALL_CHANNELS = List.of(CHANNEL_IMAGE, CHANNEL_LOCATION, CHANNEL_LOCATION_UPDATED,
            CHANNEL_ODOMETER, CHANNEL_LOCKED, CHANNEL_HVAC_STATUS, CHANNEL_HVAC_TARGET_TEMPERATURE,
            CHANNEL_EXTERNAL_TEMPERATURE, CHANNEL_PLUG_STATUS, CHANNEL_CHARGING_STATUS, CHANNEL_BATTERY_LEVEL,
            CHANNEL_ESTIMATED_RANGE, CHANNEL_BATTERY_AVAILABLE_ENERGY, CHANNEL_CHARGING_REMAINING_TIME,
            CHANNEL_BATTERY_STATUS_UPDATED, CHANNEL_CHARGING_MODE);
}
