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
package org.openhab.binding.mybmw.internal;

import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link MyBMWConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Bernd Weymann - Initial contribution
 * @author Norbert Truchsess - edit and send of charge profile
 */
@NonNullByDefault
public class MyBMWConstants {

    private static final String BINDING_ID = "mybmw";

    public static final String VIN = "vin";

    public static final int DEFAULT_IMAGE_SIZE_PX = 1024;
    public static final int DEFAULT_REFRESH_INTERVAL_MINUTES = 5;

    // See constants from bimmer-connected
    // https://github.com/bimmerconnected/bimmer_connected/blob/master/bimmer_connected/vehicle.py
    public enum VehicleType {
        CONVENTIONAL("conv"),
        PLUGIN_HYBRID("phev"),
        MILD_HYBRID("hybrid"),
        ELECTRIC_REX("bev_rex"),
        ELECTRIC("bev"),
        UNKNOWN("unknown");

        private final String type;

        VehicleType(String s) {
            type = s;
        }

        @Override
        public String toString() {
            return type;
        }
    }

    public enum ChargingMode {
        immediateCharging,
        delayedCharging
    }

    public enum ChargingPreference {
        noPreSelection,
        chargingWindow
    }

    public static final Set<String> FUEL_VEHICLES = Set.of(VehicleType.CONVENTIONAL.toString(),
            VehicleType.PLUGIN_HYBRID.toString(), VehicleType.ELECTRIC_REX.toString());
    public static final Set<String> ELECTRIC_VEHICLES = Set.of(VehicleType.ELECTRIC.toString(),
            VehicleType.PLUGIN_HYBRID.toString(), VehicleType.ELECTRIC_REX.toString());

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_CONNECTED_DRIVE_ACCOUNT = new ThingTypeUID(BINDING_ID, "account");
    public static final ThingTypeUID THING_TYPE_CONV = new ThingTypeUID(BINDING_ID,
            VehicleType.CONVENTIONAL.toString());
    public static final ThingTypeUID THING_TYPE_PHEV = new ThingTypeUID(BINDING_ID,
            VehicleType.PLUGIN_HYBRID.toString());
    public static final ThingTypeUID THING_TYPE_BEV_REX = new ThingTypeUID(BINDING_ID,
            VehicleType.ELECTRIC_REX.toString());
    public static final ThingTypeUID THING_TYPE_BEV = new ThingTypeUID(BINDING_ID, VehicleType.ELECTRIC.toString());
    public static final Set<ThingTypeUID> SUPPORTED_THING_SET = Set.of(THING_TYPE_CONNECTED_DRIVE_ACCOUNT,
            THING_TYPE_CONV, THING_TYPE_PHEV, THING_TYPE_BEV_REX, THING_TYPE_BEV);

    // Thing Group definitions
    public static final String CHANNEL_GROUP_STATUS = "status";
    public static final String CHANNEL_GROUP_SERVICE = "service";
    public static final String CHANNEL_GROUP_CHECK_CONTROL = "check";
    public static final String CHANNEL_GROUP_DOORS = "doors";
    public static final String CHANNEL_GROUP_RANGE = "range";
    public static final String CHANNEL_GROUP_LOCATION = "location";
    public static final String CHANNEL_GROUP_REMOTE = "remote";
    public static final String CHANNEL_GROUP_CHARGE_PROFILE = "profile";
    public static final String CHANNEL_GROUP_CHARGE_STATISTICS = "statistic";
    public static final String CHANNEL_GROUP_CHARGE_SESSION = "session";
    public static final String CHANNEL_GROUP_TIRES = "tires";
    public static final String CHANNEL_GROUP_VEHICLE_IMAGE = "image";

    // Charge Statistics & Sessions
    public static final String SESSIONS = "sessions";
    public static final String ENERGY = "energy";
    public static final String TITLE = "title";
    public static final String SUBTITLE = "subtitle";
    public static final String ISSUE = "issue";
    public static final String STATUS = "status";

    // Generic Constants for several groups
    public static final String NAME = "name";
    public static final String DETAILS = "details";
    public static final String SEVERITY = "severity";
    public static final String DATE = "date";
    public static final String MILEAGE = "mileage";
    public static final String GPS = "gps";
    public static final String HEADING = "heading";
    public static final String ADDRESS = "address";
    public static final String HOME_DISTANCE = "home-distance";

