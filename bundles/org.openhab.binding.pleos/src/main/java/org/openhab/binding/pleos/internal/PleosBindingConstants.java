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
package org.openhab.binding.pleos.internal;

import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The PleosBindingConstants class defines all global IDs,
 * thing types, and constants for the Pleos Vehicle Data API binding.
 *
 * @author Stephan Sandmann - Initial contribution
 */
@NonNullByDefault
public class PleosBindingConstants {

    public static final String BINDING_ID = "pleos";

    public static final ThingTypeUID THING_TYPE_BRIDGE = new ThingTypeUID(BINDING_ID, "account");
    public static final ThingTypeUID THING_TYPE_VEHICLE = new ThingTypeUID(BINDING_ID, "vehicle");

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_SET = Set.of(THING_TYPE_BRIDGE, THING_TYPE_VEHICLE);

    // Account Bridge Channel IDs
    public static final String CHANNEL_SECRET_EXPIRES = "secret-expires";
    public static final String CHANNEL_SECRET_REMAINING_DAYS = "secret-remaining-days";
    public static final String CHANNEL_SECRET_EXPIRY_WARNING = "secret-expiry-warning";
    public static final String CHANNEL_SECRET_EXPIRY_EVENT = "secret-expiry-event";

    // Channel group IDs
    public static final String GROUP_VEHICLE = "vehicle";
    public static final String GROUP_CHARGING = "charging";
    public static final String GROUP_FUEL = "fuel";
    public static final String GROUP_DOORS = "doors";
    public static final String GROUP_TIRES = "tires";
    public static final String GROUP_CLIMATE = "climate";
    public static final String GROUP_SEATS = "seats";
    public static final String GROUP_WARNINGS = "warnings";
    public static final String GROUP_FREEZER = "freezer";

    // Dynamic Channel Type IDs
    public static final String CHANNEL_TYPE_SOC = "soc";
    public static final String CHANNEL_TYPE_BATTERY_CAPACITY = "battery-capacity";
    public static final String CHANNEL_TYPE_CHARGING_BOOLEAN = "charging-boolean";
    public static final String CHANNEL_TYPE_CHARGE_PLUGIN_ENUM = "charge-plugin-enum";
    public static final String CHANNEL_TYPE_CHARGE_STATUS_ENUM = "charge-status-enum";
    public static final String CHANNEL_TYPE_REMAIN_TIME = "remain-time";
    public static final String CHANNEL_TYPE_TARGET_SOC_STANDARD = "target-soc-standard";
    public static final String CHANNEL_TYPE_TARGET_SOC_QUICK = "target-soc-quick";
    public static final String CHANNEL_TYPE_FUEL_LEVEL = "fuel-level";

    // Dynamic Channel IDs - Charging
    public static final String CHANNEL_SOC = "soc";
    public static final String CHANNEL_CAPACITY = "capacity";
    public static final String CHANNEL_CHARGING = "charging";
    public static final String CHANNEL_PLUGIN = "plugin";
    public static final String CHANNEL_CHARGE_STATUS = "charge-status";
    public static final String CHANNEL_REMAIN_TIME = "remain-time";
    public static final String CHANNEL_TARGET_SOC_STANDARD = "target-soc-standard";
    public static final String CHANNEL_TARGET_SOC_QUICK = "target-soc-quick";

    // Dynamic Channel IDs - Fuel
    public static final String CHANNEL_FUEL_LEVEL = "fuel-level";

    // Thing Properties
    public static final String PROPERTY_VIN = "vin";
    public static final String PROPERTY_VEHICLE_TYPE = "vehicleType";
    public static final String PROPERTY_MODEL_NAME = "modelName";
    public static final String PROPERTY_MODEL_CODE = "modelCode";

    // Static Channel IDs - Vehicle
    public static final String CHANNEL_DISTANCE_TO_EMPTY = "distance-to-empty";
    public static final String CHANNEL_DISTANCE_TYPE = "distance-type";
    public static final String CHANNEL_LOCATION_LAST_UPDATE = "location-last-update";
    public static final String CHANNEL_LATITUDE = "latitude";
    public static final String CHANNEL_LONGITUDE = "longitude";
    public static final String CHANNEL_ALTITUDE = "altitude";
    public static final String CHANNEL_LOCATION = "location";
    public static final String CHANNEL_SPEED = "speed";
    public static final String CHANNEL_HEADING = "heading";
    public static final String CHANNEL_IGNITION = "ignition";
    public static final String CHANNEL_IGNITION_ACC = "ignition-acc";
    public static final String CHANNEL_IGNITION_IGN1 = "ignition-ign1";
    public static final String CHANNEL_IGNITION_IGN3 = "ignition-ign3";
    public static final String CHANNEL_SLEEP_MODE = "sleep-mode";
    public static final String CHANNEL_ODOMETER = "odometer";
    public static final String CHANNEL_PARKING_POSITION = "parking-position";
    public static final String CHANNEL_LAST_UPDATE = "last-update";

