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
package org.openhab.binding.ecobee.internal;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.ecobee.internal.dto.thermostat.AlertDTO;
import org.openhab.binding.ecobee.internal.dto.thermostat.EventDTO;
import org.openhab.binding.ecobee.internal.dto.thermostat.HouseDetailsDTO;
import org.openhab.binding.ecobee.internal.dto.thermostat.LocationDTO;
import org.openhab.binding.ecobee.internal.dto.thermostat.ManagementDTO;
import org.openhab.binding.ecobee.internal.dto.thermostat.RemoteSensorDTO;
import org.openhab.binding.ecobee.internal.dto.thermostat.TechnicianDTO;
import org.openhab.binding.ecobee.internal.dto.thermostat.ThermostatDTO;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.type.ChannelTypeUID;

/**
 * The {@link EcobeeBindingConstants} class defines common constants that are
 * used across the whole binding.
 *
 * @author Mark Hilbush - Initial contribution
 */
@NonNullByDefault
public class EcobeeBindingConstants {

    public static final String BINDING_ID = "ecobee";

    // Account bridge
    public static final String THING_TYPE_ACCOUNT = "account";
    public static final ThingTypeUID UID_ACCOUNT_BRIDGE = new ThingTypeUID(BINDING_ID, THING_TYPE_ACCOUNT);
    public static final Set<ThingTypeUID> SUPPORTED_ACCOUNT_BRIDGE_THING_TYPES_UIDS = Collections
            .unmodifiableSet(Stream.of(UID_ACCOUNT_BRIDGE).collect(Collectors.toSet()));

    // Thermostat bridge
    public static final String THING_TYPE_THERMOSTAT = "thermostat";
    public static final ThingTypeUID UID_THERMOSTAT_BRIDGE = new ThingTypeUID(BINDING_ID, THING_TYPE_THERMOSTAT);
    public static final Set<ThingTypeUID> SUPPORTED_THERMOSTAT_BRIDGE_THING_TYPES_UIDS = Collections
            .unmodifiableSet(Stream.of(UID_THERMOSTAT_BRIDGE).collect(Collectors.toSet()));

    // Remote sensor thing
    public static final String THING_TYPE_SENSOR = "sensor";
    public static final ThingTypeUID UID_SENSOR_THING = new ThingTypeUID(BINDING_ID, THING_TYPE_SENSOR);
    public static final Set<ThingTypeUID> SUPPORTED_SENSOR_THING_TYPES_UIDS = Collections
            .unmodifiableSet(Stream.of(UID_SENSOR_THING).collect(Collectors.toSet()));

    // Collection of thermostat and sensor thing types
    public static final Set<ThingTypeUID> SUPPORTED_THERMOSTAT_AND_SENSOR_THING_TYPES_UIDS = Stream
            .concat(SUPPORTED_THERMOSTAT_BRIDGE_THING_TYPES_UIDS.stream(), SUPPORTED_SENSOR_THING_TYPES_UIDS.stream())
            .collect(Collectors.toSet());

    // Collection of all supported thing types
    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Collections.unmodifiableSet(
            Stream.of(UID_ACCOUNT_BRIDGE, UID_THERMOSTAT_BRIDGE, UID_SENSOR_THING).collect(Collectors.toSet()));

    // Background discovery frequency
    public static final int DISCOVERY_INTERVAL_SECONDS = 300;
    public static final int DISCOVERY_INITIAL_DELAY_SECONDS = 10;

    // Thermostat bridge and remote sensor thing config parameters
    public static final String CONFIG_THERMOSTAT_ID = "thermostatId";
    public static final String CONFIG_SENSOR_ID = "sensorId";

    // Channel groups
    public static final String CHGRP_ALERT = "alerts";
    public static final String CHGRP_INFO = "info";
    public static final String CHGRP_ELECTRICITY = "electricity";
    public static final String CHGRP_EQUIPMENT_STATUS = "equipmentStatus";
    public static final String CHGRP_EVENT = "events";
    public static final String CHGRP_EXTENDED_RUNTIME = "extendedRuntime";
    public static final String CHGRP_HOUSE_DETAILS = "houseDetails";
    public static final String CHGRP_LOCATION = "location";
    public static final String CHGRP_MANAGEMENT = "management";
    public static final String CHGRP_NOTIFICATION_SETTINGS = "notificationSettings";
    public static final String CHGRP_OEM_CFG = "oemCfg";
    public static final String CHGRP_PRIVACY = "privacy";
    public static final String CHGRP_PROGRAM = "program";
    public static final String CHGRP_RUNTIME = "runtime";
    public static final String CHGRP_SETTINGS = "settings";
    public static final String CHGRP_TECHNICIAN = "technician";
    public static final String CHGRP_UTILITY = "utility";
    public static final String CHGRP_VERSION = "version";
    public static final String CHGRP_WEATHER = "weather";
    public static final String CHGRP_FORECAST = "forecast";