    // Status
    public static final String DOORS = "doors";
    public static final String WINDOWS = "windows";
    public static final String LOCK = "lock";
    public static final String SERVICE_DATE = "service-date";
    public static final String SERVICE_MILEAGE = "service-mileage";
    public static final String CHECK_CONTROL = "check-control";
    public static final String PLUG_CONNECTION = "plug-connection";
    public static final String CHARGE_STATUS = "charge";
    public static final String CHARGE_INFO = "charge-info";
    public static final String MOTION = "motion";
    public static final String LAST_UPDATE = "last-update";
    public static final String RAW = "raw";

    // Door Details
    public static final String DOOR_DRIVER_FRONT = "driver-front";
    public static final String DOOR_DRIVER_REAR = "driver-rear";
    public static final String DOOR_PASSENGER_FRONT = "passenger-front";
    public static final String DOOR_PASSENGER_REAR = "passenger-rear";
    public static final String HOOD = "hood";
    public static final String TRUNK = "trunk";
    public static final String WINDOW_DOOR_DRIVER_FRONT = "win-driver-front";
    public static final String WINDOW_DOOR_DRIVER_REAR = "win-driver-rear";
    public static final String WINDOW_DOOR_PASSENGER_FRONT = "win-passenger-front";
    public static final String WINDOW_DOOR_PASSENGER_REAR = "win-passenger-rear";
    public static final String WINDOW_REAR = "win-rear";
    public static final String SUNROOF = "sunroof";

    // Charge Profile
    public static final String CHARGE_PROFILE_CLIMATE = "climate";
    public static final String CHARGE_PROFILE_MODE = "mode";
    public static final String CHARGE_PROFILE_PREFERENCE = "prefs";
    public static final String CHARGE_PROFILE_CONTROL = "control";
    public static final String CHARGE_PROFILE_TARGET = "target";
    public static final String CHARGE_PROFILE_LIMIT = "limit";
    public static final String CHARGE_WINDOW_START = "window-start";
    public static final String CHARGE_WINDOW_END = "window-end";
    public static final String CHARGE_TIMER1 = "timer1";
    public static final String CHARGE_TIMER2 = "timer2";
    public static final String CHARGE_TIMER3 = "timer3";
    public static final String CHARGE_TIMER4 = "timer4";
    public static final String CHARGE_DEPARTURE = "-departure";
    public static final String CHARGE_ENABLED = "-enabled";
    public static final String CHARGE_DAY_MON = "-day-mon";
    public static final String CHARGE_DAY_TUE = "-day-tue";
    public static final String CHARGE_DAY_WED = "-day-wed";
    public static final String CHARGE_DAY_THU = "-day-thu";
    public static final String CHARGE_DAY_FRI = "-day-fri";
    public static final String CHARGE_DAY_SAT = "-day-sat";
    public static final String CHARGE_DAY_SUN = "-day-sun";

    // Range
    public static final String RANGE_ELECTRIC = "electric";
    public static final String RANGE_RADIUS_ELECTRIC = "radius-electric";
    public static final String RANGE_FUEL = "fuel";
    public static final String RANGE_RADIUS_FUEL = "radius-fuel";
    public static final String RANGE_HYBRID = "hybrid";
    public static final String RANGE_RADIUS_HYBRID = "radius-hybrid";
    public static final String REMAINING_FUEL = "remaining-fuel";
    public static final String SOC = "soc";

    // Image
    public static final String IMAGE_FORMAT = "png";
    public static final String IMAGE_VIEWPORT = "view";

    // Remote Services
    public static final String REMOTE_SERVICE_LIGHT_FLASH = "light-flash";
    public static final String REMOTE_SERVICE_VEHICLE_FINDER = "vehicle-finder";
    public static final String REMOTE_SERVICE_DOOR_LOCK = "door-lock";
    public static final String REMOTE_SERVICE_DOOR_UNLOCK = "door-unlock";
    public static final String REMOTE_SERVICE_HORN = "horn-blow";
    public static final String REMOTE_SERVICE_AIR_CONDITIONING_START = "climate-now-start";
    public static final String REMOTE_SERVICE_AIR_CONDITIONING_STOP = "climate-now-stop";

    public static final String REMOTE_SERVICE_COMMAND = "command";
    public static final String REMOTE_STATE = "state";

    // TIRES
    public static final String FRONT_LEFT_CURRENT = "fl-current";
    public static final String FRONT_LEFT_TARGET = "fl-target";
    public static final String FRONT_RIGHT_CURRENT = "fr-current";
    public static final String FRONT_RIGHT_TARGET = "fr-target";
    public static final String REAR_LEFT_CURRENT = "rl-current";
    public static final String REAR_LEFT_TARGET = "rl-target";
    public static final String REAR_RIGHT_CURRENT = "rr-current";
    public static final String REAR_RIGHT_TARGET = "rr-target";
}