    // Static Channel IDs - Doors
    public static final String CHANNEL_DOOR_FRONT_LEFT = "door-front-left";
    public static final String CHANNEL_DOOR_FRONT_RIGHT = "door-front-right";
    public static final String CHANNEL_DOOR_REAR_LEFT = "door-rear-left";
    public static final String CHANNEL_DOOR_REAR_RIGHT = "door-rear-right";
    public static final String CHANNEL_HOOD = "hood";
    public static final String CHANNEL_TRUNK = "trunk";
    public static final String CHANNEL_SUNROOF = "sunroof";

    // Static Channel IDs - Tires
    public static final String CHANNEL_TIRE_PRESSURE_FRONT_LEFT = "tire-pressure-front-left";
    public static final String CHANNEL_TIRE_PRESSURE_FRONT_RIGHT = "tire-pressure-front-right";
    public static final String CHANNEL_TIRE_PRESSURE_REAR_LEFT = "tire-pressure-rear-left";
    public static final String CHANNEL_TIRE_PRESSURE_REAR_RIGHT = "tire-pressure-rear-right";
    public static final String CHANNEL_TIRE_WARNING_FL = "tire-warning-fl";
    public static final String CHANNEL_TIRE_WARNING_FR = "tire-warning-fr";
    public static final String CHANNEL_TIRE_WARNING_RL = "tire-warning-rl";
    public static final String CHANNEL_TIRE_WARNING_RR = "tire-warning-rr";
    public static final String CHANNEL_TIRE_WARNING_LOW_GENERIC = "tire-warning-low-generic";

    // Static Channel IDs - Warnings
    public static final String CHANNEL_WARNING_LOW_FUEL = "warning-low-fuel";
    public static final String CHANNEL_WARNING_SMARTKEY_BATTERY = "warning-smartkey-battery";
    public static final String CHANNEL_WARNING_WASH_FLUID = "warning-wash-fluid";
    public static final String CHANNEL_WARNING_BRAKE_OIL = "warning-brake-oil";
    public static final String CHANNEL_WARNING_ENGINE_OIL = "warning-engine-oil";
    public static final String CHANNEL_WARNING_LAMP_HEADLAMP = "warning-lamp-headlamp";
    public static final String CHANNEL_WARNING_LAMP_TURN_FRONT_LEFT = "warning-lamp-turn-front-left";
    public static final String CHANNEL_WARNING_LAMP_TURN_FRONT_RIGHT = "warning-lamp-turn-front-right";
    public static final String CHANNEL_WARNING_LAMP_STOP_REAR_LEFT = "warning-lamp-stop-rear-left";
    public static final String CHANNEL_WARNING_LAMP_STOP_REAR_RIGHT = "warning-lamp-stop-rear-right";
    public static final String CHANNEL_WARNING_LAMP_TURN_REAR_LEFT = "warning-lamp-turn-rear-left";
    public static final String CHANNEL_WARNING_LAMP_TURN_REAR_RIGHT = "warning-lamp-turn-rear-right";

    // Static Channel IDs - Climate
    public static final String CHANNEL_CLIMATE_MODE = "climate-mode";
    public static final String CHANNEL_CLIMATE_STATUS = "climate-status";
    public static final String CHANNEL_CLIMATE_SPEED = "climate-speed";
    public static final String CHANNEL_CLIMATE_TARGET_TEMP = "climate-target-temp";
    public static final String CHANNEL_WINDSHIELD_HEATER = "windshield-heater";
    public static final String CHANNEL_STEERING_WHEEL_HEATER = "steering-wheel-heater";
    public static final String CHANNEL_OUTSIDE_TEMPERATURE = "outside-temperature";

    // Static Channel IDs - Seats
    public static final String CHANNEL_SEAT_FRONT_LEFT_HEAT = "seat-front-left-heat";
    public static final String CHANNEL_SEAT_FRONT_LEFT_VENT = "seat-front-left-vent";
    public static final String CHANNEL_SEAT_FRONT_RIGHT_HEAT = "seat-front-right-heat";
    public static final String CHANNEL_SEAT_FRONT_RIGHT_VENT = "seat-front-right-vent";
    public static final String CHANNEL_SEAT_REAR_LEFT_HEAT = "seat-rear-left-heat";
    public static final String CHANNEL_SEAT_REAR_LEFT_VENT = "seat-rear-left-vent";
    public static final String CHANNEL_SEAT_REAR_RIGHT_HEAT = "seat-rear-right-heat";
    public static final String CHANNEL_SEAT_REAR_RIGHT_VENT = "seat-rear-right-vent";

    // Static Channel IDs - Freezer
    public static final String CHANNEL_FREEZER_ACTIVE = "freezer-active";
    public static final String CHANNEL_FREEZER_TEMPERATURE = "freezer-temperature";
}
