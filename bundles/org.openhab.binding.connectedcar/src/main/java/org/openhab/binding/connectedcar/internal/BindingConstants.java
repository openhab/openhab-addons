/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.connectedcar.internal;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.measure.Unit;
import javax.measure.quantity.Dimensionless;
import javax.measure.quantity.Length;
import javax.measure.quantity.Power;
import javax.measure.quantity.Temperature;
import javax.measure.quantity.Time;
import javax.measure.quantity.Volume;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.library.unit.MetricPrefix;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link BindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Markus Michels - Initial contribution
 * @author Thomas Knaller - Maintainer
 * @author Dr. Yves Kreis - Maintainer
 */
@NonNullByDefault
public class BindingConstants {

    public static final String BINDING_ID = "connectedcar";

    // List of all Thing Type UIDs
    public static final String THING_MYAUDI = "myaudi";
    public static final String THING_VOLKSWAGEN = "volkswagen";
    public static final String THING_VWID = "vwid";
    public static final String THING_SEAT = "seat";
    public static final String THING_SKODA = "skoda";
    public static final String THING_SKODA_E = "skoda-e";
    public static final String THING_FORD = "ford";
    public static final String THING_WECHARGE = "wecharge";

    public static final ThingTypeUID THING_TYPE_MYAUDI = new ThingTypeUID(BINDING_ID, THING_MYAUDI);
    public static final ThingTypeUID THING_TYPE_VW = new ThingTypeUID(BINDING_ID, THING_VOLKSWAGEN);
    public static final ThingTypeUID THING_TYPE_VWID = new ThingTypeUID(BINDING_ID, THING_VWID);
    public static final ThingTypeUID THING_TYPE_SKODA = new ThingTypeUID(BINDING_ID, THING_SKODA);
    public static final ThingTypeUID THING_TYPE_SEAT = new ThingTypeUID(BINDING_ID, THING_SEAT);
    public static final ThingTypeUID THING_TYPE_ENYAK = new ThingTypeUID(BINDING_ID, THING_SKODA_E);
    public static final ThingTypeUID THING_TYPE_FORD = new ThingTypeUID(BINDING_ID, THING_FORD);
    public static final ThingTypeUID THING_TYPE_WECHARGE = new ThingTypeUID(BINDING_ID, THING_WECHARGE);

    public static final ThingTypeUID THING_TYPE_CNVEHICLE = new ThingTypeUID(BINDING_ID, "cnvehicle");
    public static final ThingTypeUID THING_TYPE_IDVEHICLE = new ThingTypeUID(BINDING_ID, "idvehicle");
    public static final ThingTypeUID THING_TYPE_GOPLUG = new ThingTypeUID(BINDING_ID, "goplug");
    public static final ThingTypeUID THING_TYPE_WCWALLBOX = new ThingTypeUID(BINDING_ID, "wcwallbox");
    public static final ThingTypeUID THING_TYPE_SKODAEVEHICLE = new ThingTypeUID(BINDING_ID, "sevehicle");
    public static final ThingTypeUID THING_TYPE_FORDVEHICLE = new ThingTypeUID(BINDING_ID, "fordvehicle");
    public static final ThingTypeUID THING_TYPE_MMEVEHICLE = new ThingTypeUID(BINDING_ID, "mmevehicle");

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Collections
            .unmodifiableSet(Stream.of(THING_TYPE_MYAUDI, THING_TYPE_VW, THING_TYPE_SKODA, THING_TYPE_SEAT,
                    THING_TYPE_CNVEHICLE, THING_TYPE_ENYAK, THING_TYPE_SKODAEVEHICLE, THING_TYPE_FORD,
                    THING_TYPE_FORDVEHICLE, THING_TYPE_VWID, THING_TYPE_IDVEHICLE, THING_TYPE_MMEVEHICLE,
                    THING_TYPE_WECHARGE, THING_TYPE_WCWALLBOX).collect(Collectors.toSet()));

    // List of all ChannelGroups and Channels
    public static final String CHANNEL_GROUP_GENERAL = "general";
    public static final String CHANNEL_GENERAL_UPDATED = "lastUpdate";
    public static final String CHANNEL_GENERAL_ACTION = "lastAction";
    public static final String CHANNEL_GENERAL_ACTION_STATUS = "lastActionStatus";
    public static final String CHANNEL_GENERAL_ACTION_PENDING = "lastActionPending";
    public static final String CHANNEL_GENERAL_RATELIM = "rateLimit";

