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
package org.openhab.binding.teslascope.internal;

import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link TeslascopeBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Paul Smedley - Initial contribution
 */
@NonNullByDefault
public class TeslascopeBindingConstants {

    private static final String BINDING_ID = "teslascope";

    // List of all Thing Type UIDs
    public static final ThingTypeUID TESLASCOPE_ACCOUNT = new ThingTypeUID(BINDING_ID, "account");
    public static final ThingTypeUID TESLASCOPE_VEHICLE = new ThingTypeUID(BINDING_ID, "vehicle");

    // List of Config ids
    public static final String CONFIG_PUBLICID = "publicID";
    // List of all Channel ids
    public static final String CHANNEL_VIN = "vin";
    public static final String CHANNEL_VEHICLE_NAME = "vehicle-name";
    public static final String CHANNEL_VEHICLE_STATE = "vehicle-state";
    public static final String CHANNEL_ODOMETER = "odometer";
    public static final String CHANNEL_BATTERY_LEVEL = "battery-level";
    public static final String CHANNEL_CHARGING_STATE = "charging-state";
    public static final String CHANNEL_TPMS_FL = "tpms-pressure-fl";
    public static final String CHANNEL_TPMS_FR = "tpms-pressure-fr";
    public static final String CHANNEL_TPMS_RL = "tpms-pressure-rl";
    public static final String CHANNEL_TPMS_RR = "tpms-pressure-rr";
    public static final String CHANNEL_TPMS_SOFT_WARNING_FL = "tpms-soft-warning-fl";
    public static final String CHANNEL_TPMS_SOFT_WARNING_FR = "tpms-soft-warning-fr";
    public static final String CHANNEL_TPMS_SOFT_WARNING_RL = "tpms-soft-warning-rl";
    public static final String CHANNEL_TPMS_SOFT_WARNING_RR = "tpms-soft-warning-rr";
    public static final String CHANNEL_SOFTWARE_UPDATE_AVAILABLE = "software-update-available";
    public static final String CHANNEL_SOFTWARE_UPDATE_STATUS = "software-update-status";
    public static final String CHANNEL_SOFTWARE_UPDATE_VERSION = "software-update-version";
    public static final String CHANNEL_AUTOCONDITIONING = "auto-conditioning";
    public static final String CHANNEL_BATTERY_RANGE = "battery-range";
    public static final String CHANNEL_CENTER_REAR_SEAT_HEATER = "center-rear-seat-heater";
    public static final String CHANNEL_CHARGE = "charge";
    public static final String CHANNEL_CHARGE_ENERGY_ADDED = "charge-energy-added";
    public static final String CHANNEL_CHARGE_AMPS = "charge-amps";
    public static final String CHANNEL_CHARGE_CURRENT_REQUEST = "charge-current-request";
    public static final String CHANNEL_CHARGE_CURRENT_REQUEST_MAX = "charge-current-request-max";
    public static final String CHANNEL_CHARGE_LIMIT_SOC = "charge-limit-soc";
    public static final String CHANNEL_CHARGE_LIMIT_SOC_MIN = "charge-limit-soc-min";
    public static final String CHANNEL_CHARGE_LIMIT_SOC_MAX = "charge-limit-soc-max";
    public static final String CHANNEL_CHARGE_LIMIT_SOC_STANDARD = "charge-limit-soc-standard";
    public static final String CHANNEL_CHARGE_PORT = "charge-port";
    public static final String CHANNEL_CHARGE_PORT_LATCH = "charge-port-latch";
    public static final String CHANNEL_CHARGE_RATE = "charge-rate";
    public static final String CHANNEL_CHARGER_POWER = "charger-power";
    public static final String CHANNEL_CHARGER_VOLTAGE = "charger-voltage";
    public static final String CHANNEL_CLIMATE = "climate";
    public static final String CHANNEL_DOOR_LOCK = "door-lock";
    public static final String CHANNEL_DRIVER_FRONT_DOOR = "driver-front-door";
    public static final String CHANNEL_DRIVER_REAR_DOOR = "driver-rear-door";
    public static final String CHANNEL_DRIVER_TEMP = "driver-temp";
    public static final String CHANNEL_ESTIMATED_BATTERY_RANGE = "estimated-battery-range";
    public static final String CHANNEL_FAN = "fan";
    public static final String CHANNEL_FLASH_LIGHTS = "flash-lights";
    public static final String CHANNEL_FRONT_DEFROSTER = "front-defroster";
    public static final String CHANNEL_FRONT_TRUNK = "front-trunk";
    public static final String CHANNEL_HEADING = "heading";
    public static final String CHANNEL_HOMELINK = "homelink";
    public static final String CHANNEL_HONK_HORN = "honk-horn";
    public static final String CHANNEL_INSIDE_TEMP = "inside-temp";
    public static final String CHANNEL_LEFT_REAR_SEAT_HEATER = "left-rear-seat-heater";
    public static final String CHANNEL_LEFT_SEAT_HEATER = "left-seat-heater";
    public static final String CHANNEL_LEFT_TEMP_DIRECTION = "left-temp-direction";
    public static final String CHANNEL_LOCATED_AT_HOME = "located-at-home";
    public static final String CHANNEL_LOCATED_AT_WORK = "located-at-work";
    public static final String CHANNEL_LOCATED_AT_FAVORITE = "located-at-favorite";
    public static final String CHANNEL_LOCATION = "location";
    public static final String CHANNEL_MIN_AVAILABLE_TEMP = "min-available-temp";
    public static final String CHANNEL_MAX_AVAILABLE_TEMP = "max-available-temp";
    public static final String CHANNEL_OUTSIDE_TEMP = "outside-temp";
    public static final String CHANNEL_PASSENGER_FRONT_DOOR = "passenger-front-door";
    public static final String CHANNEL_PASSENGER_REAR_DOOR = "passenger-rear-door";
    public static final String CHANNEL_PASSENGER_TEMP = "passenger-temp";
    public static final String CHANNEL_POWER = "power";
    public static final String CHANNEL_PRECONDITIONING = "preconditioning";
    public static final String CHANNEL_REAR_DEFROSTER = "rear-defroster";
    public static final String CHANNEL_REAR_TRUNK = "rear-trunk";
    public static final String CHANNEL_RIGHT_REAR_SEAT_HEATER = "right-rear-seat-heater";
    public static final String CHANNEL_RIGHT_SEAT_HEATER = "right-seat-heater";
    public static final String CHANNEL_RIGHT_TEMP_DIRECTION = "right-temp-direction";
    public static final String CHANNEL_SCHEDULED_CHARGING_PENDING = "scheduled-charging-pending";
    public static final String CHANNEL_SCHEDULED_CHARGING_START = "scheduled-charging-start";
    public static final String CHANNEL_SENTRY_MODE = "sentry-mode";
    public static final String CHANNEL_SHIFT_STATE = "shift-state";
    public static final String CHANNEL_SIDE_MIRROR_HEATERS = "side-mirror-heaters";
    public static final String CHANNEL_SMARTPRECONDITIONG = "smart-preconditioning";
    public static final String CHANNEL_SPEED = "speed";
    public static final String CHANNEL_STEERING_WHEEL_HEATER = "steering-wheel-heater";
    public static final String CHANNEL_SUNROOF = "sunroof";
    public static final String CHANNEL_SUNROOF_STATE = "sunroof-state";
    public static final String CHANNEL_TIME_TO_FULL_CHARGE = "time-to-full-charge";
    public static final String CHANNEL_USABLE_BATTERY_LEVEL = "usable-battery-level";
    public static final String CHANNEL_VALET_MODE = "valet-mode";
    public static final String CHANNEL_WIPER_BLADE_HEATER = "wiper-blade-heater";

    public static final Set<ThingTypeUID> BRIDGE_THING_TYPES_UIDS = Set.of(TESLASCOPE_ACCOUNT);
    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Set.of(TESLASCOPE_ACCOUNT, TESLASCOPE_VEHICLE);
}
