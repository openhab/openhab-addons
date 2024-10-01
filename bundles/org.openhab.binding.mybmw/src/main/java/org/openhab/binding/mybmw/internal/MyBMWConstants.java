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
 * @author Martin Grassl - updated enum values
 */
@NonNullByDefault
public interface MyBMWConstants {

    static final String BINDING_ID = "mybmw";

    static final String VIN = "vin";

    static final String REFRESH_INTERVAL = "refreshInterval";

    static final String VEHICLE_BRAND = "vehicleBrand";

    static final String REMOTE_SERVICES_DISABLED = "remoteServicesDisabled";

    static final String REMOTE_SERVICES_ENABLED = "remoteServicesEnabled";

    static final String SERVICES_DISABLED = "servicesDisabled";

    static final String SERVICES_ENABLED = "servicesEnabled";

    static final String SERVICES_UNSUPPORTED = "servicesUnsupported";

    static final String SERVICES_SUPPORTED = "servicesSupported";

    static final String VEHICLE_BODYTYPE = "vehicleBodytype";

    static final String VEHICLE_CONSTRUCTION_YEAR = "vehicleConstructionYear";

    static final String VEHICLE_DRIVE_TRAIN = "vehicleDriveTrain";

    static final String VEHICLE_MODEL = "vehicleModel";

    static final int DEFAULT_IMAGE_SIZE_PX = 1024;

    static final int DEFAULT_REFRESH_INTERVAL_MINUTES = 60;

    // See constants from bimmer-connected
    // https://github.com/bimmerconnected/bimmer_connected/blob/master/bimmer_connected/vehicle.py
    enum VehicleType {
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

    enum ChargingMode {
        IMMEDIATE_CHARGING,
        DELAYED_CHARGING
    }

    enum ChargingPreference {
        NO_PRESELECTION,
        CHARGING_WINDOW
    }

    static final Set<String> FUEL_VEHICLES = Set.of(VehicleType.CONVENTIONAL.toString(),
            VehicleType.PLUGIN_HYBRID.toString(), VehicleType.ELECTRIC_REX.toString());
    static final Set<String> ELECTRIC_VEHICLES = Set.of(VehicleType.ELECTRIC.toString(),
            VehicleType.PLUGIN_HYBRID.toString(), VehicleType.ELECTRIC_REX.toString());

    // List of all Thing Type UIDs
    static final ThingTypeUID THING_TYPE_CONNECTED_DRIVE_ACCOUNT = new ThingTypeUID(BINDING_ID, "account");
    static final ThingTypeUID THING_TYPE_CONV = new ThingTypeUID(BINDING_ID, VehicleType.CONVENTIONAL.toString());
    static final ThingTypeUID THING_TYPE_PHEV = new ThingTypeUID(BINDING_ID, VehicleType.PLUGIN_HYBRID.toString());
    static final ThingTypeUID THING_TYPE_BEV_REX = new ThingTypeUID(BINDING_ID, VehicleType.ELECTRIC_REX.toString());
    static final ThingTypeUID THING_TYPE_BEV = new ThingTypeUID(BINDING_ID, VehicleType.ELECTRIC.toString());
    static final Set<ThingTypeUID> SUPPORTED_THING_SET = Set.of(THING_TYPE_CONNECTED_DRIVE_ACCOUNT, THING_TYPE_CONV,
            THING_TYPE_PHEV, THING_TYPE_BEV_REX, THING_TYPE_BEV);

    // Thing Group definitions
    static final String CHANNEL_GROUP_UPDATE = "update";
    static final String CHANNEL_GROUP_STATUS = "status";
    static final String CHANNEL_GROUP_SERVICE = "service";
    static final String CHANNEL_GROUP_CHECK_CONTROL = "check";
    static final String CHANNEL_GROUP_DOORS = "doors";
    static final String CHANNEL_GROUP_RANGE = "range";
    static final String CHANNEL_GROUP_LOCATION = "location";
    static final String CHANNEL_GROUP_REMOTE = "remote";
    static final String CHANNEL_GROUP_CHARGE_PROFILE = "profile";
    static final String CHANNEL_GROUP_CHARGE_STATISTICS = "statistic";
    static final String CHANNEL_GROUP_CHARGE_SESSION = "session";
    static final String CHANNEL_GROUP_TIRES = "tires";
    static final String CHANNEL_GROUP_VEHICLE_IMAGE = "image";

    // types of updates
    static final String STATE_UPDATE = "state-update";
    static final String CHARGING_UPDATE = "charging-update";
    static final String IMAGE_UPDATE = "image-update";

    // Charge Statistics & Sessions
    static final String SESSIONS = "sessions";
    static final String ENERGY = "energy";
    static final String TITLE = "title";
    static final String SUBTITLE = "subtitle";
    static final String ISSUE = "issue";
    static final String STATUS = "status";

    // Generic Constants for several groups
    static final String NAME = "name";
    static final String DETAILS = "details";
    static final String SEVERITY = "severity";
    static final String DATE = "date";
    static final String MILEAGE = "mileage";
    static final String GPS = "gps";
    static final String HEADING = "heading";
    static final String ADDRESS = "address";
    static final String HOME_DISTANCE = "home-distance";