    // Group status
    public static final String CHANNEL_GROUP_STATUS = "status";
    public static final String CHANNEL_STATUS_ODOMETER = "odometer";
    public static final String CHANNEL_STATUS_PLIGHT = "parkingLight";
    public static final String CHANNEL_STATUS_PBRAKE = "parkingBrake";
    public static final String CHANNEL_STATUS_LIGHTS = "vehicleLights";
    public static final String CHANNEL_STATUS_TEMPOUT = "tempOutside";
    public static final String CHANNEL_STATUS_SPEED = "currentSpeed";
    public static final String CHANNEL_STATUS_MMILAGE = "monthlyMilage";
    public static final String CHANNEL_STATUS_LOCKED = "vehicleLocked";
    public static final String CHANNEL_STATUS_MAINTREQ = "maintenanceRequired";
    public static final String CHANNEL_STATUS_WINCLOSED = "windowsClosed";
    public static final String CHANNEL_STATUS_DOORSCLOSED = "doorsClosed";
    public static final String CHANNEL_STATUS_TIRESOK = "tiresOk";
    public static final String CHANNEL_STATUS_TIMEINCAR = "timeInCar";
    public static final String CHANNEL_STATUS_ERROR = "error";
    public static final String CHANNEL_STATUS_SWUPDATE = "softwareUpgrade";
    public static final String CHANNEL_STATUS_DEEPSLEEP = "deepSleep";

    // Group control
    public static final String CHANNEL_GROUP_CONTROL = "control";
    public static final String CHANNEL_CONTROL_UPDATE = "update";
    public static final String CHANNEL_CONTROL_LOCK = "lock";
    public static final String CHANNEL_CONTROL_ENGINE = "engine";
    public static final String CHANNEL_CONTROL_RESTART = "restart";
    public static final String CHANNEL_CONTROL_CHARGER = "charge";
    public static final String CHANNEL_CONTROL_TARGETCHG = "targetChgLvl";
    public static final String CHANNEL_CONTROL_CLIMATER = "climater";
    public static final String CHANNEL_CONTROL_TARGET_TEMP = "targetTemperature";
    public static final String CHANNEL_CONTROL_WINHEAT = "windowHeat";
    public static final String CHANNEL_CONTROL_PREHEAT = "preHeater";
    public static final String CHANNEL_CONTROL_DURATION = "duration";
    public static final String CHANNEL_CONTROL_VENT = "ventilation";
    public static final String CHANNEL_CONTROL_FLASH = "flash";
    public static final String CHANNEL_CONTROL_HONKFLASH = "honkFlash";
    public static final String CHANNEL_CONTROL_HFDURATION = "hfDuration";

    public static final String CHANNEL_GROUP_LOCATION = "location";
    public static final String CHANNEL_LOCATTION_GEO = "locationPosition";
    public static final String CHANNEL_LOCATTION_ADDRESS = "locationAddress";
    public static final String CHANNEL_LOCATTION_TIME = "locationLastUpdate";
    public static final String CHANNEL_PARK_LOCATION = "parkingPosition";
    public static final String CHANNEL_PARK_ADDRESS = "parkingAddress";
    public static final String CHANNEL_PARK_TIME = "parkingTime";
    public static final String CHANNEL_CAR_MOVING = "carMoving";

    // Group range
    public static final String CHANNEL_GROUP_RANGE = "range";
    public static final String CHANNEL_RANGE_FUEL = "fuelPercentage";
    public static final String CHANNEL_RANGE_GAS = "gasPercentage";
    public static final String CHANNEL_RANGE_FMETHOD = "fuelMethod";
    public static final String CHANNEL_RANGE_TOTAL = "totalRange";
    public static final String CHANNEL_RANGE_PRANGE = "primaryRange";
    public static final String CHANNEL_RANGE_SRANGE = "secondaryRange";

    // Group Climarter
    public static final String CHANNEL_GROUP_CLIMATER = "climater";
    public static final String CHANNEL_CLIMATER_GEN_STATE = "climatisationState";
    public static final String CHANNEL_CLIMATER_HEATSOURCE = "heaterSource";
    public static final String CHANNEL_CLIMATER_MIRROR_HEAT = "mirrorHeat";
    public static final String CHANNEL_CLIMATER_REMAINING = "remainingClimatisation";

