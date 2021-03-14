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
package org.openhab.binding.groupepsa.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link GroupePSABindingConstants} class defines common constants, which
 * are used across the whole binding.
 *
 * @author Arjan Mels - Initial contribution
 */
@NonNullByDefault
public class GroupePSABindingConstants {

    public static final String BINDING_ID = "groupepsa";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_BRIDGE = new ThingTypeUID(BINDING_ID, "bridge");
    public static final ThingTypeUID THING_TYPE_VEHICLE = new ThingTypeUID(BINDING_ID, "vehicle");

    // Vehicle properties
    public static final String VEHICLE_ID = "id";
    public static final String VEHICLE_VIN = "vin";
    public static final String VEHICLE_VENDOR = "vendor";
    public static final String VEHICLE_MODEL = "model";

    // List of all Channel ids
    public static final String CHANNEL_BATTERY_CURRENT = "battery#current";
    public static final String CHANNEL_BATTERY_VOLTAGE = "battery#voltage";

    public static final String CHANNEL_TYPE_DOORLOCK = "doorlock";
    public static final String CHANNEL_TYPE_DOOROPEN = "dooropen";

    public static final String CHANNEL_GROUP_DOORS = "doors";
    public static final String CHANNEL_DOORS_LOCK = "doors#locked";
    public static final String CHANNEL_DOORS_DRIVER = "doors#driver";
    public static final String CHANNEL_DOORS_PASSENGER = "doors#passenger";
    public static final String CHANNEL_DOORS_REARLEFT = "doors#rearleft";
    public static final String CHANNEL_DOORS_REARRIGHT = "doors#rearright";
    public static final String CHANNEL_DOORS_TRUNK = "doors#trunk";
    public static final String CHANNEL_DOORS_REARWINDOW = "doors#rearwindow";
    public static final String CHANNEL_DOORS_ROOFWINDOW = "doors#roofwindow";

    public static final String CHANNEL_ENVIRONMENT_TEMPERATURE = "environment#temperature";
    public static final String CHANNEL_ENVIRONMENT_DAYTIME = "environment#daytime";

    public static final String CHANNEL_MOTION_IGNITION = "motion#ignition";
    public static final String CHANNEL_MOTION_ACCELERATION = "motion#acceleration";
    public static final String CHANNEL_MOTION_MOVING = "motion#moving";
    public static final String CHANNEL_MOTION_SPEED = "motion#speed";
    public static final String CHANNEL_MOTION_MILEAGE = "motion#mileage";

    public static final String CHANNEL_POSITION_POSITION = "position#position";
    public static final String CHANNEL_POSITION_HEADING = "position#heading";
    public static final String CHANNEL_POSITION_TYPE = "position#type";
    public static final String CHANNEL_POSITION_SIGNALSTRENGTH = "position#signal";

    public static final String CHANNEL_VARIOUS_LASTUPDATED = "various#lastupdated";
    public static final String CHANNEL_VARIOUS_PRIVACY = "various#privacy";
    public static final String CHANNEL_VARIOUS_BELT = "various#belt";
    public static final String CHANNEL_VARIOUS_EMERGENCY = "various#emergency";
    public static final String CHANNEL_VARIOUS_SERVICE = "various#service";
    public static final String CHANNEL_VARIOUS_PRECONDITINING = "various#preconditioning";
    public static final String CHANNEL_VARIOUS_PRECONDITININGFAILURE = "various#preconditioningfailure";

    public static final String CHANNEL_FUEL_AUTONOMY = "fuel#autonomy";
    public static final String CHANNEL_FUEL_CONSUMPTION = "fuel#consumption";
    public static final String CHANNEL_FUEL_LEVEL = "fuel#level";

    public static final String CHANNEL_ELECTRIC_AUTONOMY = "electric#autonomy";
    public static final String CHANNEL_ELECTRIC_LEVEL = "electric#level";
    public static final String CHANNEL_ELECTRIC_RESIDUAL = "electric#residual";

    public static final String CHANNEL_ELECTRIC_BATTERY_CAPACITY = "electric#battery_capacity";
    public static final String CHANNEL_ELECTRIC_BATTERY_HEALTH_CAPACITY = "electric#battery_health_capacity";
    public static final String CHANNEL_ELECTRIC_BATTERY_HEALTH_RESISTANCE = "electric#battery_health_resistance";

    public static final String CHANNEL_ELECTRIC_CHARGING_STATUS = "electric#charging_status";
    public static final String CHANNEL_ELECTRIC_CHARGING_MODE = "electric#charging_mode";
    public static final String CHANNEL_ELECTRIC_CHARGING_PLUGGED = "electric#charging_plugged";
    public static final String CHANNEL_ELECTRIC_CHARGING_RATE = "electric#charging_rate";
    public static final String CHANNEL_ELECTRIC_CHARGING_REMAININGTIME = "electric#charging_remainingTime";
    public static final String CHANNEL_ELECTRIC_CHARGING_NEXTDELAYEDTIME = "electric#charging_nextDelayedTime";

    public enum VendorConstants {
        PEUGEOT("https://idpcvs.peugeot.com/am/oauth2/access_token", "clientsB2CPeugeot"),
        CITROEN("https://idpcvs.opel.com/am/oauth2/access_token", "clientsB2CCitroen"),
        DS("https://idpcvs.driveds.com/am/oauth2/access_token", "clientsB2CDS"),
        OPEL("https://idpcvs.opel.com/am/oauth2/access_token", "clientsB2COpel"),
        VAUXHALL("https://idpcvs.vauxhall.co.uk/am/oauth2/access_token", "clientsB2CVauxhall");

        public final String OAUTH_URL;
        public final String OAUTH_REALM;
        public final String OAUTH_SCOPE;

        VendorConstants(String oauth_url, String oauth_realm) {
            this.OAUTH_URL = oauth_url;
            this.OAUTH_REALM = oauth_realm;
            this.OAUTH_SCOPE = "profile openid";
        }
    }

    public static final String API_URL = "https://api.groupe-psa.com/connectedcar/v4";
}
