/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.bluelink.internal;

import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.library.CoreItemFactory;
import org.openhab.core.thing.ThingTypeUID;

/**
 * @author Marcus Better - Initial contribution
 */
@NonNullByDefault
public class BluelinkBindingConstants {

    public static final String BINDING_ID = "bluelink";

    // Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_ACCOUNT = new ThingTypeUID(BINDING_ID, "account");
    public static final ThingTypeUID THING_TYPE_VEHICLE = new ThingTypeUID(BINDING_ID, "vehicle");
    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Set.of(THING_TYPE_ACCOUNT, THING_TYPE_VEHICLE);

    // API Configuration
    public static final String API_ENDPOINT = "https://api.telematics.hyundaiusa.com";
    public static final String CLIENT_ID = "m66129Bb-em93-SPAHYN-bZ91-am4540zp19920";
    public static final String CLIENT_SECRET = "v558o935-6nne-423i-baa8";

    // Channel Groups
    public static final String GROUP_STATUS = "status";
    public static final String GROUP_CHARGING = "charging";
    public static final String GROUP_FUEL = "fuel";
    public static final String GROUP_DOORS = "doors";
    public static final String GROUP_WINDOWS = "windows";
    public static final String GROUP_CLIMATE = "climate";
    public static final String GROUP_RANGE = "range";
    public static final String GROUP_WARNINGS = "warnings";

    // Channel Types
    public static final String CHANNEL_TYPE_EV_SOC = "ev-soc";
    public static final String CHANNEL_TYPE_EV_CHARGING = "ev-charging";
    public static final String CHANNEL_TYPE_EV_PLUGGED_IN = "ev-plugged-in";
    public static final String CHANNEL_TYPE_CHARGE_LIMIT_DC = "charge-limit-dc";
    public static final String CHANNEL_TYPE_CHARGE_LIMIT_AC = "charge-limit-ac";
    public static final String CHANNEL_TYPE_CHARGE_TIME_CURRENT = "charge-time-current";
    public static final String CHANNEL_TYPE_CHARGE_TIME_FAST = "charge-time-fast";
    public static final String CHANNEL_TYPE_CHARGE_TIME_PORTABLE = "charge-time-portable";
    public static final String CHANNEL_TYPE_CHARGE_TIME_STATION = "charge-time-station";
    public static final String CHANNEL_TYPE_FUEL_LEVEL = "fuel-level";
    public static final String CHANNEL_TYPE_FUEL_WARNING = "fuel-warning";

    // Item Types
    public static final String NUMBER_DIMENSIONLESS = CoreItemFactory.NUMBER + ":Dimensionless";
    public static final String NUMBER_TIME = CoreItemFactory.NUMBER + ":Time";

    // Status Channels
    public static final String CHANNEL_LOCKED = "locked";
    public static final String CHANNEL_ENGINE_RUNNING = "engine-running";
    public static final String CHANNEL_ODOMETER = "odometer";
    public static final String CHANNEL_BATTERY_LEVEL = "battery-level";
    public static final String CHANNEL_LOCATION = "location";
    public static final String CHANNEL_LAST_UPDATE = "last-update";

    // Door Channels
    public static final String CHANNEL_DOOR_FRONT_LEFT = "front-left";
    public static final String CHANNEL_DOOR_FRONT_RIGHT = "front-right";
    public static final String CHANNEL_DOOR_REAR_LEFT = "rear-left";
    public static final String CHANNEL_DOOR_REAR_RIGHT = "rear-right";
    public static final String CHANNEL_TRUNK = "trunk";
    public static final String CHANNEL_HOOD = "hood";

    // Window Channels
    public static final String CHANNEL_WINDOW_FRONT_LEFT = "front-left";
    public static final String CHANNEL_WINDOW_FRONT_RIGHT = "front-right";
    public static final String CHANNEL_WINDOW_REAR_LEFT = "rear-left";
    public static final String CHANNEL_WINDOW_REAR_RIGHT = "rear-right";

    // Climate Channels
    public static final String CHANNEL_TEMPERATURE = "temperature-setpoint";
    public static final String CHANNEL_HVAC_ON = "hvac-on";
    public static final String CHANNEL_DEFROST = "defrost";
    public static final String CHANNEL_SEAT_FRONT_LEFT = "seat-front-left";
    public static final String CHANNEL_SEAT_FRONT_RIGHT = "seat-front-right";
    public static final String CHANNEL_SEAT_REAR_LEFT = "seat-rear-left";
    public static final String CHANNEL_SEAT_REAR_RIGHT = "seat-rear-right";
    public static final String CHANNEL_STEERING_HEATER = "steering-heater";
    public static final String CHANNEL_REAR_WINDOW_HEATER = "rear-window-heater";
    public static final String CHANNEL_SIDE_MIRROR_HEATER = "side-mirror-heater";

    // Range Channels
    public static final String CHANNEL_TOTAL_RANGE = "total-range";
    public static final String CHANNEL_EV_RANGE = "ev-range";
    public static final String CHANNEL_FUEL_RANGE = "fuel-range";

    // Fuel Channels
    public static final String CHANNEL_FUEL_LEVEL = "level";
    public static final String CHANNEL_FUEL_LOW_WARNING = "low-fuel-warning";

    // Battery and Charging Channels
    public static final String CHANNEL_EV_BATTERY_SOC = "soc";
    public static final String CHANNEL_EV_CHARGING = "charging";
    public static final String CHANNEL_EV_PLUGGED_IN = "plugged-in";
    public static final String CHANNEL_CHARGE_LIMIT_DC = "charge-limit-dc";
    public static final String CHANNEL_CHARGE_LIMIT_AC = "charge-limit-ac";
    public static final String CHANNEL_TIME_TO_FULL_CURRENT = "time-to-full-current";
    public static final String CHANNEL_TIME_TO_FULL_FAST = "time-to-full-fast";
    public static final String CHANNEL_TIME_TO_FULL_PORTABLE = "time-to-full-portable";
    public static final String CHANNEL_TIME_TO_FULL_STATION = "time-to-full-station";

    // Warning Channels
    public static final String CHANNEL_TIRE_PRESSURE_WARNING = "tire-pressure";
    public static final String CHANNEL_TIRE_PRESSURE_WARNING_FL = "tire-pressure-front-left";
    public static final String CHANNEL_TIRE_PRESSURE_WARNING_FR = "tire-pressure-front-right";
    public static final String CHANNEL_TIRE_PRESSURE_WARNING_RR = "tire-pressure-rear-left";
    public static final String CHANNEL_TIRE_PRESSURE_WARNING_RL = "tire-pressure-rear-right";
    public static final String CHANNEL_WASHER_FLUID_WARNING = "washer-fluid";
    public static final String CHANNEL_BRAKE_FLUID_WARNING = "brake-fluid";
    public static final String CHANNEL_SMART_KEY_BATTERY_WARNING = "smart-key-battery";

    // Vehicle Properties
    public static final String PROPERTY_VIN = "vin";
    public static final String PROPERTY_MODEL = "model";
    public static final String PROPERTY_ENGINE_TYPE = "engineType";
}
