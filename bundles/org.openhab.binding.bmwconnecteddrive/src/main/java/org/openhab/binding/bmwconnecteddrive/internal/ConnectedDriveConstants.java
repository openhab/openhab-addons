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
@NonNullByDefault
public class ConnectedDriveConstants {

    private static final String BINDING_ID = "bmwconnecteddrive";

    // Units
    public static final String UNITS_AUTODETECT = "AUTODETECT";
    public static final String UNITS_IMPERIAL = "IMPERIAL";
    public static final String UNITS_METRIC = "METRIC";

    // See constants from bimmer-connected
    // https://github.com/bimmerconnected/bimmer_connected/blob/master/bimmer_connected/vehicle.py
    public enum CarType {
        CONVENTIONAL("CONV"),
        PLUGIN_HYBRID("PHEV"),
        ELECTRIC_REX("BEV_REX"),
        ELECTRIC("BEV");

        private final String type;

        CarType(String s) {
            type = s;
        }

        @Override
        public String toString() {
            return type;
        }
    }

    public static final Set<String> FUEL_CARS = new HashSet<String>() {
        {
            add(CarType.CONVENTIONAL.toString());
            add(CarType.PLUGIN_HYBRID.toString());
            add(CarType.ELECTRIC_REX.toString());
        }
    };
    public static final Set<String> ELECTRIC_CARS = new HashSet<String>() {
        {
            add(CarType.ELECTRIC.toString());
            add(CarType.PLUGIN_HYBRID.toString());
            add(CarType.ELECTRIC_REX.toString());
        }
    };

    // Countries with Mileage display
    public static final Set<String> MILE_COUNTRIES = new HashSet<String>() {
        {
            add("US");
            add("GB");
        }
    };

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_CONNECTED_DRIVE_ACCOUNT = new ThingTypeUID(BINDING_ID, "account");
    public static final ThingTypeUID THING_TYPE_CONV = new ThingTypeUID(BINDING_ID, CarType.CONVENTIONAL.toString());
    public static final ThingTypeUID THING_TYPE_PHEV = new ThingTypeUID(BINDING_ID, CarType.PLUGIN_HYBRID.toString());
    public static final ThingTypeUID THING_TYPE_BEV_REX = new ThingTypeUID(BINDING_ID, CarType.ELECTRIC_REX.toString());
    public static final ThingTypeUID THING_TYPE_BEV = new ThingTypeUID(BINDING_ID, CarType.ELECTRIC.toString());
    public static final Set<ThingTypeUID> SUPPORTED_THING_SET = new HashSet<ThingTypeUID>() {
        {
            add(THING_TYPE_CONNECTED_DRIVE_ACCOUNT);
            add(THING_TYPE_CONV);
            add(THING_TYPE_PHEV);
            add(THING_TYPE_BEV_REX);
            add(THING_TYPE_BEV);
        }
    };

    // Bridge Channel
    public static final String DISCOVERY_FINGERPRINT = "discovery-fingerprint";

    // Thing Group definitions
    public static final String CHANNEL_GROUP_STATUS = "status";
    public static final String CHANNEL_GROUP_RANGE = "range";
    public static final String CHANNEL_GROUP_LIFETIME = "lifetime";
    public static final String CHANNEL_GROUP_LAST_TRIP = "last-trip";
    public static final String CHANNEL_GROUP_CAR_STATUS = "status";
    public static final String CHANNEL_GROUP_LOCATION = "location";
    public static final String CHANNEL_GROUP_CAR_IMAGE = "image";
    public static final String CHANNEL_GROUP_REMOTE = "remote";
    public static final String CHANNEL_GROUP_CHARGE_PROFILE = "charge-profile";
    public static final String CHANNEL_GROUP_RANGE_MAP = "range-map";
    public static final String CHANNEL_GROUP_TROUBLESHOOT = "troubleshoot";

    public static final String PROPERTIES_BRAND = "brand";
    public static final String PROPERTIES_MODEL = "model";
    public static final String PROPERTIES_DRIVETRAIN = "drivetrain";
    public static final String PROPERTIES_BODYTYPE = "body";
    public static final String PROPERTIES_COLOR = "color";
    public static final String PROPERTIES_CONSTRUCTION_YEAR = "construction-year";
    public static final String PROPERTIES_COMMUNITY = "community-statistics";
    public static final String PROPERTIES_ALARM = "alarm";
    public static final String PROPERTIES_DEALER_NAME = "dealer-name";
    public static final String PROPERTIES_DEALER_ADDRESS = "dealer-address";
    public static final String PROPERTIES_DEALER_PHONE = "dealer-phone";
    public static final String PROPERTIES_BREAKDOWN_PHONE = "breakdown-phone";
    public static final String PROPERTIES_ACTIVATED_SERVICES = "activated-services";
    public static final String PROPERTIES_DEACTIVATED_SERVICES = "deactivated-services";
    public static final String PROPERTIES_SUPPORTED_SERVICES = "supported-services";
    public static final String PROPERTIES_NOT_SUPPORTED_SERVICES = "not-supported-services";
    public static final String PROPERTIES_CHARGING_MODES = "charging-modes";

    // Status
    public static final String DOORS = "doors";
    public static final String WINDOWS = "windows";
    public static final String LOCK = "lock";
    public static final String SERVICE = "service";
    public static final String CHECK_CONTROL = "check-control";
    public static final String CHARGING_STATUS = "charging-status";

    // Range
    public static final String MILEAGE = "mileage";
    public static final String REMAINING_RANGE_HYBRID = "remaining-range-hybrid";
    public static final String REMAINING_RANGE_ELECTRIC = "remaining-range-electric";
    public static final String REMAINING_SOC = "remaining-soc";
    public static final String REMAINING_RANGE_FUEL = "remaining-range-fuel";
    public static final String REMAINING_FUEL = "remaining-fuel";
    public static final String LAST_UPDATE = "last-update";

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
    public static final String LATLONG = "latlong";
    public static final String HEADING = "heading";
    public static final String RANGE_RADIUS = "range-radius";

    // Image
    public static final String IMAGE = "rendered";

    // Remote Services
    public static final String REMOTE_SERVICE_LIGHT_FLASH = "light";
    public static final String REMOTE_SERVICE_VEHICLE_FINDER = "finder";
    public static final String REMOTE_SERVICE_DOOR_LOCK = "lock";
    public static final String REMOTE_SERVICE_DOOR_UNLOCK = "unlock";
    public static final String REMOTE_SERVICE_HORN = "horn";
    public static final String REMOTE_SERVICE_AIR_CONDITIONING = "climate";
    public static final String REMOTE_STATE = "state";

    // Troubleshoot
    public static final String CARDATA_FINGERPRINT = "cardata-fingerprint";

}
