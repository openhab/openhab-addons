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
package org.openhab.binding.evcc.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.type.ChannelTypeUID;

/**
 * The {@link EvccBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Florian Hotze - Initial contribution
 */
@NonNullByDefault
public class EvccBindingConstants {

    private static final String BINDING_ID = "evcc";

    // List of all Channel ids
    public static final String CHANNEL_BATTERY_CAPACITY = "batteryCapacity";
    public static final String CHANNEL_BATTERY_POWER = "batteryPower";
    public static final String CHANNEL_BATTERY_SOC = "batterySoC";
    public static final String CHANNEL_BATTERY_PRIORITY_SOC = "batteryPrioritySoC";
    public static final String CHANNEL_GRID_POWER = "gridPower";
    public static final String CHANNEL_HOME_POWER = "homePower";
    public static final String CHANNEL_PV_POWER = "pvPower";
    public static final String CHANNEL_LOADPOINT_ACTIVE_PHASES = "activePhases";
    public static final String CHANNEL_LOADPOINT_CHARGE_CURRENT = "chargeCurrent";
    public static final String CHANNEL_LOADPOINT_CHARGE_DURATION = "chargeDuration";
    public static final String CHANNEL_LOADPOINT_CHARGE_POWER = "chargePower";
    public static final String CHANNEL_LOADPOINT_CHARGE_REMAINING_DURATION = "chargeRemainingDuration";
    public static final String CHANNEL_LOADPOINT_CHARGE_REMAINING_ENERGY = "chargeRemainingEnergy";
    public static final String CHANNEL_LOADPOINT_CHARGED_ENERGY = "chargedEnergy";
    public static final String CHANNEL_LOADPOINT_CHARGING = "charging";
    public static final String CHANNEL_LOADPOINT_CONNECTED = "vehicleConnected";
    public static final String CHANNEL_LOADPOINT_CONNECTED_DURATION = "vehicleConnectedDuration";
    public static final String CHANNEL_LOADPOINT_ENABLED = "enabled";
    public static final String CHANNEL_LOADPOINT_HAS_VEHICLE = "hasVehicle";
    public static final String CHANNEL_LOADPOINT_MAX_CURRENT = "maxCurrent";
    public static final String CHANNEL_LOADPOINT_MIN_CURRENT = "minCurrent";
    public static final String CHANNEL_LOADPOINT_MIN_SOC = "minSoC";
    public static final String CHANNEL_LOADPOINT_MODE = "mode";
    public static final String CHANNEL_LOADPOINT_PHASES = "phases";
    public static final String CHANNEL_LOADPOINT_TARGET_SOC = "targetSoC";
    public static final String CHANNEL_LOADPOINT_TARGET_TIME = "targetTime";
    /**
     * Whether a target time is set on loadpoint.
     */
    public static final String CHANNEL_LOADPOINT_TARGET_TIME_ENABLED = "targetTimeEnabled";
    public static final String CHANNEL_LOADPOINT_TITLE = "title";
    public static final String CHANNEL_LOADPOINT_VEHICLE_CAPACITY = "vehicleCapacity";
    public static final String CHANNEL_LOADPOINT_VEHICLE_ODOMETER = "vehicleOdometer";
    public static final String CHANNEL_LOADPOINT_VEHICLE_PRESENT = "vehiclePresent";
    public static final String CHANNEL_LOADPOINT_VEHICLE_RANGE = "vehicleRange";
    public static final String CHANNEL_LOADPOINT_VEHICLE_SOC = "vehicleSoC";
    public static final String CHANNEL_LOADPOINT_VEHICLE_TITLE = "vehicleTitle";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_DEVICE = new ThingTypeUID(BINDING_ID, "device");