    // Exclude CHGRP_INFO and CHGRP_FORECAST because they are not part of the selection object
    public static final List<String> CHANNEL_GROUPS = Stream.of(CHGRP_ALERT, CHGRP_ELECTRICITY, CHGRP_EQUIPMENT_STATUS,
            CHGRP_EVENT, CHGRP_EXTENDED_RUNTIME, CHGRP_HOUSE_DETAILS, CHGRP_LOCATION, CHGRP_MANAGEMENT,
            CHGRP_NOTIFICATION_SETTINGS, CHGRP_OEM_CFG, CHGRP_PRIVACY, CHGRP_PROGRAM, CHGRP_RUNTIME, CHGRP_SETTINGS,
            CHGRP_TECHNICIAN, CHGRP_UTILITY, CHGRP_VERSION, CHGRP_WEATHER).collect(Collectors.toList());

    // Thermostat bridge info channels
    public static final String CH_IDENTIFIER = "identifier";
    public static final String CH_NAME = "name";
    public static final String CH_THERMOSTAT_REV = "thermostatRev";
    public static final String CH_IS_REGISTERED = "isRegistered";
    public static final String CH_MODEL_NUMBER = "modelNumber";
    public static final String CH_BRAND = "brand";
    public static final String CH_FEATURES = "features";
    public static final String CH_LAST_MODIFIED = "lastModified";
    public static final String CH_THERMOSTAT_TIME = "thermostatTime";
    public static final String CH_UTC_TIME = "utcTime";

    // Thermostat bridge equipment status channels
    public static final String CH_EQUIPMENT_STATUS = "equipmentStatus";

    // Thermostat bridge ALERT channels
    public static final String CH_ALERT_ACKNOWLEDGE_REF = "acknowledgeRef";
    public static final String CH_ALERT_DATE = "date";
    public static final String CH_ALERT_TIME = "time";
    public static final String CH_ALERT_SEVERITY = "severity";
    public static final String CH_ALERT_TEXT = "text";
    public static final String CH_ALERT_ALERT_NUMBER = "number";
    public static final String CH_ALERT_ALERT_TYPE = "type";
    public static final String CH_ALERT_IS_OPERATOR_ALERT = "isOperatorAlert";
    public static final String CH_ALERT_REMINDER = "reminder";
    public static final String CH_ALERT_SHOW_IDT = "showIdt";
    public static final String CH_ALERT_SHOW_WEB = "showWeb";
    public static final String CH_ALERT_SEND_EMAIL = "sendEmail";
    public static final String CH_ALERT_ACKNOWLEDGEMENT = "acknowledgement";
    public static final String CH_ALERT_REMIND_ME_LATER = "remindMeLater";
    public static final String CH_ALERT_THERMOSTAT_IDENTIFIER = "thermostatIdentifier";
    public static final String CH_ALERT_NOTIFICATION_TYPE = "notificationType";

