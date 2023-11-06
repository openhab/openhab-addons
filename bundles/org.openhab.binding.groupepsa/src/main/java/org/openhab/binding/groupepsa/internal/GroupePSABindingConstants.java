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

    public static final String CHANNEL_TYPE_DOORLOCK = "doorLock";
    public static final String CHANNEL_TYPE_DOOROPEN = "doorOpen";

    public static final String CHANNEL_GROUP_DOORS = "doors";
    public static final String CHANNEL_DOORS_LOCK = "doors#locked";

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

    public static final String CHANNEL_VARIOUS_LAST_UPDATED = "various#lastUpdated";
    public static final String CHANNEL_VARIOUS_PRIVACY = "various#privacy";
    public static final String CHANNEL_VARIOUS_BELT = "various#belt";
    public static final String CHANNEL_VARIOUS_EMERGENCY = "various#emergency";
    public static final String CHANNEL_VARIOUS_SERVICE = "various#service";
    public static final String CHANNEL_VARIOUS_PRECONDITINING = "various#preconditioning";
    public static final String CHANNEL_VARIOUS_PRECONDITINING_FAILURE = "various#preconditioningFailure";

    public static final String CHANNEL_FUEL_AUTONOMY = "fuel#autonomy";
    public static final String CHANNEL_FUEL_CONSUMPTION = "fuel#consumption";
    public static final String CHANNEL_FUEL_LEVEL = "fuel#level";

    public static final String CHANNEL_ELECTRIC_AUTONOMY = "electric#autonomy";
    public static final String CHANNEL_ELECTRIC_LEVEL = "electric#level";
    public static final String CHANNEL_ELECTRIC_RESIDUAL = "electric#residual";

    public static final String CHANNEL_ELECTRIC_BATTERY_CAPACITY = "electric#batteryCapacity";
    public static final String CHANNEL_ELECTRIC_BATTERY_HEALTH_CAPACITY = "electric#batteryHealthCapacity";
    public static final String CHANNEL_ELECTRIC_BATTERY_HEALTH_RESISTANCE = "electric#batteryHealthResistance";

    public static final String CHANNEL_ELECTRIC_CHARGING_STATUS = "electric#chargingStatus";
    public static final String CHANNEL_ELECTRIC_CHARGING_MODE = "electric#chargingMode";
    public static final String CHANNEL_ELECTRIC_CHARGING_PLUGGED = "electric#chargingPlugged";
    public static final String CHANNEL_ELECTRIC_CHARGING_RATE = "electric#chargingRate";
    public static final String CHANNEL_ELECTRIC_CHARGING_REMAININGTIME = "electric#chargingRemainingTime";
    public static final String CHANNEL_ELECTRIC_CHARGING_NEXTDELAYEDTIME = "electric#chargingNextDelayedTime";

    public enum VendorConstants {
        PEUGEOT("https://idpcvs.peugeot.com/am/oauth2/access_token", "clientsB2CPeugeot"),
        CITROEN("https://idpcvs.citroen.com/am/oauth2/access_token", "clientsB2CCitroen"),
        DS("https://idpcvs.driveds.com/am/oauth2/access_token", "clientsB2CDS"),
        OPEL("https://idpcvs.opel.com/am/oauth2/access_token", "clientsB2COpel"),
        VAUXHALL("https://idpcvs.vauxhall.co.uk/am/oauth2/access_token", "clientsB2CVauxhall");

        public final String url;
        public final String realm;
        public final String scope;

        VendorConstants(String url, String realm) {
            this.url = url;
            this.realm = realm;
            this.scope = "profile openid";
        }
    }

    public static final String API_URL = "https://api.groupe-psa.com/connectedcar/v4";
}
