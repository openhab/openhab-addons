/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
 * The {@link VolvoOnCallBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class VolvoOnCallBindingConstants {

    public static final String BINDING_ID = "volvooncall";

    // Vehicle properties
    public static final String VIN = "vin";

    // The URL to use to connect to VocAPI with.
    // TODO : for North America and China syntax changes to vocapi-cn.xxx
    public static final String SERVICE_URL = "https://vocapi.wirelesscar.net/customerapi/rest/v3.0/";

    // The JSON content type used when talking to VocAPI.
    public static final String JSON_CONTENT_TYPE = "application/json";

    // List of Thing Type UIDs
    public static final ThingTypeUID APIBRIDGE_THING_TYPE = new ThingTypeUID(BINDING_ID, "vocapi");
    public static final ThingTypeUID VEHICLE_THING_TYPE = new ThingTypeUID(BINDING_ID, "vehicle");

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
    public static final String WASHER_FLUID = "washerFluidLevel";
    public static final String SERVICE_WARNING = "serviceWarningStatus";
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
}