    // Status
    static final String DOORS = "doors";
    static final String WINDOWS = "windows";
    static final String LOCK = "lock";
    static final String SERVICE_DATE = "service-date";
    static final String SERVICE_MILEAGE = "service-mileage";
    static final String CHECK_CONTROL = "check-control";
    static final String PLUG_CONNECTION = "plug-connection";
    static final String CHARGE_STATUS = "charge";
    static final String CHARGE_REMAINING = "charge-remaining";
    static final String LAST_UPDATE = "last-update";
    static final String LAST_FETCHED = "last-fetched";
    static final String RAW = "raw";

    // Door Details
    static final String DOOR_DRIVER_FRONT = "driver-front";
    static final String DOOR_DRIVER_REAR = "driver-rear";
    static final String DOOR_PASSENGER_FRONT = "passenger-front";
    static final String DOOR_PASSENGER_REAR = "passenger-rear";
    static final String HOOD = "hood";
    static final String TRUNK = "trunk";
    static final String WINDOW_DOOR_DRIVER_FRONT = "win-driver-front";
    static final String WINDOW_DOOR_DRIVER_REAR = "win-driver-rear";
    static final String WINDOW_DOOR_PASSENGER_FRONT = "win-passenger-front";
    static final String WINDOW_DOOR_PASSENGER_REAR = "win-passenger-rear";
    static final String WINDOW_REAR = "win-rear";
    static final String SUNROOF = "sunroof";

    // Charge Profile
    static final String CHARGE_PROFILE_CLIMATE = "climate";
    static final String CHARGE_PROFILE_MODE = "mode";
    static final String CHARGE_PROFILE_PREFERENCE = "prefs";
    static final String CHARGE_PROFILE_CONTROL = "control";
    static final String CHARGE_PROFILE_TARGET = "target";
    static final String CHARGE_PROFILE_LIMIT = "limit";
    static final String CHARGE_WINDOW_START = "window-start";
    static final String CHARGE_WINDOW_END = "window-end";
    static final String CHARGE_TIMER1 = "timer1";
    static final String CHARGE_TIMER2 = "timer2";
    static final String CHARGE_TIMER3 = "timer3";
    static final String CHARGE_TIMER4 = "timer4";
    static final String CHARGE_DEPARTURE = "-departure";
    static final String CHARGE_ENABLED = "-enabled";
    static final String CHARGE_DAY_MON = "-day-mon";
    static final String CHARGE_DAY_TUE = "-day-tue";
    static final String CHARGE_DAY_WED = "-day-wed";
    static final String CHARGE_DAY_THU = "-day-thu";
    static final String CHARGE_DAY_FRI = "-day-fri";
    static final String CHARGE_DAY_SAT = "-day-sat";
    static final String CHARGE_DAY_SUN = "-day-sun";

    // Range
    static final String RANGE_ELECTRIC = "electric";
    static final String RANGE_RADIUS_ELECTRIC = "radius-electric";
    static final String RANGE_FUEL = "fuel";
    static final String RANGE_RADIUS_FUEL = "radius-fuel";
    static final String RANGE_HYBRID = "hybrid";
    static final String RANGE_RADIUS_HYBRID = "radius-hybrid";
    static final String REMAINING_FUEL = "remaining-fuel";
    static final String ESTIMATED_FUEL_L_100KM = "estimated-fuel-l-100km";
    static final String ESTIMATED_FUEL_MPG = "estimated-fuel-mpg";
    static final String SOC = "soc";

    // Image
    static final String IMAGE_FORMAT = "png";
    static final String IMAGE_VIEWPORT = "view";

    // Remote Services
    static final String REMOTE_SERVICE_LIGHT_FLASH = "light-flash";
    static final String REMOTE_SERVICE_VEHICLE_FINDER = "vehicle-finder";
    static final String REMOTE_SERVICE_DOOR_LOCK = "door-lock";
    static final String REMOTE_SERVICE_DOOR_UNLOCK = "door-unlock";
    static final String REMOTE_SERVICE_HORN = "horn-blow";
    static final String REMOTE_SERVICE_AIR_CONDITIONING_START = "climate-now-start";
    static final String REMOTE_SERVICE_AIR_CONDITIONING_STOP = "climate-now-stop";
    static final String REMOTE_SERVICE_START_CHARGING = "start-charging";
    static final String REMOTE_SERVICE_STOP_CHARGING = "stop-charging";

    static final String REMOTE_SERVICE_COMMAND = "command";
    static final String REMOTE_STATE = "state";

    // TIRES
    static final String FRONT_LEFT_CURRENT = "fl-current";
    static final String FRONT_LEFT_TARGET = "fl-target";
    static final String FRONT_RIGHT_CURRENT = "fr-current";
    static final String FRONT_RIGHT_TARGET = "fr-target";
    static final String REAR_LEFT_CURRENT = "rl-current";
    static final String REAR_LEFT_TARGET = "rl-target";
    static final String REAR_RIGHT_CURRENT = "rr-current";
    static final String REAR_RIGHT_TARGET = "rr-target";
}
