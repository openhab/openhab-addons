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
package org.openhab.binding.vwweconnect.internal;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link VWWeConnectBinding} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Jan Gustafsson - Initial contribution
 */
@NonNullByDefault
public class VWWeConnectBindingConstants {

    public static final String BINDING_ID = "vwweconnect";

    // List of Thing Type UIDs
    public static final ThingTypeUID BRIDGE_THING_TYPE = new ThingTypeUID(BINDING_ID, "vwweconnectapi");
    public static final ThingTypeUID VEHICLE_THING_TYPE = new ThingTypeUID(BINDING_ID, "vehicle");

    // List of all addressable things in OH = SUPPORTED_DEVICE_THING_TYPES_UIDS + the virtual bridge
    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Stream.of(BRIDGE_THING_TYPE, VEHICLE_THING_TYPE)
            .collect(Collectors.toSet());

    // Vehicle properties
    public static final String VIN = "vin";

    // List of Channel ID's
    // Doors
    public static final String TRUNK = "trunk";
    public static final String RIGHT_BACK = "rightBack";
    public static final String LEFT_BACK = "leftBack";
    public static final String RIGHT_FRONT = "rightFront";
    public static final String LEFT_FRONT = "leftFront";
    public static final String HOOD = "hood";
    public static final String DOORS_LOCKED = "doorsLocked";
    public static final String TRUNK_LOCKED = "trunkLocked";

    // Windows
    public static final String RIGHT_BACK_WND = "rightBackWnd";
    public static final String LEFT_BACK_WND = "leftBackWnd";
    public static final String RIGHT_FRONT_WND = "rightFrontWnd";
    public static final String LEFT_FRONT_WND = "leftFrontWnd";
    public static final String ROOF = "roof";
    public static final String SUN_ROOF = "sunroof";

    // Fuel
    public static final String FUEL_LEVEL = "fuelLevel";
    public static final String FUEL_CONSUMPTION = "fuelConsumption";
    public static final String FUEL_ALERT = "fuelAlert";
    public static final String FUEL_RANGE = "fuelRange";

    // Compressed Natural Gas
    public static final String CNG_LEVEL = "cngLevel";
    public static final String CNG_CONSUMPTION = "cngConsumption";
    public static final String CNG_ALERT = "cngAlert";
    public static final String CNG_RANGE = "cngRange";

    // Electric/hybrid
    public static final String BATTERY_LEVEL = "batteryLevel";
    public static final String ELECTRIC_CONSUMPTION = "electricConsumption";
    public static final String BATTERY_ALERT = "batteryAlert";
    public static final String BATTERY_RANGE = "batteryRange";
    public static final String CHARGING_STATE = "chargingState";
    public static final String CHARGING_REMAINING_HOUR = "chargingRemainingHour";
    public static final String CHARGING_REMAINING_MINUTE = "chargingRemainingMinute";
    public static final String CHARGING_REASON = "chargingReason";
    public static final String PLUGIN_STATE = "pluginState";
    public static final String LOCK_STATE = "lockState";
    public static final String EXTERNAL_POWER_SUPPLY_STATE = "extPowerSupplyState";
    public static final String CHARGER_MAX_CURRENT = "chargerMaxCurrent";
    public static final String MAX_AMPERE = "maxAmpere";
    public static final String MAX_CURRENT_REDUCED = "maxCurrentReduced";
    public static final String CLIMATISATION_STATE = "climatisationState";
    public static final String CLIMATISATION_REMAINING_TIME = "climatisationRemainingTime";
    public static final String CLIMATISATION_REASON = "climatisationReason";
    public static final String WINDOW_HEATING_STATE_FRONT = "windowHeatingStateFront";
    public static final String WINDOW_HEATING_STATE_REAR = "windowHeatingStateRear";

    // Location
    public static final String ACTUAL_LOCATION = "location";

    // Odometer
    public static final String TOTAL_DISTANCE = "totalDistance";
    public static final String TOTAL_AVERAGE_SPEED = "totalAverageSpeed";
    public static final String TOTAL_TRIP_DISTANCE = "totalTripDistance";
    public static final String TOTAL_TRIP_DURATION = "totalTripDuration";

    // Last Trip
    public static final String LAST_TRIP_GROUP = "lasttrip";
    public static final String AVERAGE_FUEL_CONSUMPTION = "averageFuelConsumption";
    public static final String AVERAGE_CNG_CONSUMPTION = "averageCngConsumption";
    public static final String AVERAGE_ELECTRIC_CONSUMPTION = "averageElectricConsumption";
    public static final String AVERAGE_AUXILIARY_CONSUMPTION = "averageAuxiliaryConsumption";
    public static final String TRIP_AVERAGE_SPEED = "tripAverageSpeed";
    public static final String TRIP_DISTANCE = "tripDistance";
    public static final String TRIP_DURATION = "tripDuration";
    public static final String TRIP_START_TIME = "tripStartTime";
    public static final String TRIP_END_TIME = "tripEndTime";

    // Vehicle properties
    // public static final String HONK = "honk";
    // public static final String BLINK = "blink";
    // public static final String HONK_BLINK = "honkAndBlink";
    // public static final String HONK_AND_OR_BLINK = "honkAndOrBlink";

    // Actions
    public static final String REMOTE_HEATER = "remoteHeater";
    public static final String REMOTE_VENTILATION = "remoteVentilation";
    public static final String REMAINING_TIME = "remainingTime";
    public static final String TEMPERATURE = "temperature";
    public static final String HEATING = "HEATING";
    public static final String VENTILATION = "VENTILATION";