    // Thermostat bridge EVENT channels
    public static final String CH_EVENT_NAME = "name";
    public static final String CH_EVENT_TYPE = "type";
    public static final String CH_EVENT_RUNNING = "running";
    public static final String CH_EVENT_START_DATE = "startDate";
    public static final String CH_EVENT_START_TIME = "startTime";
    public static final String CH_EVENT_END_DATE = "endDate";
    public static final String CH_EVENT_END_TIME = "endTime";
    public static final String CH_EVENT_IS_OCCUPIED = "isOccupied";
    public static final String CH_EVENT_IS_COOL_OFF = "isCoolOff";
    public static final String CH_EVENT_IS_HEAT_OFF = "isHeatOff";
    public static final String CH_EVENT_COOL_HOLD_TEMP = "coolHoldTemp";
    public static final String CH_EVENT_HEAT_HOLD_TEMP = "heatHoldTemp";
    public static final String CH_EVENT_FAN = "fan";
    public static final String CH_EVENT_VENT = "vent";
    public static final String CH_EVENT_VENTILATOR_MIN_ON_TIME = "ventilatorMinOnTime";
    public static final String CH_EVENT_IS_OPTIONAL = "isOptional";
    public static final String CH_EVENT_IS_TEMPERATURE_RELATIVE = "isTemperatureRelative";
    public static final String CH_EVENT_COOL_RELATIVE_TEMP = "coolRelativeTemp";
    public static final String CH_EVENT_HEAT_RELATIVE_TEMP = "heatRelativeTemp";
    public static final String CH_EVENT_IS_TEMPERATURE_ABSOLUTE = "isTemperatureAbsolute";
    public static final String CH_EVENT_DUTY_CYCLE_PERCENTAGE = "dutyCyclePercentage";
    public static final String CH_EVENT_FAN_MIN_ON_TIME = "fanMinOnTime";
    public static final String CH_EVENT_OCCUPIED_SENSOR_ACTIVE = "occupiedSensorActive";
    public static final String CH_EVENT_UNOCCUPIED_SENSOR_ACTIVE = "unoccupiedSensorActive";
    public static final String CH_EVENT_DR_RAMP_UP_TEMP = "drRampUpTemp";
    public static final String CH_EVENT_DR_RAMP_UP_TIME = "drRampUpTime";
    public static final String CH_EVENT_LINK_REF = "linkRef";
    public static final String CH_EVENT_HOLD_CLIMATE_REF = "holdClimateRef";

    // Thermostat bridge HOUSE DETAILS channels
    public static final String CH_HOUSEDETAILS_STYLE = "style";
    public static final String CH_HOUSEDETAILS_SIZE = "size";
    public static final String CH_HOUSEDETAILS_NUMBER_OF_FLOORS = "numberOfFloors";
    public static final String CH_HOUSEDETAILS_NUMBER_OF_ROOMS = "numberOfRooms";
    public static final String CH_HOUSEDETAILS_NUMBER_OF_OCCUPANTS = "numberOfOccupants";
    public static final String CH_HOUSEDETAILS_AGE = "age";
    public static final String CH_HOUSEDETAILS_WINDOW_EFFICIENCY = "windowEfficiency";

    // Thermostat bridge LOCATION channels
    public static final String CH_TIME_ZONE_OFFSET_MINUTES = "timeZoneOffsetMinutes";
    public static final String CH_TIME_ZONE = "timeZone";
    public static final String CH_IS_DAYLIGHT_SAVING = "isDaylightSaving";
    public static final String CH_STREET_ADDRESS = "streetAddress";
    public static final String CH_CITY = "city";
    public static final String CH_PROVINCE_STATE = "provinceState";
    public static final String CH_COUNTRY = "country";
    public static final String CH_POSTAL_CODE = "postalCode";
    public static final String CH_PHONE_NUMBER = "phoneNumber";
    public static final String CH_MAP_COORDINATES = "mapCoordinates";

    // Thermostat bridge MANAGEMENT channels
    public static final String CH_MANAGEMENT_ADMIN_CONTACT = "administrativeContact";
    public static final String CH_MANAGEMENT_BILLING_CONTACT = "billingContact";
    public static final String CH_MANAGEMENT_NAME = "name";
    public static final String CH_MANAGEMENT_PHONE = "phone";
    public static final String CH_MANAGEMENT_EMAIL = "email";
    public static final String CH_MANAGEMENT_WEB = "web";
    public static final String CH_MANAGEMENT_SHOW_ALERT_IDT = "showAlertIdt";
    public static final String CH_MANAGEMENT_SHOW_ALERT_WEB = "showAlertWeb";

    // Thermostat bridge PROGRAM channels
    public static final String CH_PROGRAM_CURRENT_CLIMATE_REF = "currentClimateRef";

