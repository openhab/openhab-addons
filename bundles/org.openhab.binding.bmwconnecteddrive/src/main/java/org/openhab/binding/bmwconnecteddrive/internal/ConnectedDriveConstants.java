/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.bmwconnecteddrive.internal;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
 * The {@link ConnectedDriveConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Bernd Weymann - Initial contribution
 */
@SuppressWarnings("serial")
@NonNullByDefault
public class ConnectedDriveConstants {

    private static final String BINDING_ID = "bmwconnecteddrive";

    // Units
    public static final String UNITS_AUTODETECT = "AUTODETECT";
    public static final String UNITS_IMPERIAL = "IMPERIAL";
    public static final String UNITS_METRIC = "METRIC";

    // See constants from bimmer-connected
    // https://github.com/bimmerconnected/bimmer_connected/blob/master/bimmer_connected/vehicle.py
    public enum VehicleType {
        CONVENTIONAL("CONV"),
        PLUGIN_HYBRID("PHEV"),
        ELECTRIC_REX("BEV_REX"),
        ELECTRIC("BEV");

        private final String type;

        VehicleType(String s) {
            type = s;
        }

        @Override
        public String toString() {
            return type;
        }
    }

    public static final Set<String> FUEL_VEHICLES = new HashSet<String>() {
        {
            add(VehicleType.CONVENTIONAL.toString());
            add(VehicleType.PLUGIN_HYBRID.toString());
            add(VehicleType.ELECTRIC_REX.toString());
        }
    };
    public static final Set<String> ELECTRIC_VEHICLES = new HashSet<String>() {
        {
            add(VehicleType.ELECTRIC.toString());
            add(VehicleType.PLUGIN_HYBRID.toString());
            add(VehicleType.ELECTRIC_REX.toString());
        }
    };

    // Countries with Mileage display
    public static final Set<String> IMPERIAL_COUNTRIES = new HashSet<String>() {
        {
            add("US");
            add("GB");
        }
    };

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_CONNECTED_DRIVE_ACCOUNT = new ThingTypeUID(BINDING_ID, "account");
    public static final ThingTypeUID THING_TYPE_CONV = new ThingTypeUID(BINDING_ID,
            VehicleType.CONVENTIONAL.toString());
    public static final ThingTypeUID THING_TYPE_PHEV = new ThingTypeUID(BINDING_ID,
            VehicleType.PLUGIN_HYBRID.toString());
    public static final ThingTypeUID THING_TYPE_BEV_REX = new ThingTypeUID(BINDING_ID,
            VehicleType.ELECTRIC_REX.toString());
    public static final ThingTypeUID THING_TYPE_BEV = new ThingTypeUID(BINDING_ID, VehicleType.ELECTRIC.toString());
    public static final Set<ThingTypeUID> SUPPORTED_THING_SET = new HashSet<ThingTypeUID>() {
        {
            add(THING_TYPE_CONNECTED_DRIVE_ACCOUNT);
            add(THING_TYPE_CONV);
            add(THING_TYPE_PHEV);
            add(THING_TYPE_BEV_REX);
            add(THING_TYPE_BEV);
        }
    };

    // Thing Group definitions
    public static final String CHANNEL_GROUP_STATUS = "status";
    public static final String CHANNEL_GROUP_RANGE = "range";
    public static final String CHANNEL_GROUP_LOCATION = "location";
    public static final String CHANNEL_GROUP_LAST_TRIP = "last-trip";
    public static final String CHANNEL_GROUP_LIFETIME = "lifetime";
    public static final String CHANNEL_GROUP_REMOTE = "remote";
    public static final String CHANNEL_GROUP_CHARGE = "charge";
    public static final String CHANNEL_GROUP_VEHICLE_IMAGE = "image";
    public static final String CHANNEL_GROUP_DESTINATION = "destination";
    public static final String CHANNEL_GROUP_TROUBLESHOOT = "troubleshoot";

    // Status
    public static final String DOORS = "doors";
    public static final String WINDOWS = "windows";
    public static final String LOCK = "lock";
    public static final String SERVICE_DATE = "service-date";
    public static final String SERVICE_MILEAGE = "service-mileage";
    public static final String SERVICE_NAME = "service-name";
    public static final String CHECK_CONTROL = "check-control";
    public static final String LAST_UPDATE = "last-update";

    // Chraging
    public static final String CHARGE_STATUS = "status";
    public static final String CHARGE_PROFILE_CLIMATE = "profile-climate";
    public static final String CHARGE_PROFILE_MODE = "profile-mode";
    public static final String CHARGE_WINDOW_START = "window-start";
    public static final String CHARGE_WINDOW_END = "window-end";
    public static final String CHARGE_TIMER1_DEPARTURE = "timer1-departure";
    public static final String CHARGE_TIMER1_DAYS = "timer1-days";
    public static final String CHARGE_TIMER1_ENABLED = "timer1-enabled";
    public static final String CHARGE_TIMER2_DEPARTURE = "timer2-departure";
    public static final String CHARGE_TIMER2_DAYS = "timer2-days";
    public static final String CHARGE_TIMER2_ENABLED = "timer2-enabled";
    public static final String CHARGE_TIMER3_DEPARTURE = "timer3-departure";
    public static final String CHARGE_TIMER3_ENABLED = "timer3-days";
    public static final String CHARGE_TIMER3_DAYS = "timer3-enabled";

    // Range
    public static final String MILEAGE = "mileage";
    public static final String RANGE_HYBRID = "hybrid";
    public static final String RANGE_ELECTRIC = "electric";
    public static final String SOC = "soc";
    public static final String RANGE_FUEL = "fuel";
    public static final String REMAINING_FUEL = "remaining-fuel";
    public static final String RANGE_RADIUS_ELECTRIC = "radius-electric";
    public static final String RANGE_RADIUS_FUEL = "radius-fuel";
    public static final String RANGE_RADIUS_HYBRID = "radius-hybrid";

    // Lifetime
    public static final String AVG_CONSUMPTION = "average-consumption";
    public static final String AVG_RECUPERATION = "average-recuperation";
    public static final String CUMULATED_DRIVEN_DISTANCE = "cumulated-driven-distance";
    public static final String SINGLE_LONGEST_DISTANCE = "single-longest-distance";

    // Last Trip
    public static final String DISTANCE = "distance";
    public static final String DISTANCE_SINCE_CHARGING = "distance-since-charging";

    // Lifetime
    public static final String LONGITUDE = "longitude";
    public static final String LATITUDE = "latitude";
    public static final String HEADING = "heading";

    // Image
    public static final String IMAGE = "png";

    // Remote Services
    public static final String REMOTE_SERVICE_LIGHT_FLASH = "light";
    public static final String REMOTE_SERVICE_VEHICLE_FINDER = "finder";
    public static final String REMOTE_SERVICE_DOOR_LOCK = "lock";
    public static final String REMOTE_SERVICE_DOOR_UNLOCK = "unlock";
    public static final String REMOTE_SERVICE_HORN = "horn";
    public static final String REMOTE_SERVICE_AIR_CONDITIONING = "climate";
    public static final String REMOTE_STATE = "state";

    public static final String DESTINATION_NAME_1 = "name-1";
    public static final String DESTINATION_LAT_1 = "lat-1";
    public static final String DESTINATION_LON_1 = "lon-1";
    public static final String DESTINATION_NAME_2 = "name-2";
    public static final String DESTINATION_LAT_2 = "lat-2";
    public static final String DESTINATION_LON_2 = "lon-2";
    public static final String DESTINATION_NAME_3 = "name-3";
    public static final String DESTINATION_LAT_3 = "lat-3";
    public static final String DESTINATION_LON_3 = "lon-3";

    // Troubleshoot
    public static final String DISCOVERY_FINGERPRINT = "discovery-fingerprint";
    public static final String VEHICLE_FINGERPRINT = "vehicle-fingerprint";
}