    // Group charger
    public static final String CHANNEL_GROUP_CHARGER = "charger";
    public static final String CHANNEL_CHARGER_STATUS = "chargingStatus";
    public static final String CHANNEL_CHARGER_ERROR = "errorCode";
    public static final String CHANNEL_CHARGER_PWR_STATE = "powerState";
    public static final String CHANNEL_CHARGER_FLOW = "energyFlow";
    public static final String CHANNEL_CHARGER_CHG_STATE = "chargingState";
    public static final String CHANNEL_CHARGER_MODE = "chargingMode";
    public static final String CHANNEL_CHARGER_CHGLVL = "chargingLevel";
    public static final String CHANNEL_CHARGER_BAT_STATE = "batteryState";
    public static final String CHANNEL_CHARGER_PLUG_STATE = "plugState";
    public static final String CHANNEL_CHARGER_LOCK_STATE = "lockState";
    public static final String CHANNEL_CHARGER_REMAINING = "remainingChargingTime";
    public static final String CHANNEL_CHARGER_POWER = "chargingPower";
    public static final String CHANNEL_CHARGER_MAXCURRENT = "maxCurrent";
    public static final String CHANNEL_CHARGER_RATE = "chargingRate";
    // Wallbox
    public static final String CHANNEL_CHARGER_NAME = "chargerName";
    public static final String CHANNEL_CHARGER_ADDRESS = "chargerAddress";
    public static final String CHANNEL_CHARGER_LAST_CONNECT = "chargerLastConnect";
    public static final String CHANNEL_CHARGER_CYCLES = "chargingCycles";
    public static final String CHANNEL_CHARGER_ENERGY = "chargingEnergy";

    public static final String CHANNEL_GROUP_TRIP_PRE = "trip";
    public static final String CHANNEL_TRIP_SHORT = "Short";
    public static final String CHANNEL_TRIP_LONG = "Long";
    public static final String CHANNEL_GROUP_STRIP = CHANNEL_GROUP_TRIP_PRE + CHANNEL_TRIP_SHORT;
    public static final String CHANNEL_GROUP_LTRIP = CHANNEL_GROUP_TRIP_PRE + CHANNEL_TRIP_LONG;
    public static final String CHANNEL_TRIP_TIME = "timestamp";
    public static final String CHANNEL_TRIP_TRAVELTIME = "traveltime";
    public static final String CHANNEL_TRIP_AVG_ELCON = "avgElectricConsumption";
    public static final String CHANNEL_TRIP_AVG_FUELCON = "avgFuelConsumption";
    public static final String CHANNEL_TRIP_AVG_SPEED = "avgSpeed";
    public static final String CHANNEL_TRIP_MILAGE = "mileage";
    public static final String CHANNEL_TRIP_START_MIL = "startMileage";
    public static final String CHANNEL_TRIP_OVR_MILAGE = "overallMileage";

    public static final String CHANNEL_GROUP_DEST_PRE = "destination";
    public static final String CHANNEL_DEST_NAME = "destinationName";
    public static final String CHANNEL_DEST_POI = "destinationPoi";
    public static final String CHANNEL_DEST_GEO = "destinationLocation";
    public static final String CHANNEL_DEST_STREET = "destinatinStreet";
    public static final String CHANNEL_DEST_CITY = "destinationCity";
    public static final String CHANNEL_DEST_ZIP = "destinationZip";
    public static final String CHANNEL_DEST_COUNTY = "destinationCountry";
    public static final String CHANNEL_DEST_SOURCE = "destinationSource";

    public static final String CHANNEL_GROUP_SPEEDALERT = "speedAlerts";
    public static final String CHANNEL_SPEEDALERT_TYPE = "speedAlertType";
    public static final String CHANNEL_SPEEDALERT_DESCR = "speedAlertDescr";
    public static final String CHANNEL_SPEEDALERT_TIME = "speedAlertTime";
    public static final String CHANNEL_SPEEDALERT_LIMIT = "speedAlertLimit";

    public static final String CHANNEL_GROUP_GEOFENCE = "geoFenceAlerts";
    public static final String CHANNEL_GEOFENCE_TYPE = "geoFenceAlertType";
    public static final String CHANNEL_GEOFENCE_DESCR = "geoFenceAlertDescr";
    public static final String CHANNEL_GEOFENCE_TIME = "geoFenceAlertTime";

    public static final String CHANNEL_GROUP_TIRES = "tires";
    public static final String CHANNEL_TIREP_FRONTLEFT = "tirePresFrontLeft";
    public static final String CHANNEL_TIREP_FRONTRIGHT = "tirePresFrontRight";
    public static final String CHANNEL_TIREP_REARLEFT = "tirePresRearLeft";
    public static final String CHANNEL_TIREP_REARRIGHT = "tirePresRearRight";
    public static final String CHANNEL_TIREP_INNERREARLEFT = "tirePresInnerRearLeft";
    public static final String CHANNEL_TIREP_INNERREARRIGHT = "tirePresInnerRearRight";
    public static final String CHANNEL_TIREP_SPARE = "tirePresSpare";