    // Thermostat bridge RUNTIME channels
    public static final String CH_RUNTIME_REV = "runtimeRev";
    public static final String CH_CONNECTED = "connected";
    public static final String CH_FIRST_CONNECTED = "firstConnected";
    public static final String CH_CONNECT_DATE_TIME = "connectDateTime";
    public static final String CH_DISCONNECT_DATE_TIME = "disconnectDateTime";
    public static final String CH_RT_LAST_MODIFIED = "lastModified";
    public static final String CH_RT_LAST_STATUS_MODIFIED = "lastStatusModified";
    public static final String CH_RUNTIME_DATE = "runtimeDate";
    public static final String CH_RUNTIME_INTERVAL = "runtimeInterval";
    public static final String CH_ACTUAL_TEMPERATURE = "actualTemperature";
    public static final String CH_ACTUAL_HUMIDITY = "actualHumidity";
    public static final String CH_RAW_TEMPERATURE = "rawTemperature";
    public static final String CH_SHOW_ICON_MODE = "showIconMode";
    public static final String CH_DESIRED_HEAT = "desiredHeat";
    public static final String CH_DESIRED_COOL = "desiredCool";
    public static final String CH_DESIRED_HUMIDITY = "desiredHumidity";
    public static final String CH_DESIRED_DEHUMIDITY = "desiredDehumidity";
    public static final String CH_DESIRED_FAN_MODE = "desiredFanMode";
    public static final String CH_DESIRED_HEAT_RANGE_LOW = "desiredHeatRangeLow";
    public static final String CH_DESIRED_HEAT_RANGE_HIGH = "desiredHeatRangeHigh";
    public static final String CH_DESIRED_COOL_RANGE_LOW = "desiredCoolRangeLow";
    public static final String CH_DESIRED_COOL_RANGE_HIGH = "desiredCoolRangeHigh";
    public static final String CH_ACTUAL_AQ_ACCURACY = "actualAQAccuracy";
    public static final String CH_ACTUAL_AQ_SCORE = "actualAQScore";
    public static final String CH_ACTUAL_CO2 = "actualCO2";
    public static final String CH_ACTUAL_VOC = "actualVOC";