    // Actions Electric/hybrid
    public static final String EMANAGER_CHARGE = "emanagerCharge";
    public static final String EMANAGER_CLIMATE = "emanagerClimate";
    public static final String EMANAGER_WINDOW_HEAT = "emanagerWindowHeat";

    // Vehicle details
    public static final String NAME = "name";
    public static final String MODEL = "model";
    public static final String MODEL_CODE = "modelCode";
    public static final String MODEL_YEAR = "modelYear";
    public static final String ENROLLMENT_DATE = "enrollmentDate";
    public static final String DASHBOARD_URL = "dashboardURL";
    public static final String IMAGE_URL = "imageURL";
    public static final String ENGINE_TYPE_COMBUSTIAN = "engineTypeCombustian";
    public static final String ENGINE_TYPE_ELECTRIC = "engineTypeElectic";
    public static final String ENGINE_TYPE_HYBRID_OCU1 = "engineTypeHybridOCU1";
    public static final String ENGINE_TYPE_HYBRID_OCU2 = "engineTypeHybridOCU2";
    public static final String ENGINE_TYPE_CNG = "engineTypeCNG";
    public static final String SERVICE_INSPECTION = "serviceInspectionStatus";
    public static final String OIL_INSPECTION = "oilInspectionStatus";
    public static final String LAST_TRIP_ID = "lastTripId";

    // Status
    public static final String CHANNEL_STATUS = "status";

    // Common API URLs
    public static final String REQUEST_LANDING_PAGE = "/portal/en_GB/web/guest/home";
    public static final String GET_LOGIN_URL = "portal/web/guest/home/-/csrftokenhandling/get-login-url";
    public static final String GET_FULLY_LOADED_CARS = "-/mainnavigation/get-fully-loaded-cars";
    public static final String LOGIN_CHECK = "-/msgc/get-new-messages";
    public static final String VEHICLE_DETAILS = "/-/mainnavigation/load-car-details/";
    public static final String VEHICLE_DETAILS_SPECIFIC = "/-/vehicle-info/get-vehicle-details";
    public static final String REQUEST_VEHICLE_STATUS_REPORT = "/-/vsr/request-vsr";
    public static final String VEHICLE_STATUS = "/-/vsr/get-vsr";
    public static final String TRIP_STATISTICS = "/-/rts/get-latest-trip-statistics";
    public static final String VEHICLE_LOCATION = "/-/cf/get-location";
    public static final String REQUEST_STATUS_LOCK_ACTION = "/-/vsr/get-request-status";
    public static final String REQUEST_STATUS_HEATER_ACTION = "/-/rah/get-request-status";
    public static final String GET_HEATER_STATUS = "/-/rah/get-status";

    // Action URLs
    public static final String SET_TIMEZONE = "/-/mainnavigation/set-time-zone";
    public static final String LOCKUNLOCK = "/-/vsr/remote-";
    public static final String LOCK = "lock";
    public static final String UNLOCK = "unlock";
    public static final String STARTSTOP_HEATER = "/-/rah/quick-";
    public static final String START_HEATER = "start";
    public static final String STOP_HEATER = "stop";

    // Electric/hybrid car URLs
    public static final String EMANAGER_GET_EMANAGER = "/-/emanager/get-emanager";
    public static final String EMANAGER_GET_NOTIFICATIONS = "/-/emanager/get-notifications";

    // Electric/hybrid car Action URLs
    public static final String CHARGE_BATTERY = "/-/emanager/charge-battery";
    public static final String TRIGGER_CLIMATISATION = "/-/emanager/trigger-climatisation";
    public static final String TRIGGER_WINDOW_HEAT = "/-/emanager/trigger-windowheating";

    // JSON constants
    public static final String PARSE_REQUEST_STATUS = "$.rluRequestStatus.status";
    public static final String VEHICLES_NOT_FULLY_LOADED = "$['fullyLoadedVehiclesResponse']['vehiclesNotFullyLoaded'][*]";
    public static final String COMPLETE_VEHICLES = "$['fullyLoadedVehiclesResponse']['completeVehicles'][*]";
    public static final String REQUEST_IN_PROGRESS = "REQUEST_IN_PROGRESS";
    public static final String REQUEST_SUCCESSFUL = "REQUEST_SUCCESSFUL";

    // REST URI constants
    public static final String USERNAME = "username";
    public static final String PASSWORD = "password";
    public static final String SESSION_BASE = "https://www.portal.volkswagen-we.com/";
    public static final String SESSION_HEADERS = "'Accept': 'application/json, text/plain, */*', 'Content-Type': 'application/json;charset=UTF-8', 'User-Agent': 'Mozilla/5.0 (Linux; Android 6.0.1; D5803 Build/23.5.A.1.291; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/63.0.3239.111 Mobile Safari/537.36'";
    public static final String AUTH_BASE = "https://identity.vwgroup.io";
    public static final String AUTH_HEADERS = "'Accept': 'text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3', 'Content-Type': 'application/x-www-form-urlencoded', 'User-Agent': 'Mozilla/5.0 (Linux; Android 6.0.1; D5803 Build/23.5.A.1.291; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/63.0.3239.111 Mobile Safari/537.36'";
    public static final String COOKIESTORE = "www.portal.volkswagen-we.com";

    public static final int SLEEP_TIME_MILLIS = 1000;
    public static final int MAX_WAIT_MILLIS = 30000;
    public static final int MAX_IMAGE_SIZE = 1000000;
    public static final int REFRESH_DELAY_SECONDS = 60;
}