    // List of all Channel Type UIDs
    public static final ChannelTypeUID CHANNEL_TYPE_UID_BATTERY_CAPACITY = new ChannelTypeUID(BINDING_ID,
            CHANNEL_BATTERY_CAPACITY);
    public static final ChannelTypeUID CHANNEL_TYPE_UID_BATTERY_POWER = new ChannelTypeUID(BINDING_ID,
            CHANNEL_BATTERY_POWER);
    public static final ChannelTypeUID CHANNEL_TYPE_UID_BATTERY_SOC = new ChannelTypeUID(BINDING_ID,
            CHANNEL_BATTERY_SOC);
    public static final ChannelTypeUID CHANNEL_TYPE_UID_BATTERY_PRIORITY_SOC = new ChannelTypeUID(BINDING_ID,
            CHANNEL_BATTERY_PRIORITY_SOC);
    public static final ChannelTypeUID CHANNEL_TYPE_UID_GRID_POWER = new ChannelTypeUID(BINDING_ID, CHANNEL_GRID_POWER);
    public static final ChannelTypeUID CHANNEL_TYPE_UID_HOME_POWER = new ChannelTypeUID(BINDING_ID, CHANNEL_HOME_POWER);
    public static final ChannelTypeUID CHANNEL_TYPE_UID_PV_POWER = new ChannelTypeUID(BINDING_ID, CHANNEL_PV_POWER);
    public static final ChannelTypeUID CHANNEL_TYPE_UID_LOADPOINT_ACTIVE_PHASES = new ChannelTypeUID(BINDING_ID,
            CHANNEL_LOADPOINT_ACTIVE_PHASES);
    public static final ChannelTypeUID CHANNEL_TYPE_UID_LOADPOINT_CHARGE_CURRENT = new ChannelTypeUID(BINDING_ID,
            CHANNEL_LOADPOINT_CHARGE_CURRENT);
    public static final ChannelTypeUID CHANNEL_TYPE_UID_LOADPOINT_CHARGE_DURATION = new ChannelTypeUID(BINDING_ID,
            CHANNEL_LOADPOINT_CHARGE_DURATION);
    public static final ChannelTypeUID CHANNEL_TYPE_UID_LOADPOINT_CHARGE_POWER = new ChannelTypeUID(BINDING_ID,
            CHANNEL_LOADPOINT_CHARGE_POWER);
    public static final ChannelTypeUID CHANNEL_TYPE_UID_LOADPOINT_CHARGE_REMAINING_DURATION = new ChannelTypeUID(
            BINDING_ID, CHANNEL_LOADPOINT_CHARGE_REMAINING_DURATION);
    public static final ChannelTypeUID CHANNEL_TYPE_UID_LOADPOINT_CHARGE_REMAINING_ENERGY = new ChannelTypeUID(
            BINDING_ID, CHANNEL_LOADPOINT_CHARGE_REMAINING_ENERGY);
    public static final ChannelTypeUID CHANNEL_TYPE_UID_LOADPOINT_CHARGED_ENERGY = new ChannelTypeUID(BINDING_ID,
            CHANNEL_LOADPOINT_CHARGED_ENERGY);
    public static final ChannelTypeUID CHANNEL_TYPE_UID_LOADPOINT_CHARGING = new ChannelTypeUID(BINDING_ID,
            CHANNEL_LOADPOINT_CHARGING);
    public static final ChannelTypeUID CHANNEL_TYPE_UID_LOADPOINT_CONNECTED = new ChannelTypeUID(BINDING_ID,
            CHANNEL_LOADPOINT_CONNECTED);
    public static final ChannelTypeUID CHANNEL_TYPE_UID_LOADPOINT_CONNECTED_DURATION = new ChannelTypeUID(BINDING_ID,
            CHANNEL_LOADPOINT_CONNECTED_DURATION);
    public static final ChannelTypeUID CHANNEL_TYPE_UID_LOADPOINT_ENABLED = new ChannelTypeUID(BINDING_ID,
            CHANNEL_LOADPOINT_ENABLED);
    public static final ChannelTypeUID CHANNEL_TYPE_UID_LOADPOINT_HAS_VEHICLE = new ChannelTypeUID(BINDING_ID,
            CHANNEL_LOADPOINT_HAS_VEHICLE);
    public static final ChannelTypeUID CHANNEL_TYPE_UID_LOADPOINT_MAX_CURRENT = new ChannelTypeUID(BINDING_ID,
            CHANNEL_LOADPOINT_MAX_CURRENT);
    public static final ChannelTypeUID CHANNEL_TYPE_UID_LOADPOINT_MIN_CURRENT = new ChannelTypeUID(BINDING_ID,
            CHANNEL_LOADPOINT_MIN_CURRENT);
    public static final ChannelTypeUID CHANNEL_TYPE_UID_LOADPOINT_MIN_SOC = new ChannelTypeUID(BINDING_ID,
            CHANNEL_LOADPOINT_MIN_SOC);
    public static final ChannelTypeUID CHANNEL_TYPE_UID_LOADPOINT_MODE = new ChannelTypeUID(BINDING_ID,
            CHANNEL_LOADPOINT_MODE);
    public static final ChannelTypeUID CHANNEL_TYPE_UID_LOADPOINT_PHASES = new ChannelTypeUID(BINDING_ID,
            CHANNEL_LOADPOINT_PHASES);
    public static final ChannelTypeUID CHANNEL_TYPE_UID_LOADPOINT_TARGET_SOC = new ChannelTypeUID(BINDING_ID,
            CHANNEL_LOADPOINT_TARGET_SOC);
    public static final ChannelTypeUID CHANNEL_TYPE_UID_LOADPOINT_TARGET_TIME = new ChannelTypeUID(BINDING_ID,
            CHANNEL_LOADPOINT_TARGET_TIME);
    public static final ChannelTypeUID CHANNEL_TYPE_UID_LOADPOINT_TARGET_TIME_ENABLED = new ChannelTypeUID(BINDING_ID,
            CHANNEL_LOADPOINT_TARGET_TIME_ENABLED);
    public static final ChannelTypeUID CHANNEL_TYPE_UID_LOADPOINT_TITLE = new ChannelTypeUID(BINDING_ID,
            CHANNEL_LOADPOINT_TITLE);
    public static final ChannelTypeUID CHANNEL_TYPE_UID_LOADPOINT_VEHICLE_CAPACITY = new ChannelTypeUID(BINDING_ID,
            CHANNEL_LOADPOINT_VEHICLE_CAPACITY);
    public static final ChannelTypeUID CHANNEL_TYPE_UID_LOADPOINT_VEHICLE_ODOMETER = new ChannelTypeUID(BINDING_ID,
            CHANNEL_LOADPOINT_VEHICLE_ODOMETER);
    public static final ChannelTypeUID CHANNEL_TYPE_UID_LOADPOINT_VEHICLE_PRESENT = new ChannelTypeUID(BINDING_ID,
            CHANNEL_LOADPOINT_VEHICLE_PRESENT);
    public static final ChannelTypeUID CHANNEL_TYPE_UID_LOADPOINT_VEHICLE_RANGE = new ChannelTypeUID(BINDING_ID,
            CHANNEL_LOADPOINT_VEHICLE_RANGE);
    public static final ChannelTypeUID CHANNEL_TYPE_UID_LOADPOINT_VEHICLE_SOC = new ChannelTypeUID(BINDING_ID,
            CHANNEL_LOADPOINT_VEHICLE_SOC);
    public static final ChannelTypeUID CHANNEL_TYPE_UID_LOADPOINT_VEHICLE_TITLE = new ChannelTypeUID(BINDING_ID,
            CHANNEL_LOADPOINT_VEHICLE_TITLE);

    public static final int CONNECTION_TIMEOUT_MILLISEC = 5000;
    public static final int LONG_CONNECTION_TIMEOUT_MILLISEC = 60000;
    public static final String EVCC_REST_API = "/api/";
}