    // Group Windows
    public static final String CHANNEL_GROUP_WINDOWS = "windows";
    public static final String CHANNEL_WIN_FLSTATE = "windowFrontLeftState";
    public static final String CHANNEL_WIN_FLPOS = "windowFrontLeftPos";
    public static final String CHANNEL_WIN_RLSTATE = "windowRearLeftState";
    public static final String CHANNEL_WIN_RLPOS = "windowRearLeftPos";
    public static final String CHANNEL_WIN_FRSTATE = "windowFrontRightState";
    public static final String CHANNEL_WIN_FRPOS = "windowFrontRightPos";
    public static final String CHANNEL_WIN_RRSTATE = "windowRearRightState";
    public static final String CHANNEL_WIN_RRPOS = "windowRearRightPos";
    public static final String CHANNEL_WIN_FROOFSTATE = "roofFrontCoverState";
    public static final String CHANNEL_WIN_FROOFPOS = "roofFrontCoverPos";
    public static final String CHANNEL_WIN_RROOFSTATE = "roofRearCoverState";
    public static final String CHANNEL_WIN_RROOFPOS = "roofRearCoverPos";
    public static final String CHANNEL_WIN_SROOFSTATE = "sunRoofCoverState";
    public static final String CHANNEL_WIN_SROOFPOS = "sunRoofCoverPos";

    // Group Doors
    public static final String CHANNEL_GROUP_DOORS = "doors";
    public static final String CHANNEL_DOORS_FLSTATE = "doorFrontLeftState";
    public static final String CHANNEL_DOORS_FLLOCKED = "doorFrontLeftLocked";
    public static final String CHANNEL_DOORS_FRSTATE = "doorFrontRightState";
    public static final String CHANNEL_DOORS_FRLOCKED = "doorFrontRightLocked";
    public static final String CHANNEL_DOORS_RLSTATE = "doorRearLeftState";
    public static final String CHANNEL_DOORS_RLLOCKED = "doorRearLeftLocked";
    public static final String CHANNEL_DOORS_RRSTATE = "doorRearRightState";
    public static final String CHANNEL_DOORS_RRLOCKED = "doorRearRightLocked";
    public static final String CHANNEL_DOORS_HOODSTATE = "hoodState";
    public static final String CHANNEL_DOORS_HOODLOCKED = "hoodLocked";
    public static final String CHANNEL_DOORS_TRUNKLSTATE = "trunkLidState";
    public static final String CHANNEL_DOORS_TRUNKLLOCKED = "trunkLidLocked";
    public static final String CHANNEL_DOORS_CTOPSTATE = "covertibleTopState";
    public static final String CHANNEL_DOORS_CTOPPOS = "covertibleTopPos";
    public static final String CHANNEL_DOORS_ITAILGSTATE = "innerTailgateState";

    // Other channels group in here
    public static final String CHANNEL_GROUP_MAINT = "maintenance";
    public static final String CHANNEL_MAINT_ALARMINSP = "alarmInspection";
    public static final String CHANNEL_MAINT_DISTINSP = "distanceToInspection";
    public static final String CHANNEL_MAINT_DISTTIME = "timeToInspection";
    public static final String CHANNEL_MAINT_OILPERC = "oilPercentage";
    public static final String CHANNEL_MAINT_OILWARNCHG = "oilWarningChange";
    public static final String CHANNEL_MAINT_OILWARNLVL = "oilWarningLevel";
    public static final String CHANNEL_MAINT_OILDIST = "distanceOilChange";
    public static final String CHANNEL_MAINT_OILINTV = "intervalOilChange";
    public static final String CHANNEL_MAINT_ABDIST = "distanceAdBlue";

    public static final String CHANNEL_GROUP_PICTURES = "pictures";
    public static final String CHANNEL_PICTURES_IMG_PREFIX = "imageUrl";

    public static final String CHANNEL_GROUP_RLUHIST = "rluHistory";
    public static final String CHANNEL_RLUHIST_TS = "rluTimestamp";
    public static final String CHANNEL_RLUHIST_OP = "rluOperation";
    public static final String CHANNEL_RLUHIST_RES = "rluResult";

    // Subscriptions (Wallbox)
    public static final String CHANNEL_GROUP_SUBSCRIPTION = "subscription";
    public static final String CHANNEL_SUB_ENDDATE = "subEndDate";
    public static final String CHANNEL_SUB_STATUS = "subStatus";
    public static final String CHANNEL_SUB_TARIFF = "subTariff";
    public static final String CHANNEL_SUB_MFEE = "subMonthlyFee";