    // Thermostat bridge SETTINGS channels
    public static final String CH_HVAC_MODE = "hvacMode";
    public static final String CH_LAST_SERVICE_DATE = "lastServiceDate";
    public static final String CH_SERVICE_REMIND_ME = "serviceRemindMe";
    public static final String CH_MONTHS_BETWEEN_SERVICE = "monthsBetweenService";
    public static final String CH_REMIND_ME_DATE = "remindMeDate";
    public static final String CH_VENT = "vent";
    public static final String CH_VENTILATOR_MIN_ON_TIME = "ventilatorMinOnTime";
    public static final String CH_SERVICE_REMIND_TECHNICIAN = "serviceRemindTechnician";
    public static final String CH_EI_LOCATION = "eiLocation";
    public static final String CH_COLD_TEMP_ALERT = "coldTempAlert";
    public static final String CH_COLD_TEMP_ALERT_ENABLED = "coldTempAlertEnabled";
    public static final String CH_HOT_TEMP_ALERT = "hotTempAlert";
    public static final String CH_HOT_TEMP_ALERT_ENABLED = "hotTempAlertEnabled";
    public static final String CH_COOL_STAGES = "coolStages";
    public static final String CH_HEAT_STAGES = "heatStages";
    public static final String CH_MAX_SET_BACK = "maxSetBack";
    public static final String CH_MAX_SET_FORWARD = "maxSetForward";
    public static final String CH_QUICK_SAVE_SET_BACK = "quickSaveSetBack";
    public static final String CH_QUICK_SAVE_SET_FORWARD = "quickSaveSetForward";
    public static final String CH_HAS_HEAT_PUMP = "hasHeatPump";
    public static final String CH_HAS_FORCED_AIR = "hasForcedAir";
    public static final String CH_HAS_BOILER = "hasBoiler";
    public static final String CH_HAS_HUMIDIFIER = "hasHumidifier";
    public static final String CH_HAS_ERV = "hasErv";
    public static final String CH_HAS_HRV = "hasHrv";
    public static final String CH_CONDENSATION_AVOID = "condensationAvoid";
    public static final String CH_USE_CELSIUS = "useCelsius";
    public static final String CH_USE_TIME_FORMAT_12 = "useTimeFormat12";
    public static final String CH_LOCALE = "locale";
    public static final String CH_HUMIDITY = "humidity";
    public static final String CH_HUMIDIFIER_MODE = "humidifierMode";
    public static final String CH_BACKLIGHT_ON_INTENSITY = "backlightOnIntensity";
    public static final String CH_BACKLIGHT_SLEEP_INTENSITY = "backlightSleepIntensity";
    public static final String CH_BACKLIGHT_OFF_TIME = "backlightOffTime";
    public static final String CH_SOUND_TICK_VOLUME = "soundTickVolume";
    public static final String CH_SOUND_ALERT_VOLUME = "soundAlertVolume";
    public static final String CH_COMPRESSOR_PROTECTION_MIN_TIME = "compressorProtectionMinTime";
    public static final String CH_COMPRESSOR_PROTECTION_MIN_TEMP = "compressorProtectionMinTemp";
    public static final String CH_STAGE1_HEATING_DIFFERENTIAL_TEMP = "stage1HeatingDifferentialTemp";
    public static final String CH_STAGE1_COOLING_DIFFERENTIAL_TEMP = "stage1CoolingDifferentialTemp";
    public static final String CH_STAGE1_HEATING_DISSIPATION_TIME = "stage1HeatingDissipationTime";
    public static final String CH_STAGE1_COOLING_DISSIPATION_TIME = "stage1CoolingDissipationTime";
    public static final String CH_HEAT_PUMP_REVERSAL_ON_COOL = "heatPumpReversalOnCool";
    public static final String CH_FAN_CONTROLLER_REQUIRED = "fanControlRequired";
    public static final String CH_FAN_MIN_ON_TIME = "fanMinOnTime";
    public static final String CH_HEAT_COOL_MIN_DELTA = "heatCoolMinDelta";
    public static final String CH_TEMP_CORRECTION = "tempCorrection";
    public static final String CH_HOLD_ACTION = "holdAction";
    public static final String CH_HEAT_PUMP_GROUND_WATER = "heatPumpGroundWater";
    public static final String CH_HAS_ELECTRIC = "hasElectric";
    public static final String CH_HAS_DEHUMIDIFIER = "hasDehumidifier";
    public static final String CH_DEHUMIDIFIER_MODE = "dehumidifierMode";
    public static final String CH_DEHUMIDIFIER_LEVEL = "dehumidifierLevel";
    public static final String CH_DEHUMIDIFY_WITH_AC = "dehumidifyWithAC";
    public static final String CH_DEHUMIDIFY_OVERCOOL_OFFSET = "dehumidifyOvercoolOffset";
    public static final String CH_AUTO_HEAT_COOL_FEATURE_ENABLED = "autoHeatCoolFeatureEnabled";
    public static final String CH_WIFI_OFFLINE_ALERT = "wifiOfflineAlert";
    public static final String CH_HEAT_MIN_TEMP = "heatMinTemp";
    public static final String CH_HEAT_MAX_TEMP = "heatMaxTemp";
    public static final String CH_COOL_MIN_TEMP = "coolMinTemp";
    public static final String CH_COOL_MAX_TEMP = "coolMaxTemp";
    public static final String CH_HEAT_RANGE_HIGH = "heatRangeHigh";
    public static final String CH_HEAT_RANGE_LOW = "heatRangeLow";
    public static final String CH_COOL_RANGE_HIGH = "coolRangeHigh";
    public static final String CH_COOL_RANGE_LOW = "coolRangeLow";
    public static final String CH_USER_ACCESS_CODE = "userAccessCode";
    public static final String CH_USER_ACCESS_SETTING = "userAccessSetting";
    public static final String CH_AUX_RUNTIME_ALERT = "auxRuntimeAlert";
    public static final String CH_AUX_OUTDOOR_TEMP_ALERT = "auxOutdoorTempAlert";
    public static final String CH_AUX_MAX_OUTDOOR_TEMP = "auxMaxOutdoorTemp";
    public static final String CH_AUX_RUNTIME_ALERT_NOTIFY = "auxRuntimeAlertNotify";
    public static final String CH_AUX_OUTDOOR_TEMP_ALERT_NOTIFY = "auxOutdoorTempAlertNotify";
    public static final String CH_AUX_RUNTIME_ALERT_NOTIFY_TECHNICIAN = "auxRuntimeAlertNotifyTechnician";
    public static final String CH_AUX_OUTDOOR_TEMP_ALERT_NOTIFY_TECHNICIAN = "auxOutdoorTempAlertNotifyTechnician";
    public static final String CH_DISABLE_PREHEATING = "disablePreHeating";
    public static final String CH_DISABLE_PRECOOLING = "disablePreCooling";
    public static final String CH_INSTALLER_CODE_REQUIRED = "installerCodeRequired";
    public static final String CH_DR_ACCEPT = "drAccept";
    public static final String CH_IS_RENTAL_PROPERTY = "isRentalProperty";
    public static final String CH_USE_ZONE_CONTROLLER = "useZoneController";
    public static final String CH_RANDOM_START_DELAY_COOL = "randomStartDelayCool";
    public static final String CH_RANDOM_START_DELAY_HEAT = "randomStartDelayHeat";
    public static final String CH_HUMIDITY_HIGH_ALERT = "humidityHighAlert";
    public static final String CH_HUMIDITY_LOW_ALERT = "humidityLowAlert";
    public static final String CH_DISABLE_HEAT_PUMP_ALERTS = "disableHeatPumpAlerts";
    public static final String CH_DISABLE_ALERTS_ON_IDT = "disableAlertsOnIdt";
    public static final String CH_HUMIDITY_ALERT_NOTIFY = "humidityAlertNotify";
    public static final String CH_HUMIDITY_ALERT_NOTIFY_TECHNICIAN = "humidityAlertNotifyTechnician";
    public static final String CH_TEMP_ALERT_NOTIFY = "tempAlertNotify";
    public static final String CH_TEMP_ALERT_NOTIFY_TECHNICIAN = "tempAlertNotifyTechnician";
    public static final String CH_MONTHLY_ELECTRICITY_BILL_LIMIT = "monthlyElectricityBillLimit";
    public static final String CH_ENABLE_ELECTRICITY_BILL_ALERT = "enableElectricityBillAlert";
    public static final String CH_ENABLE_PROJECTED_ELECTRICITY_BILL_ALERT = "enableProjectedElectricityBillAlert";
    public static final String CH_ELECTRICITY_BILLING_DAY_OF_MONTH = "electricityBillingDayOfMonth";
    public static final String CH_ELECTRICITY_BILL_CYCLE_MONTHS = "electricityBillCycleMonths";
    public static final String CH_ELECTRICITY_BILL_START_MONTH = "electricityBillStartMonth";
    public static final String CH_VENTILATOR_MIN_ON_TIME_HOME = "ventilatorMinOnTimeHome";
    public static final String CH_VENTILATOR_MIN_ON_TIME_AWAY = "ventilatorMinOnTimeAway";
    public static final String CH_BACKLIGHT_OFF_DURING_SLEEP = "backlightOffDuringSleep";
    public static final String CH_AUTO_AWAY = "autoAway";
    public static final String CH_SMART_CIRCULATION = "smartCirculation";
    public static final String CH_FOLLOW_ME_COMFORT = "followMeComfort";
    public static final String CH_VENTILATOR_TYPE = "ventilatorType";
    public static final String CH_IS_VENTILATOR_TIMER_ON = "isVentilatorTimerOn";
    public static final String CH_VENTILATOR_OFF_DATE_TIME = "ventilatorOffDateTime";
    public static final String CH_HAS_UV_FILTER = "hasUVFilter";
    public static final String CH_COOLING_LOCKOUT = "coolingLockout";
    public static final String CH_VENTILATOR_FREE_COOLING = "ventilatorFreeCooling";
    public static final String CH_DEHUMIDIFY_WHEN_HEATING = "dehumidifyWhenHeating";
    public static final String CH_VENTILATOR_DEHUMIDIFY = "ventilatorDehumidify";
    public static final String CH_GROUP_REF = "groupRef";
    public static final String CH_GROUP_NAME = "groupName";
    public static final String CH_GROUP_SETTING = "groupSetting";

