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
package org.openhab.binding.volvocars.internal;

import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link VolvoCarsBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Michael Weger - Initial contribution
 */
@NonNullByDefault
public class VolvoCarsBindingConstants {

    public static final String BINDING_ID = "volvocars";

    public static final ThingTypeUID THING_TYPE_ACCOUNT = new ThingTypeUID(BINDING_ID, "account");
    public static final ThingTypeUID THING_TYPE_VEHICLE = new ThingTypeUID(BINDING_ID, "vehicle");

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Set.of(THING_TYPE_ACCOUNT, THING_TYPE_VEHICLE);

    public static final String AUTHORIZE_URL = "https://volvoid.eu.volvocars.com/as/authorization.oauth2";
    public static final String TOKEN_URL = "https://volvoid.eu.volvocars.com/as/token.oauth2";
    public static final String API_URL = "https://api.volvocars.com";
    public static final String CONNECTED_VEHICLE_ENDPOINT = API_URL + "/connected-vehicle/v2/vehicles";
    public static final String ENERGY_ENDPOINT = API_URL + "/energy/v2/vehicles";
    public static final String LOCATION_ENDPOINT = API_URL + "/location/v1/vehicles";

    public static final String DEFAULT_SCOPES = "openid conve:battery_charge_level conve:brake_status "
            + "conve:climatization_start_stop conve:command_accessibility conve:commands "
            + "conve:diagnostics_engine_status conve:diagnostics_workshop conve:doors_status conve:engine_status "
            + "conve:fuel_status conve:lock_status conve:odometer_status conve:trip_statistics conve:tyre_status "
            + "conve:vehicle_relation conve:warnings conve:windows_status energy:capability:read energy:state:read "
            + "conve:lock conve:unlock conve:engine_start_stop conve:honk_flash location:read";

    // Thing properties
    public static final String PROPERTY_VIN = "vin";
    public static final String PROPERTY_MODEL = "model";
    public static final String PROPERTY_MODEL_YEAR = "modelYear";
    public static final String PROPERTY_FUEL_TYPE = "fuelType";
    public static final String PROPERTY_GEARBOX = "gearbox";
    public static final String PROPERTY_EXTERIOR_COLOUR = "exteriorColour";
    public static final String PROPERTY_BATTERY_CAPACITY = "batteryCapacityKWh";
    public static final String PROPERTY_EXTERIOR_IMAGE_URL = "exteriorImageUrl";

    // Channel groups
    public static final String GROUP_DOORS = "doors";
    public static final String GROUP_WINDOWS = "windows";
    public static final String GROUP_STATUS = "status";
    public static final String GROUP_FUEL = "fuel";
    public static final String GROUP_BATTERY = "battery";
    public static final String GROUP_TRIP = "trip";
    public static final String GROUP_LOCATION = "location";
    public static final String GROUP_MAINTENANCE = "maintenance";
    public static final String GROUP_CONTROL = "control";

    // Channels of group doors
    public static final String CHANNEL_LOCK = "lock";
    public static final String CHANNEL_FRONT_LEFT = "front-left";
    public static final String CHANNEL_FRONT_RIGHT = "front-right";
    public static final String CHANNEL_REAR_LEFT = "rear-left";
    public static final String CHANNEL_REAR_RIGHT = "rear-right";
    public static final String CHANNEL_HOOD = "hood";
    public static final String CHANNEL_TAILGATE = "tailgate";
    public static final String CHANNEL_TANK_LID = "tank-lid";

    // Channels of group windows (front-left, front-right, rear-left and rear-right are shared with doors)
    public static final String CHANNEL_SUNROOF = "sunroof";

    // Channels of group status
    public static final String CHANNEL_ENGINE_RUNNING = "engine-running";
    public static final String CHANNEL_ODOMETER = "odometer";
    public static final String CHANNEL_AVAILABILITY = "availability";