    // RFID cards (Wallbox)
    public static final String CHANNEL_CHANNEL_GROUP_RFID = "rfid";
    public static final String CHANNEL_RFID_ID = "rfidId";
    public static final String CHANNEL_RFID_STATUS = "rfidStatus";
    public static final String CHANNEL_RFID_PUBLIC = "rfidPublicCharging";
    public static final String CHANNEL_RFID_UPDATE = "rfidLastUpdated";

    // CHarging transactions
    public static final String CHANNEL_CHANNEL_GROUP_TRANSACTIONS = "transaction";
    public static final String CHANNEL_TRANS_ID = "transId";
    public static final String CHANNEL_TRANS_PUBLIC = "transPublic";
    public static final String CHANNEL_TRANS_LOCATION = "transLocation";
    public static final String CHANNEL_TRANS_ADDRESS = "transAddress";
    public static final String CHANNEL_TRANS_SUBID = "transSubscription";
    public static final String CHANNEL_TRANS_EVSE = "transStationId";
    public static final String CHANNEL_TRANS_PTYPE = "transPowerType";
    public static final String CHANNEL_TRANS_START = "transStart";
    public static final String CHANNEL_TRANS_END = "transEnd";
    public static final String CHANNEL_TRANS_ENERGY = "transEnergy";
    public static final String CHANNEL_TRANS_PRICE = "transPrice";
    public static final String CHANNEL_TRANS_RFID = "transRfidCard";
    public static final String CHANNEL_TRANS_TARIFF = "transTariff";
    public static final String CHANNEL_TRANS_DURATION = "transDuration";

    // Thing Properties
    public static final String PROPERTY_VIN = "vin";
    public static final String PROPERTY_BRAND = "brand";
    public static final String PROPERTY_MODEL = "model";
    public static final String PROPERTY_COLOR = "color";
    public static final String PROPERTY_MMI = "mmi";
    public static final String PROPERTY_ENGINE = "engine";
    public static final String PROPERTY_TRANS = "transmission";
    public static final String PROPERTY_NONCE = "loginNonce";

    public static final String ITEMT_STRING = "String";
    public static final String ITEMT_NUMBER = "Number";
    public static final String ITEMT_PERCENT = "Number:Dimensionless";
    public static final String ITEMT_SWITCH = "Switch";
    public static final String ITEMT_CONTACT = "Contact";
    public static final String ITEMT_LOCATION = "Location";
    public static final String ITEMT_TIME = "Number:Time";
    public static final String ITEMT_DATETIME = "DateTime";
    public static final String ITEMT_TEMP = "Number:Temperature";
    public static final String ITEMT_DISTANCE = "Number:Length";
    public static final String ITEMT_SPEED = "Number:Speed";
    public static final String ITEMT_VOLUME = "Number:Volume";
    public static final String ITEMT_POWER = "Number:Power";
    public static final String ITEMT_ENERGY = "Number:Energy";
    public static final String ITEMT_VOLT = "Number:ElectricPotential";
    public static final String ITEMT_AMP = "Number:ElectricCurrent";

    public static final String CONTENT_TYPE_FORM_URLENC = "application/x-www-form-urlencoded";
    public static final String CONTENT_TYPE_JSON = "application/json";

    public static final int POLL_INTERVAL_SEC = 3; // poll cycle evey 3sec
    public static final int API_TOKEN_REFRESH_INTERVAL_SEC = 5 * 60; // interval to check for valid token
    public static final int API_REQUEST_TIMEOUT_SEC = 180 + 5;
    public static final int API_REQUEST_CHECK_INT = 15 / POLL_INTERVAL_SEC; // interval for checking pending requests
    public static final int API_TIMEOUT_MS = 60 * 1000;
    public static final int DEFAULT_TOKEN_VALIDITY_SEC = 3600;

    public static int VENT_DEFAULT_DURATION_MIN = 30;
    public static int HF_DEFAULT_DURATION_SEC = 10;

    public static final Unit<Length> KILOMETRE = MetricPrefix.KILO(SIUnits.METRE);
    public static final Unit<Power> KWATT = MetricPrefix.KILO(Units.WATT);
    public static final Unit<Time> QDAYS = Units.DAY;
    public static final Unit<Time> QMINUTES = Units.MINUTE;
    public static final Unit<Dimensionless> PERCENT = Units.PERCENT;
    public static final Unit<Temperature> DKELVIN = MetricPrefix.DECI(Units.KELVIN);
    public static final Unit<Volume> DLITRE = MetricPrefix.DECI(Units.LITRE);
}
