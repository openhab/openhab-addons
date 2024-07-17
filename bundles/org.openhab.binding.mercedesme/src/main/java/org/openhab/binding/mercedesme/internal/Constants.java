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
package org.openhab.binding.mercedesme.internal;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.measure.Unit;
import javax.measure.quantity.Energy;
import javax.measure.quantity.Length;
import javax.measure.quantity.Power;
import javax.measure.quantity.Pressure;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.library.unit.MetricPrefix;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.ThingTypeUID;

/**
 * {@link Constants} defines common constants, which are
 * used across the whole binding.
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public class Constants {
    public static final String BINDING_VERSION = "oh-release";
    public static final String BINDING_ID = "mercedesme";

    public static final String COMBUSTION = "combustion";
    public static final String HYBRID = "hybrid";
    public static final String BEV = "bev";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_ACCOUNT = new ThingTypeUID(BINDING_ID, "account");
    public static final ThingTypeUID THING_TYPE_COMB = new ThingTypeUID(BINDING_ID, COMBUSTION);
    public static final ThingTypeUID THING_TYPE_HYBRID = new ThingTypeUID(BINDING_ID, HYBRID);
    public static final ThingTypeUID THING_TYPE_BEV = new ThingTypeUID(BINDING_ID, BEV);

    public static final int REQUEST_TIMEOUT_MS = 10_000;

    public static final Set<ThingTypeUID> DISCOVERABLE_DEVICE_TYPE_UIDS = Collections
            .unmodifiableSet(Stream.of(THING_TYPE_COMB, THING_TYPE_HYBRID, THING_TYPE_BEV).collect(Collectors.toSet()));

    public static final String MB_KEY_TIRE_SENSOR_AVAILABLE = "tireSensorAvailable";
    public static final String MB_KEY_CHARGE_COUPLER_DC_LOCK_STATUS = "chargeCouplerDCLockStatus";
    public static final String MB_KEY_CHARGE_COUPLER_DC_STATUS = "chargeCouplerDCStatus";
    public static final String MB_KEY_CHARGE_COUPLER_AC_STATUS = "chargeCouplerACStatus";
    public static final String MB_KEY_CHARGE_FLAP_DC_STATUS = "chargeFlapDCStatus";
    public static final String MB_KEY_SERVICEINTERVALDAYS = "serviceintervaldays";
    public static final String MB_KEY_TIREWARNINGSRDK = "tirewarningsrdk";
    public static final String MB_KEY_STARTER_BATTERY_STATE = "starterBatteryState";
    public static final String MB_KEY_FLIP_WINDOW_STATUS = "flipWindowStatus";
    public static final String MB_KEY_WINDOW_STATUS_REAR_BLIND = "windowStatusRearBlind";
    public static final String MB_KEY_WINDOW_STATUS_REAR_LEFT_BLIND = "windowStatusRearLeftBlind";
    public static final String MB_KEY_WINDOW_STATUS_REAR_RIGHT_BLIND = "windowStatusRearRightBlind";
    public static final String MB_KEY_WINDOWSTATUSREARRIGHT = "windowstatusrearright";
    public static final String MB_KEY_WINDOWSTATUSREARLEFT = "windowstatusrearleft";
    public static final String MB_KEY_WINDOWSTATUSFRONTRIGHT = "windowstatusfrontright";
    public static final String MB_KEY_WINDOWSTATUSFRONTLEFT = "windowstatusfrontleft";
    public static final String MB_KEY_ROOFTOPSTATUS = "rooftopstatus";
    public static final String MB_KEY_SUNROOF_STATUS_REAR_BLIND = "sunroofStatusRearBlind";
    public static final String MB_KEY_SUNROOF_STATUS_FRONT_BLIND = "sunroofStatusFrontBlind";
    public static final String MB_KEY_SUNROOFSTATUS = "sunroofstatus";
    public static final String MB_KEY_IGNITIONSTATE = "ignitionstate";
    public static final String MB_KEY_DOOR_STATUS_OVERALL = "doorStatusOverall";
    public static final String MB_KEY_WINDOW_STATUS_OVERALL = "windowStatusOverall";
    public static final String MB_KEY_DOOR_LOCK_STATUS_OVERALL = "doorLockStatusOverall";
    public static final String MB_KEY_TIRE_MARKER_FRONT_RIGHT = "tireMarkerFrontRight";
    public static final String MB_KEY_TIRE_MARKER_FRONT_LEFT = "tireMarkerFrontLeft";
    public static final String MB_KEY_TIRE_MARKER_REAR_RIGHT = "tireMarkerRearRight";
    public static final String MB_KEY_TIRE_MARKER_REAR_LEFT = "tireMarkerRearLeft";
    public static final String MB_KEY_PARKBRAKESTATUS = "parkbrakestatus";
    public static final String MB_KEY_PRECOND_NOW = "precondNow";
    public static final String MB_KEY_PRECOND_SEAT_FRONT_RIGHT = "precondSeatFrontRight";
    public static final String MB_KEY_PRECOND_SEAT_FRONT_LEFT = "precondSeatFrontLeft";
    public static final String MB_KEY_PRECOND_SEAT_REAR_RIGHT = "precondSeatRearRight";
    public static final String MB_KEY_PRECOND_SEAT_REAR_LEFT = "precondSeatRearLeft";
    public static final String MB_KEY_WARNINGBRAKEFLUID = "warningbrakefluid";
    public static final String MB_KEY_WARNINGBRAKELININGWEAR = "warningbrakeliningwear";
    public static final String MB_KEY_WARNINGWASHWATER = "warningwashwater";
    public static final String MB_KEY_WARNINGCOOLANTLEVELLOW = "warningcoolantlevellow";
    public static final String MB_KEY_WARNINGENGINELIGHT = "warningenginelight";
    public static final String MB_KEY_CHARGINGACTIVE = "chargingactive";
    public static final String MB_KEY_DOORLOCKSTATUSFRONTRIGHT = "doorlockstatusfrontright";
    public static final String MB_KEY_DOORLOCKSTATUSFRONTLEFT = "doorlockstatusfrontleft";
    public static final String MB_KEY_DOORLOCKSTATUSREARRIGHT = "doorlockstatusrearright";
    public static final String MB_KEY_DOORLOCKSTATUSREARLEFT = "doorlockstatusrearleft";
    public static final String MB_KEY_DOORLOCKSTATUSDECKLID = "doorlockstatusdecklid";
    public static final String MB_KEY_DOORLOCKSTATUSGAS = "doorlockstatusgas";
    public static final String MB_KEY_TIREPRESSURE_FRONT_LEFT = "tirepressureFrontLeft";
    public static final String MB_KEY_TIREPRESSURE_FRONT_RIGHT = "tirepressureFrontRight";
    public static final String MB_KEY_TIREPRESSURE_REAR_LEFT = "tirepressureRearLeft";
    public static final String MB_KEY_POSITION_HEADING = "positionHeading";
    public static final String MB_KEY_TIREPRESSURE_REAR_RIGHT = "tirepressureRearRight";
    public static final String MB_KEY_ENGINE_HOOD_STATUS = "engineHoodStatus";
    public static final String MB_KEY_DECKLIDSTATUS = "decklidstatus";
    public static final String MB_KEY_DOORSTATUSREARLEFT = "doorstatusrearleft";
    public static final String MB_KEY_DOORSTATUSREARRIGHT = "doorstatusrearright";
    public static final String MB_KEY_DOORSTATUSFRONTLEFT = "doorstatusfrontleft";
    public static final String MB_KEY_DOORSTATUSFRONTRIGHT = "doorstatusfrontright";
    public static final String MB_KEY_TANKLEVELPERCENT = "tanklevelpercent";
    public static final String MB_KEY_SOC = "soc";
    public static final String MB_KEY_TIRE_PRESS_MEAS_TIMESTAMP = "tirePressMeasTimestamp";
    public static final String MB_KEY_ENDOFCHARGETIME = "endofchargetime";
    public static final String MB_KEY_ENDOFCHARGEDAY = "endofChargeTimeWeekday";
    public static final String MB_KEY_LIQUIDCONSUMPTIONRESET = "liquidconsumptionreset";
    public static final String MB_KEY_LIQUIDCONSUMPTIONSTART = "liquidconsumptionstart";
    public static final String MB_KEY_ELECTRICCONSUMPTIONRESET = "electricconsumptionreset";
    public static final String MB_KEY_ELECTRICCONSUMPTIONSTART = "electricconsumptionstart";
    public static final String MB_KEY_AVERAGE_SPEED_RESET = "averageSpeedReset";
    public static final String MB_KEY_AVERAGE_SPEED_START = "averageSpeedStart";
    public static final String MB_KEY_CHARGING_POWER = "chargingPower";
    public static final String MB_KEY_DRIVEN_TIME_RESET = "drivenTimeReset";
    public static final String MB_KEY_DRIVEN_TIME_START = "drivenTimeStart";
    public static final String MB_KEY_DISTANCE_RESET = "distanceReset";
    public static final String MB_KEY_DISTANCE_START = "distanceStart";
    public static final String MB_KEY_RANGELIQUID = "rangeliquid";
    public static final String MB_KEY_OVERALL_RANGE = "overallRange";
    public static final String MB_KEY_RANGEELECTRIC = "rangeelectric";
    public static final String MB_KEY_ODO = "odo";
    public static final String MB_KEY_POSITION_LONG = "positionLong";
    public static final String MB_KEY_POSITION_LAT = "positionLat";
    public static final String MB_KEY_TEMPERATURE_POINTS = "temperaturePoints";
    public static final String MB_KEY_SELECTED_CHARGE_PROGRAM = "selectedChargeProgram";
    public static final String MB_KEY_CHARGE_PROGRAMS = "chargePrograms";
    public static final String MB_KEY_COMMAND_CAPABILITIES = "command-capabilities";
    public static final String MB_KEY_FEATURE_CAPABILITIES = "feature-capabilities";
    public static final String MB_KEY_COMMAND_ZEV_PRECONDITION_CONFIGURE_SEATS = "commandZevPreconditionConfigureSeats";
    public static final String MB_KEY_COMMAND_SUNROOF_OPEN = "commandSunroofOpen";
    public static final String MB_KEY_COMMAND_CHARGE_PROGRAM_CONFIGURE = "commandChargeProgramConfigure";
    public static final String MB_KEY_COMMAND_SIGPOS_START = "commandSigposStart";
    public static final String MB_KEY_FEATURE_AUX_HEAT = "featureAuxHeat";
    public static final String MB_KEY_COMMAND_ZEV_PRECONDITIONING_START = "commandZevPreconditioningStart";
    public static final String MB_KEY_COMMAND_ZEV_PRECONDITION_CONFIGURE = "commandZevPreconditionConfigure";
    public static final String MB_KEY_COMMAND_DOORS_LOCK = "commandDoorsLock";
    public static final String MB_KEY_COMMAND_WINDOWS_OPEN = "commandWindowsOpen";
    public static final String MB_KEY_COMMAND_ENGINE_START = "commandEngineStart";

    public static final String GROUP_VEHICLE = "vehicle";
    public static final String GROUP_DOORS = "doors";
    public static final String GROUP_LOCK = "lock";
    public static final String GROUP_WINDOWS = "windows";
    public static final String GROUP_HVAC = "hvac";
    public static final String GROUP_SERVICE = "service";
    public static final String GROUP_RANGE = "range";
    public static final String GROUP_CHARGE = "charge";
    public static final String GROUP_TRIP = "trip";
    public static final String GROUP_POSITION = "position";
    public static final String GROUP_TIRES = "tires";
    public static final String GROUP_COMMAND = "command";

    public static final String OH_CHANNEL_LAST_UPDATE = "last-update";
    public static final String OH_CHANNEL_SENSOR_AVAILABLE = "sensor-available";
    public static final String OH_CHANNEL_MARKER_FRONT_LEFT = "marker-front-left";
    public static final String OH_CHANNEL_MARKER_REAR_LEFT = "marker-rear-left";
    public static final String OH_CHANNEL_MARKER_FRONT_RIGHT = "marker-front-right";
    public static final String OH_CHANNEL_MARKER_REAR_RIGHT = "marker-rear-right";
    public static final String OH_CHANNEL_PRESSURE_FRONT_LEFT = "pressure-front-left";
    public static final String OH_CHANNEL_PRESSURE_REAR_LEFT = "pressure-rear-left";
    public static final String OH_CHANNEL_PRESSURE_FRONT_RIGHT = "pressure-front-right";
    public static final String OH_CHANNEL_PRESSURE_REAR_RIGHT = "pressure-rear-right";
    public static final String OH_CHANNEL_CONS_CONV_RESET = "cons-conv-reset";
    public static final String OH_CHANNEL_CONS_EV_RESET = "cons-ev-reset";
    public static final String OH_CHANNEL_AVG_SPEED_RESET = "avg-speed-reset";
    public static final String OH_CHANNEL_TIME_RESET = "time-reset";
    public static final String OH_CHANNEL_DISTANCE_RESET = "distance-reset";
    public static final String OH_CHANNEL_CONS_CONV = "cons-conv";
    public static final String OH_CHANNEL_CONS_EV = "cons-ev";
    public static final String OH_CHANNEL_AVG_SPEED = "avg-speed";
    public static final String OH_CHANNEL_TIME = "time";
    public static final String OH_CHANNEL_DISTANCE = "distance";
    public static final String OH_CHANNEL_HEADING = "heading";
    public static final String OH_CHANNEL_END_TIME = "end-time";
    public static final String OH_CHANNEL_POWER = "power";
    public static final String OH_CHANNEL_COUPLER_LOCK = "coupler-lock";
    public static final String OH_CHANNEL_COUPLER_DC = "coupler-dc";
    public static final String OH_CHANNEL_COUPLER_AC = "coupler-ac";
    public static final String OH_CHANNEL_CHARGE_FLAP = "charge-flap";
    public static final String OH_CHANNEL_FUEL_LEVEL = "fuel-level";
    public static final String OH_CHANNEL_RANGE_HYBRID = "range-hybrid";
    public static final String OH_CHANNEL_RANGE_FUEL = "range-fuel";
    public static final String OH_CHANNEL_RANGE_ELECTRIC = "range-electric";
    public static final String OH_CHANNEL_RADIUS_HYBRID = "radius-hybrid";
    public static final String OH_CHANNEL_RADIUS_FUEL = "radius-fuel";
    public static final String OH_CHANNEL_RADIUS_ELECTRIC = "radius-electric";
    public static final String OH_CHANNEL_SERVICE_DAYS = "service-days";
    public static final String OH_CHANNEL_TIRES_RDK = "tires-rdk";
    public static final String OH_CHANNEL_ENGINE = "engine";
    public static final String OH_CHANNEL_COOLANT_FLUID = "coolant-fluid";
    public static final String OH_CHANNEL_BRAKE_LINING_WEAR = "brake-lining-wear";
    public static final String OH_CHANNEL_WASH_WATER = "wash-water";
    public static final String OH_CHANNEL_BRAKE_FLUID = "brake-fluid";
    public static final String OH_CHANNEL_STARTER_BATTERY = "starter-battery";
    public static final String OH_CHANNEL_ACTIVE = "active";
    public static final String OH_CHANNEL_FLIP_WINDOW = "flip-window";
    public static final String OH_CHANNEL_REAR_BLIND = "rear-blind";
    public static final String OH_CHANNEL_REAR_LEFT_BLIND = "rear-left-blind";
    public static final String OH_CHANNEL_REAR_RIGHT_BLIND = "rear-right-blind";
    public static final String OH_CHANNEL_GAS_FLAP = "gas-flap";
    public static final String OH_CHANNEL_ROOFTOP = "rooftop";
    public static final String OH_CHANNEL_SUNROOF_REAR_BLIND = "sunroof-rear-blind";
    public static final String OH_CHANNEL_SUNROOF_FRONT_BLIND = "sunroof-front-blind";
    public static final String OH_CHANNEL_SUNROOF = "sunroof";
    public static final String OH_CHANNEL_ENGINE_HOOD = "engine-hood";
    public static final String OH_CHANNEL_DECK_LID = "deck-lid";
    public static final String OH_CHANNEL_REAR_LEFT = "rear-left";
    public static final String OH_CHANNEL_REAR_RIGHT = "rear-right";
    public static final String OH_CHANNEL_FRONT_LEFT = "front-left";
    public static final String OH_CHANNEL_FRONT_RIGHT = "front-right";
    public static final String OH_CHANNEL_PARK_BRAKE = "park-brake";
    public static final String OH_CHANNEL_IGNITION = "ignition";
    public static final String OH_CHANNEL_DOOR_STATUS = "door-status";
    public static final String OH_CHANNEL_WINDOWS = "windows";
    public static final String OH_CHANNEL_LOCK = "lock";
    public static final String OH_CHANNEL_MILEAGE = "mileage";
    public static final String OH_CHANNEL_TEMPERATURE = "temperature";
    public static final String OH_CHANNEL_AUX_HEAT = "aux-heat";
    public static final String OH_CHANNEL_ZONE = "zone";
    public static final String OH_CHANNEL_SIGNAL = "signal";
    public static final String OH_CHANNEL_AUTO_UNLOCK = "auto-unlock";
    public static final String OH_CHANNEL_MAX_SOC = "max-soc";
    public static final String OH_CHANNEL_PROGRAM = "program";
    public static final String OH_CHANNEL_CMD_LAST_UPDATE = "cmd-last-update";
    public static final String OH_CHANNEL_CMD_STATE = "cmd-state";
    public static final String OH_CHANNEL_CMD_NAME = "cmd-name";
    public static final String OH_CHANNEL_PROTO_UPDATE = "proto-update";
    public static final String OH_CHANNEL_SOC = "soc";
    public static final String OH_CHANNEL_UNCHARGED = "uncharged";
    public static final String OH_CHANNEL_CHARGED = "charged";
    public static final String OH_CHANNEL_TANK_OPEN = "tank-open";
    public static final String OH_CHANNEL_TANK_REMAIN = "tank-remain";
    public static final String OH_CHANNEL_HOME_DISTANCE = "home-distance";
    public static final String OH_CHANNEL_GPS = "gps";
    public static final String OH_CHANNEL_CONS_CONV_UNIT = "cons-conv-unit";
    public static final String OH_CHANNEL_CONS_EV_UNIT = "cons-ev-unit";

    public static final String CALLBACK_ENDPOINT = "/mb-auth";
    // https://developer.mercedes-benz.com/content-page/api_migration_guide
    public static final String IMAGE_BASE_URL = "https://api.mercedes-benz.com/vehicle_images/v2";
    public static final String IMAGE_EXTERIOR_RESOURCE_URL = IMAGE_BASE_URL + "/vehicles/%s";

    public static final String STATUS_TEXT_PREFIX = "@text/mercedesme.";
    public static final String STATUS_AUTH_NEEDED = ".status.authorization-needed";
    public static final String STATUS_EMAIL_MISSING = ".status.email-missing";
    public static final String STATUS_REGION_MISSING = ".status.region-missing";
    public static final String STATUS_REFRESH_INVALID = ".status.refresh-invalid";
    public static final String STATUS_IP_MISSING = ".status.ip-missing";
    public static final String STATUS_PORT_MISSING = ".status.port-missing";
    public static final String STATUS_SERVER_RESTART = ".status.server-restart";
    public static final String STATUS_BRIDGE_MISSING = ".status.bridge-missing";

    public static final String SPACE = " ";
    public static final String EMPTY = "";
    public static final String COLON = ":";
    public static final String NOT_SET = "not set";
    public static final String UNRECOGNIZED = "UNRECOGNIZED";

    public static final String CODE = "code";
    public static final String GUID = "guid";
    public static final String PIN = "pin";
    public static final String MIME_PREFIX = "image/";

    public static final Unit<Length> KILOMETRE_UNIT = MetricPrefix.KILO(SIUnits.METRE);
    public static final Unit<Power> KILOWATT_UNIT = MetricPrefix.KILO(Units.WATT);
    public static final Unit<Energy> KILOWATT_HOUR_UNIT = MetricPrefix.KILO(Units.WATT_HOUR);
    public static final Unit<Pressure> KPA_UNIT = MetricPrefix.KILO(SIUnits.PASCAL);

    public static final String LOGIN_APP_ID = "01398c1c-dc45-4b42-882b-9f5ba9f175f1";
    public static final String LOGIN_APP_ID_EU = "01398c1c-dc45-4b42-882b-9f5ba9f175f1";
    public static final String LOGIN_APP_ID_CN = "3f36efb1-f84b-4402-b5a2-68a118fec33e";
    public static final String LOGIN_BASE_URI = "https://id.mercedes-benz.com";
    public static final String LOGIN_BASE_URI_CN = "https://ciam-1.mercedes-benz.com.cn";
    public static final String LOGIN_BASE_URI_NA = "https://id.mercedes-benz.com";
    public static final String LOGIN_BASE_URI_PA = "https://id.mercedes-benz.com";
    public static final String PSAG_BASE_URI = "https://psag.query.api.dvb.corpinter.net";
    public static final String PSAG_BASE_URI_CN = "https://psag.query.api.dvb.corpinter.net.cn";
    public static final String RCP_BASE_URI = "https://rcp-rs.query.api.dvb.corpinter.net";
    public static final String RCP_BASE_URI_CN = "https://rcp-rs.query.api.dvb.corpinter.net.cn";
    public static final String REST_API_BASE = "https://bff.emea-prod.mobilesdk.mercedes-benz.com";
    public static final String REST_API_BASE_CN = "https://bff.cn-prod.mobilesdk.mercedes-benz.com";
    public static final String REST_API_BASE_NA = "https://bff.amap-prod.mobilesdk.mercedes-benz.com";
    public static final String REST_API_BASE_PA = "https://bff.amap-prod.mobilesdk.mercedes-benz.com";
    public static final String WEBSOCKET_API_BASE = "wss://websocket.emea-prod.mobilesdk.mercedes-benz.com/ws";
    public static final String WEBSOCKET_API_BASE_NA = "wss://websocket.amap-prod.mobilesdk.mercedes-benz.com/ws";
    public static final String WEBSOCKET_API_BASE_PA = "wss://websocket.amap-prod.mobilesdk.mercedes-benz.com/ws";
    public static final String WEBSOCKET_API_BASE_CN = "wss://websocket.cn-prod.mobilesdk.mercedes-benz.com/ws";
    public static final String WEBSOCKET_USER_AGENT = "MyCar/1.30.1 (com.daimler.ris.mercedesme.ece.ios; build:1819; iOS 16.5.0) Alamofire/5.4.0";
    public static final String WEBSOCKET_USER_AGENT_CN = "MyStarCN/1.27.0 (com.daimler.ris.mercedesme.cn.ios; build:1758; iOS 16.3.1) Alamofire/5.4.0";
    public static final String WEBSOCKET_USER_AGENT_PA = "mycar-store-ap v1.27.0, android 8.0.0, SDK 2.84.3";

    public static final String RIS_APPLICATION_VERSION_NA = "3.40.0";
    public static final String RIS_APPLICATION_VERSION_CN = "1.39.0";
    public static final String RIS_APPLICATION_VERSION_PA = "1.40.0";
    public static final String RIS_APPLICATION_VERSION = "1.42.0 (2168)";
    public static final String RIS_SDK_VERSION = "2.114.0";
    public static final String RIS_SDK_VERSION_CN = "2.109.2";
    public static final String RIS_OS_VERSION = "17.4.1";
    public static final String RIS_OS_NAME = "ios";
    public static final String X_APPLICATIONNAME = "mycar-store-ece";
    public static final String X_APPLICATIONNAME_ECE = "mycar-store-ece";
    public static final String X_APPLICATIONNAME_CN = "mycar-store-cn";
    public static final String X_APPLICATIONNAME_US = "mycar-store-us";
    public static final String X_APPLICATIONNAME_AP = "mycar-store-ap";

    public static final String REGION_EUROPE = "EU";
    public static final String REGION_NORAM = "NA";
    public static final String REGION_APAC = "AP";
    public static final String REGION_CHINA = "CN";

    public static final String SCOPE = "openid email phone profile offline_access ciam-uid";

    public static final String MAX_SOC_KEY = "maxsoc";
    public static final String AUTO_UNLOCK_KEY = "autolock";

    public static final String JUNIT_SERVER_ADDR = "http://999.999.999.999:99999/mb-auth";
    public static final String JUNIT_TOKEN = "junitTestToken";
    public static final String JUNIT_REFRESH_TOKEN = "junitRefreshToken";
}