    // Thermostat bridge TECHNICIAN channels
    public static final String CH_TECHNICIAN_CONTRACTOR_REF = "contractorRef";
    public static final String CH_TECHNICIAN_NAME = "name";
    public static final String CH_TECHNICIAN_PHONE = "phone";
    public static final String CH_TECHNICIAN_STREET_ADDRESS = "streetAddress";
    public static final String CH_TECHNICIAN_CITY = "city";
    public static final String CH_TECHNICIAN_PROVINCE_STATE = "provinceState";
    public static final String CH_TECHNICIAN_COUNTRY = "country";
    public static final String CH_TECHNICIAN_POSTAL_CODE = "postalCode";
    public static final String CH_TECHNICIAN_EMAIL = "email";
    public static final String CH_TECHNICIAN_WEB = "web";

    // Thermostat bridge VERSION channels
    public static final String CH_THERMOSTAT_FIRMWARE_VERSION = "thermostatFirmwareVersion";

    // Thermostat bridge WEATHER channels
    public static final String CH_WEATHER_TIMESTAMP = "timestamp";
    public static final String CH_WEATHER_WEATHER_STATION = "weatherStation";

    // Thermostat bridge FORECAST channels
    public static final String CH_FORECAST_WEATHER_SYMBOL = "weatherSymbol";
    public static final String CH_FORECAST_WEATHER_SYMBOL_TEXT = "weatherSymbolText";
    public static final String CH_FORECAST_DATE_TIME = "dateTime";
    public static final String CH_FORECAST_CONDITION = "condition";
    public static final String CH_FORECAST_TEMPERATURE = "temperature";
    public static final String CH_FORECAST_PRESSURE = "pressure";
    public static final String CH_FORECAST_RELATIVE_HUMIDITY = "relativeHumidity";
    public static final String CH_FORECAST_DEWPOINT = "dewpoint";
    public static final String CH_FORECAST_VISIBILITY = "visibility";
    public static final String CH_FORECAST_WIND_SPEED = "windSpeed";
    public static final String CH_FORECAST_WIND_GUST = "windGust";
    public static final String CH_FORECAST_WIND_DIRECTION = "windDirection";
    public static final String CH_FORECAST_WIND_BEARING = "windBearing";
    public static final String CH_FORECAST_POP = "pop";
    public static final String CH_FORECAST_TEMP_HIGH = "tempHigh";
    public static final String CH_FORECAST_TEMP_LOW = "tempLow";
    public static final String CH_FORECAST_SKY = "sky";
    public static final String CH_FORECAST_SKY_TEXT = "skyText";

