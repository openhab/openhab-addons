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
package org.openhab.binding.volvooncall.internal;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link VolvoOnCallBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class VolvoOnCallBindingConstants {

    public static final String BINDING_ID = "volvooncall";

    // List of Thing Type UIDs
    public static final ThingTypeUID APIBRIDGE_THING_TYPE = new ThingTypeUID(BINDING_ID, "vocapi");
    public static final ThingTypeUID VEHICLE_THING_TYPE = new ThingTypeUID(BINDING_ID, "vehicle");

    // List of Channel groups
    public static final String GROUP_DOORS = "doors";
    public static final String GROUP_WINDOWS = "windows";
    public static final String GROUP_TYRES = "tyrePressure";
    public static final String GROUP_BATTERY = "battery";
    public static final String GROUP_OTHER = "other";
    public static final String GROUP_POSITION = "position";
    public static final String GROUP_ODOMETER = "odometer";
    public static final String GROUP_TANK = "tank";

    // List of Channel id's
    public static final String TAILGATE = "tailgate";
    public static final String REAR_RIGHT = "rearRight";
    public static final String REAR_LEFT = "rearLeft";
    public static final String FRONT_RIGHT = "frontRight";
    public static final String FRONT_LEFT = "frontLeft";
    public static final String HOOD = "hood";
    public static final String REAR_RIGHT_WND = "rearRightWnd";
    public static final String REAR_LEFT_WND = "rearLeftWnd";
    public static final String FRONT_RIGHT_WND = "frontRightWnd";
    public static final String FRONT_LEFT_WND = "frontLeftWnd";
    public static final String REAR_RIGHT_TYRE = "rearRightTyre";
    public static final String REAR_LEFT_TYRE = "rearLeftTyre";
    public static final String FRONT_RIGHT_TYRE = "frontRightTyre";
    public static final String FRONT_LEFT_TYRE = "frontLeftTyre";
    public static final String ODOMETER = "odometer";
    public static final String TRIPMETER1 = "tripmeter1";
    public static final String TRIPMETER2 = "tripmeter2";
    public static final String DISTANCE_TO_EMPTY = "distanceToEmpty";
    public static final String FUEL_AMOUNT = "fuelAmount";
    public static final String FUEL_LEVEL = "fuelLevel";
    public static final String FUEL_CONSUMPTION = "fuelConsumption";
    public static final String FUEL_ALERT = "fuelAlert";
    public static final String CALCULATED_LOCATION = "calculatedLocation";
    public static final String ACTUAL_LOCATION = "location";
    public static final String LOCATION_TIMESTAMP = "locationTimestamp";
    public static final String HEADING = "heading";
    public static final String CAR_LOCKED = "carLocked";
    public static final String ENGINE_RUNNING = "engineRunning";
    public static final String BRAKE_FLUID_LEVEL = "brakeFluidLevel";
    public static final String WASHER_FLUID_LEVEL = "washerFluidLevel";
    public static final String AVERAGE_SPEED = "averageSpeed";
    public static final String SERVICE_WARNING = "serviceWarningStatus";
    public static final String BATTERY_LEVEL = "batteryLevel";
    public static final String BATTERY_LEVEL_RAW = "batteryLevelRaw";
    public static final String BATTERY_DISTANCE_TO_EMPTY = "batteryDistanceToEmpty";
    public static final String CHARGE_STATUS = "chargeStatus";
    public static final String CHARGE_STATUS_CABLE = "chargeStatusCable";
    public static final String CHARGE_STATUS_CHARGING = "chargeStatusCharging";
    public static final String CHARGE_STATUS_FULLY_CHARGED = "chargeStatusFullyCharged";
    public static final String TIME_TO_BATTERY_FULLY_CHARGED = "timeToHVBatteryFullyCharged";
    public static final String CHARGING_END = "chargingEnd";
    public static final String BULB_FAILURE = "bulbFailure";

    // Car Events
    public static final String CAR_EVENT = "carEvent";
    public static final String EVENT_CAR_STOPPED = "CAR_STOPPED";
    public static final String EVENT_CAR_MOVED = "CAR_MOVED";
    public static final String EVENT_CAR_STARTED = "CAR_STARTED";
    // Last Trip Channel Id's
    public static final String LAST_TRIP_GROUP = "lasttrip";
    public static final String TRIP_CONSUMPTION = "tripConsumption";
    public static final String TRIP_DISTANCE = "tripDistance";
    public static final String TRIP_DURATION = "tripDuration";
    public static final String TRIP_START_TIME = "tripStartTime";
    public static final String TRIP_END_TIME = "tripEndTime";
    public static final String TRIP_START_ODOMETER = "tripStartOdometer";
    public static final String TRIP_STOP_ODOMETER = "tripStopOdometer";
    public static final String TRIP_START_POSITION = "startPosition";
    public static final String TRIP_END_POSITION = "endPosition";

    // Optional Channels depends upon car version
    public static final String CAR_LOCATOR = "carLocator";
    public static final String JOURNAL_LOG = "journalLog";

    // Car properties
    public static final String ENGINE_START = "engineStart";
    public static final String UNLOCK = "unlock";
    public static final String UNLOCK_TIME = "unlockTimeFrame";
    public static final String LOCK = "lock";
    public static final String HONK = "honk";
    public static final String BLINK = "blink";
    public static final String HONK_BLINK = "honkAndBlink";
    public static final String HONK_AND_OR_BLINK = "honkAndOrBlink";
    public static final String REMOTE_HEATER = "remoteHeater";
    public static final String PRECLIMATIZATION = "preclimatization";
    public static final String LAST_TRIP_ID = "lastTripId";

    // List of all adressable things in OH = SUPPORTED_DEVICE_THING_TYPES_UIDS + the virtual bridge
    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Stream
            .of(APIBRIDGE_THING_TYPE, VEHICLE_THING_TYPE).collect(Collectors.toSet());

    // Default value for undefined integers
    public static final int UNDEFINED = -1;
}