    // Channels of group fuel
    public static final String CHANNEL_FUEL_AMOUNT = "fuel-amount";
    public static final String CHANNEL_DISTANCE_TO_EMPTY_TANK = "distance-to-empty-tank";

    // Channels of group battery
    public static final String CHANNEL_BATTERY_LEVEL = "battery-level";
    public static final String CHANNEL_ELECTRIC_RANGE = "electric-range";
    public static final String CHANNEL_DISTANCE_TO_EMPTY_BATTERY = "distance-to-empty-battery";
    public static final String CHANNEL_CHARGER_CONNECTION_STATUS = "charger-connection-status";
    public static final String CHANNEL_CHARGING_STATUS = "charging-status";
    public static final String CHANNEL_CHARGING_TYPE = "charging-type";
    public static final String CHANNEL_CHARGER_POWER_STATUS = "charger-power-status";
    public static final String CHANNEL_ESTIMATED_CHARGING_TIME = "estimated-charging-time";
    public static final String CHANNEL_TARGET_BATTERY_LEVEL = "target-battery-level";
    public static final String CHANNEL_CHARGING_CURRENT_LIMIT = "charging-current-limit";
    public static final String CHANNEL_CHARGING_POWER = "charging-power";

    // Channels of group trip
    public static final String CHANNEL_AVERAGE_SPEED = "average-speed";
    public static final String CHANNEL_AVERAGE_SPEED_AUTOMATIC = "average-speed-automatic";
    public static final String CHANNEL_TRIP_METER_MANUAL = "trip-meter-manual";
    public static final String CHANNEL_TRIP_METER_AUTOMATIC = "trip-meter-automatic";
    public static final String CHANNEL_AVERAGE_FUEL_CONSUMPTION = "average-fuel-consumption";
    public static final String CHANNEL_AVERAGE_FUEL_CONSUMPTION_AUTOMATIC = "average-fuel-consumption-automatic";
    public static final String CHANNEL_AVERAGE_ENERGY_CONSUMPTION = "average-energy-consumption";

    // Channels of group location
    public static final String CHANNEL_POSITION = "position";
    public static final String CHANNEL_HEADING = "heading";
    public static final String CHANNEL_TIMESTAMP = "timestamp";

    // Channels of group maintenance
    public static final String CHANNEL_SERVICE_WARNING = "service-warning";
    public static final String CHANNEL_DISTANCE_TO_SERVICE = "distance-to-service";
    public static final String CHANNEL_TIME_TO_SERVICE = "time-to-service";
    public static final String CHANNEL_ENGINE_HOURS_TO_SERVICE = "engine-hours-to-service";
    public static final String CHANNEL_WASHER_FLUID_WARNING = "washer-fluid-warning";
    public static final String CHANNEL_BRAKE_FLUID_WARNING = "brake-fluid-warning";
    public static final String CHANNEL_OIL_LEVEL_WARNING = "oil-level-warning";
    public static final String CHANNEL_COOLANT_LEVEL_WARNING = "coolant-level-warning";
    public static final String CHANNEL_TYRE_FRONT_LEFT = "tyre-front-left";
    public static final String CHANNEL_TYRE_FRONT_RIGHT = "tyre-front-right";
    public static final String CHANNEL_TYRE_REAR_LEFT = "tyre-rear-left";
    public static final String CHANNEL_TYRE_REAR_RIGHT = "tyre-rear-right";
    public static final String CHANNEL_LIGHT_FAILURE = "light-failure";
    public static final String CHANNEL_LIGHT_FAILURES = "light-failures";

    // Channels of group control
    public static final String CHANNEL_CLIMATIZATION = "climatization";
    public static final String CHANNEL_ENGINE_REMOTE_START = "engine-remote-start";
    public static final String CHANNEL_HONK_FLASH = "honk-flash";
    public static final String CHANNEL_LOCK_REDUCED_GUARD = "lock-reduced-guard";
    public static final String CHANNEL_LAST_COMMAND_STATUS = "last-command-status";
}