    // Remote sensor thing channel IDs
    public static final String CH_SENSOR_ID = "id";
    public static final String CH_SENSOR_NAME = "name";
    public static final String CH_SENSOR_TYPE = "type";
    public static final String CH_SENSOR_CODE = "code";
    public static final String CH_SENSOR_IN_USE = "inUse";

    // Channel Type UIDs for dynamically created sensor channels
    public static final ChannelTypeUID CHANNELTYPEUID_TEMPERATURE = new ChannelTypeUID(BINDING_ID, "sensorTemperature");
    public static final ChannelTypeUID CHANNELTYPEUID_HUMIDITY = new ChannelTypeUID(BINDING_ID, "sensorHumidity");
    public static final ChannelTypeUID CHANNELTYPEUID_OCCUPANCY = new ChannelTypeUID(BINDING_ID, "sensorOccupancy");
    public static final ChannelTypeUID CHANNELTYPEUID_GENERIC = new ChannelTypeUID(BINDING_ID, "sensorGeneric");

    public static final AlertDTO EMPTY_ALERT = new AlertDTO();
    public static final EventDTO EMPTY_EVENT = new EventDTO();
    public static final LocationDTO EMPTY_LOCATION = new LocationDTO();
    public static final HouseDetailsDTO EMPTY_HOUSEDETAILS = new HouseDetailsDTO();
    public static final ManagementDTO EMPTY_MANAGEMENT = new ManagementDTO();
    public static final TechnicianDTO EMPTY_TECHNICIAN = new TechnicianDTO();
    public static final List<RemoteSensorDTO> EMPTY_SENSORS = Collections.<RemoteSensorDTO> emptyList();
    public static final List<ThermostatDTO> EMPTY_THERMOSTATS = Collections.<ThermostatDTO> emptyList();

    public static final String ECOBEE_BASE_URL = "https://api.ecobee.com/";
    public static final String ECOBEE_AUTHORIZE_URL = ECOBEE_BASE_URL + "authorize";
    public static final String ECOBEE_TOKEN_URL = ECOBEE_BASE_URL + "token";
    public static final String ECOBEE_SCOPE = "smartWrite";
    public static final String ECOBEE_DATETIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
}
