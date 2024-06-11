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
package org.openhab.binding.teslascope.internal;

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
    public static final ThingTypeUID THING_TYPE_SAMPLE = new ThingTypeUID(BINDING_ID, "service");

    // List of all Channel ids
    public static final String CHANNEL_VIN = "vin";
    public static final String CHANNEL_VEHICLENAME = "vehiclename";
    public static final String CHANNEL_VEHICLESTATE = "vehiclestate";
    public static final String CHANNEL_ODOMETER = "odometer";
    public static final String CHANNEL_BATTERYLEVEL = "batterylevel";
    public static final String CHANNEL_CHARGINGSTATE = "chargingstate";
    public static final String CHANNEL_TPMSFL = "tpms_pressure_fl";
    public static final String CHANNEL_TPMSFR = "tpms_pressure_fr";
    public static final String CHANNEL_TPMSRL = "tpms_pressure_rl";
    public static final String CHANNEL_TPMSRR = "tpms_pressure_rr";
    public static final String CHANNEL_SOFTWAREUPDATESTATUS = "softwareupdatestatus";
    public static final String CHANNEL_SOFTWAREUPDATEVERSION = "softwareupdateversion";
    public static final String CHANNEL_AUTOCONDITIONING = "autoconditioning";
    public static final String CHANNEL_BATTERYRANGE = "batteryrange";
    public static final String CHANNEL_CENTERREARSEATHEATER = "centerrearseatheater";
    public static final String CHANNEL_CHARGE = "charge";
    public static final String CHANNEL_CHARGELIMITSOCSTANDARD = "chargelimitsocstandard";
    public static final String CHANNEL_CHARGERATE = "chargerate";
    public static final String CHANNEL_CHARGERPOWER = "chargerpower";
    public static final String CHANNEL_CHARGERVOLTAGE = "chargervoltage";
    public static final String CHANNEL_CLIMATE = "climate";
    public static final String CHANNEL_DOORLOCK = "doorlock";
    public static final String CHANNEL_DRIVERFRONTDOOR = "driverfrontdoor";
    public static final String CHANNEL_DRIVERREARDOOR = "driverreardoor";
    public static final String CHANNEL_DRIVERTEMP = "drivertemp";
    public static final String CHANNEL_ESTIMATEDBATTERYRANGE = "estimatedbatteryrange";
    public static final String CHANNEL_ESTIMATEDRANGE = "estimatedrange";
    public static final String CHANNEL_FAN = "fan";
    public static final String CHANNEL_FLASHLIGHTS = "flashlights";
    public static final String CHANNEL_FRONTDEFROSTER = "frontdefroster";
    public static final String CHANNEL_FRONTTRUNK = "fronttrunk";
    public static final String CHANNEL_HEADING = "heading";
    public static final String CHANNEL_HOMELINK = "homelink";
    public static final String CHANNEL_HONKHORN = "honkhorn";
    public static final String CHANNEL_INSIDETEMP = "insidetemp";
    public static final String CHANNEL_LEFTREARSEATHEATER = "leftrearseatheater";
    public static final String CHANNEL_LEFSEATHEATER = "leftseatheater";
    public static final String CHANNEL_LEFTTEMPDIRECTION = "lefttempdirection";
    public static final String CHANNEL_LOCATION = "location";
    public static final String CHANNEL_MINAVAILABLETEMP = "minavailabletemp";
    public static final String CHANNEL_MAXAVAILABLETEMP = "maxavailabletemp";
    public static final String CHANNEL_OUTSIDETEMP = "outsidetemp";
    public static final String CHANNEL_PASSENGERFRONTDOOR = "passengerfrontdoor";
    public static final String CHANNEL_PASSENGERREARDOOR = "passengerreardoor";
    public static final String CHANNEL_PASSENGERTEMP = "passengertemp";
    public static final String CHANNEL_POWER = "power";
    public static final String CHANNEL_PRECONDITIONING = "preconditioning";
    public static final String CHANNEL_REARDEFROSTER = "reardefroster";
    public static final String CHANNEL_REARTRUNK = "reartrunk";
    public static final String CHANNEL_RIGHTREARSEATHEATER = "rightrearseatheater";
    public static final String CHANNEL_RIGHTSEATHEATER = "rightseatheater";
    public static final String CHANNEL_RIGHTTEMPDIRECTION = "righttempdirection";
    public static final String CHANNEL_SCHEDULEDCHARGINGPENDING = "scheduledchargingpending";
    public static final String CHANNEL_SCHEDULEDCHARGINGSTART = "scheduledchargingstart";
    public static final String CHANNEL_SENTRYMODE = "sentrymode";
    public static final String CHANNEL_SHIFTSTATE = "shiftstate";
    public static final String CHANNEL_SIDEMIRRORHEATERS = "sidemirrorheaters";
    public static final String CHANNEL_SMARTPRECONDITIONG = "smartpreconditioning";
    public static final String CHANNEL_SPEED = "speed";
    public static final String CHANNEL_STEERINGWHEELHEATER = "steeringwheelheater";
    public static final String CHANNEL_SUNROOF = "sunroof";
    public static final String CHANNEL_SUNROOFSTATE = "sunroofstate";
    public static final String CHANNEL_TIMETOFULLCHARGE = "timetofullcharge";
    public static final String CHANNEL_USABLEBATTERYLEVEL = "usablebatterylevel";
    public static final String CHANNEL_VALETMODE = "valetmode";
    public static final String CHANNEL_WIPERBLADEHEATER = "wiperbladeheater";
}
