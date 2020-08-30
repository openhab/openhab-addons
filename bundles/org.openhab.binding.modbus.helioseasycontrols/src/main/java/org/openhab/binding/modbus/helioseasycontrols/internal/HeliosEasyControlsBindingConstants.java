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
package org.openhab.binding.modbus.helioseasycontrols.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.openhab.binding.modbus.ModbusBindingConstants;

/**
 * The {@link HeliosEasyControlsBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Bernhard Bauer - Initial contribution
 */
@NonNullByDefault
public class HeliosEasyControlsBindingConstants {

    private static final String BINDING_ID = ModbusBindingConstants.BINDING_ID;

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_HELIOS_VENTILATION_EASY_CONTROLS = new ThingTypeUID(BINDING_ID,
            "helios-ventilation-easycontrols");

    // List of all Channel ids
    public final static String ARTICLE_DESCRIPTION = "articleDescription";
    public final static String REF_NO = "refNo";
    public final static String MAC_ADDRESS = "macAddress";
    public final static String LANGUAGE = "language";
    public final static String DATE = "date";
    public final static String TIME = "time";
    public final static String SUMMER_WINTER = "summerWinter";
    public final static String AUTO_SW_UPDATE = "autoSwUpdate";
    public final static String ACCESS_HELIOS_PORTAL = "accessHeliosPortal";
    public final static String VOLTAGE_FAN_STAGE_1_EXTRACT_AIR = "voltageFanStage1ExtractAir";
    public final static String VOLTAGE_FAN_STAGE_2_EXTRACT_AIR = "voltageFanStage2ExtractAir";
    public final static String VOLTAGE_FAN_STAGE_3_EXTRACT_AIR = "voltageFanStage3ExtractAir";
    public final static String VOLTAGE_FAN_STAGE_4_EXTRACT_AIR = "voltageFanStage4ExtractAir";
    public final static String VOLTAGE_FAN_STAGE_1_SUPPLY_AIR = "voltageFanStage1SupplyAir";
    public final static String VOLTAGE_FAN_STAGE_2_SUPPLY_AIR = "voltageFanStage2SupplyAir";
    public final static String VOLTAGE_FAN_STAGE_3_SUPPLY_AIR = "voltageFanStage3SupplyAir";
    public final static String VOLTAGE_FAN_STAGE_4_SUPPLY_AIR = "voltageFanStage4SupplyAir";
    public final static String MIN_FAN_STAGE = "minFanStage";
    public final static String KWL_BE = "kwlBe";
    public final static String KWL_BEC = "kwlBec";
    public final static String UNIT_CONFIG = "unitConfig";
    public final static String PRE_HEATER_STATUS = "preHeaterStatus";
    public final static String KWL_FTF_CONFIG_0 = "kwlFtfConfig0";
    public final static String KWL_FTF_CONFIG_1 = "kwlFtfConfig1";
    public final static String KWL_FTF_CONFIG_2 = "kwlFtfConfig2";
    public final static String KWL_FTF_CONFIG_3 = "kwlFtfConfig3";
    public final static String KWL_FTF_CONFIG_4 = "kwlFtfConfig4";
    public final static String KWL_FTF_CONFIG_5 = "kwlFtfConfig5";
    public final static String KWL_FTF_CONFIG_6 = "kwlFtfConfig6";
    public final static String KWL_FTF_CONFIG_7 = "kwlFtfConfig7";
    public final static String HUMIDITY_CONTROL_STATUS = "humidityControlStatus";
    public final static String HUMIDITY_CONTROL_SET_VALUE = "humidityControlSetValue";
    public final static String HUMIDITY_CONTROL_STEPS = "humidityControlSteps";
    public final static String HUMIDITY_STOP_TIME = "humidityStopTime";
    public final static String CO2_CONTROL_STATUS = "co2ControlStatus";
    public final static String CO2_CONTROL_SET_VALUE = "co2ControlSetValue";
    public final static String CO2_CONTROL_STEPS = "co2ControlSteps";
    public final static String VOC_CONTROL_STATUS = "vocControlStatus";
    public final static String VOC_CONTROL_SET_VALUE = "vocControlSetValue";
    public final static String VOC_CONTROL_STEPS = "vocControlSteps";
    public final static String COMFORT_TEMP = "comfortTemp";
    public final static String TIME_ZONE_DIFFERENCE_TO_GMT = "timeZoneDifferenceToGmt";
    public final static String DATE_FORMAT = "dateFormat";
    public final static String HEAT_EXCHANGER_TYPE = "heatExchangerType";
    public final static String PARTY_MODE_DURATION = "partyModeDuration";
    public final static String PARTY_MODE_FAN_STAGE = "partyModeFanStage";
    public final static String PARTY_MODE_REMAINING_TIME = "partyModeRemainingTime";
    public final static String PARTY_MODE_STATUS = "partyModeStatus";
    public final static String STANDBY_MODE_DURATION = "standbyModeDuration";
    public final static String STANDBY_MODE_FAN_STAGE = "standbyModeFanStage";
    public final static String STANDBY_MODE_REMAINING_TIME = "standbyModeRemainingTime";
    public final static String STANDBY_MODE_STATUS = "standbyModeStatus";
    public final static String OPERATING_MODE = "operatingMode";
    public final static String FAN_STAGE = "fanStage";
    public final static String PERCENTAGE_FAN_STAGE = "percentageFanStage";
    public final static String TEMPERATURE_OUTSIDE_AIR = "temperatureOutsideAir";
    public final static String TEMPERATURE_SUPPLY_AIR = "temperatureSupplyAir";
    public final static String TEMPERATURE_OUTGOING_AIR = "temperatureOutgoingAir";
    public final static String TEMPERATURE_EXTRACT_AIR = "temperatureExtractAir";
    public final static String VHZ_DUCT_SENSOR = "vhzDuctSensor";
    public final static String NHZ_RETURN_SENSOR = "nhzReturnSensor";
    public final static String EXTERNAL_SENSOR_KWL_FTF_HUMIDITY_1 = "externalSensorKwlFtfHumidity1";
    public final static String EXTERNAL_SENSOR_KWL_FTF_HUMIDITY_2 = "externalSensorKwlFtfHumidity2";
    public final static String EXTERNAL_SENSOR_KWL_FTF_HUMIDITY_3 = "externalSensorKwlFtfHumidity3";
    public final static String EXTERNAL_SENSOR_KWL_FTF_HUMIDITY_4 = "externalSensorKwlFtfHumidity4";
    public final static String EXTERNAL_SENSOR_KWL_FTF_HUMIDITY_5 = "externalSensorKwlFtfHumidity5";
    public final static String EXTERNAL_SENSOR_KWL_FTF_HUMIDITY_6 = "externalSensorKwlFtfHumidity6";
    public final static String EXTERNAL_SENSOR_KWL_FTF_HUMIDITY_7 = "externalSensorKwlFtfHumidity7";
    public final static String EXTERNAL_SENSOR_KWL_FTF_HUMIDITY_8 = "externalSensorKwlFtfHumidity8";
    public final static String EXTERNAL_SENSOR_KWL_FTF_TEMPERATURE_1 = "externalSensorKwlFtfTemperature1";
    public final static String EXTERNAL_SENSOR_KWL_FTF_TEMPERATURE_2 = "externalSensorKwlFtfTemperature2";
    public final static String EXTERNAL_SENSOR_KWL_FTF_TEMPERATURE_3 = "externalSensorKwlFtfTemperature3";
    public final static String EXTERNAL_SENSOR_KWL_FTF_TEMPERATURE_4 = "externalSensorKwlFtfTemperature4";
    public final static String EXTERNAL_SENSOR_KWL_FTF_TEMPERATURE_5 = "externalSensorKwlFtfTemperature5";
    public final static String EXTERNAL_SENSOR_KWL_FTF_TEMPERATURE_6 = "externalSensorKwlFtfTemperature6";
    public final static String EXTERNAL_SENSOR_KWL_FTF_TEMPERATURE_7 = "externalSensorKwlFtfTemperature7";
    public final static String EXTERNAL_SENSOR_KWL_FTF_TEMPERATURE_8 = "externalSensorKwlFtfTemperature8";
    public final static String EXTERNAL_SENSOR_KWL_CO2_1 = "externalSensorKwlCo21";
    public final static String EXTERNAL_SENSOR_KWL_CO2_2 = "externalSensorKwlCo22";
    public final static String EXTERNAL_SENSOR_KWL_CO2_3 = "externalSensorKwlCo23";
    public final static String EXTERNAL_SENSOR_KWL_CO2_4 = "externalSensorKwlCo24";
    public final static String EXTERNAL_SENSOR_KWL_CO2_5 = "externalSensorKwlCo25";
    public final static String EXTERNAL_SENSOR_KWL_CO2_6 = "externalSensorKwlCo26";
    public final static String EXTERNAL_SENSOR_KWL_CO2_7 = "externalSensorKwlCo27";
    public final static String EXTERNAL_SENSOR_KWL_CO2_8 = "externalSensorKwlCo28";
    public final static String EXTERNAL_SENSOR_KWL_VOC_1 = "externalSensorKwlVoc1";
    public final static String EXTERNAL_SENSOR_KWL_VOC_2 = "externalSensorKwlVoc2";
    public final static String EXTERNAL_SENSOR_KWL_VOC_3 = "externalSensorKwlVoc3";
    public final static String EXTERNAL_SENSOR_KWL_VOC_4 = "externalSensorKwlVoc4";
    public final static String EXTERNAL_SENSOR_KWL_VOC_5 = "externalSensorKwlVoc5";
    public final static String EXTERNAL_SENSOR_KWL_VOC_6 = "externalSensorKwlVoc6";
    public final static String EXTERNAL_SENSOR_KWL_VOC_7 = "externalSensorKwlVoc7";
    public final static String EXTERNAL_SENSOR_KWL_VOC_8 = "externalSensorKwlVoc8";
    public final static String NHZ_DUCT_SENSOR = "nhzDuctSensor";
    public final static String WEEK_PROFILE_NHZ = "weekProfileNhz";
    public final static String SER_NO = "serNo";
    public final static String PROD_CODE = "prodCode";
    public final static String SUPPLY_AIR_RPM = "supplyAirRpm";
    public final static String EXTRACT_AIR_RPM = "extractAirRpm";
    public final static String LOGOUT = "logout";
    public final static String HOLIDAY_PROGRAMME = "holidayProgramme";
    public final static String HOLIDAY_PROGRAMME_FAN_STAGE = "holidayProgrammeFanStage";
    public final static String HOLIDAY_PROGRAMME_START = "holidayProgrammeStart";
    public final static String HOLIDAY_PROGRAMME_END = "holidayProgrammeEnd";
    public final static String HOLIDAY_PROGRAMME_INTERVAL = "holidayProgrammeInterval";
    public final static String HOLIDAY_PROGRAMME_ACTIVATION_TIME = "holidayProgrammeActivationTime";
    public final static String VHZ_TYPE = "vhzType";
    public final static String FUNCTION_TYPE_KWL_EM = "functionTypeKwlEm";
    public final static String RUN_ON_TIME_VHZ_NHZ = "runOnTimeVhzNhz";
    public final static String EXTERNAL_CONTACT = "externalContact";
    public final static String ERROR_OUTPUT_FUNCTION = "errorOutputFunction";
    public final static String FILTER_CHANGE = "filterChange";
    public final static String FILTER_CHANGE_INTERVAL = "filterChangeInterval";
    public final static String FILTER_CHANGE_REMAINING_TIME = "filterChangeRemainingTime";
    public final static String FILTER_CHANGE_RESET = "filterChangeReset";
    public final static String BYPASS_ROOM_TEMPERATURE = "bypassRoomTemperature";
    public final static String BYPASS_MIN_OUTSIDE_TEMPERATURE = "bypassMinOutsideTemperature";
    public final static String TBD = "tbd";
    public final static String FACTORY_SETTING_WZU = "factorySettingWzu";
    public final static String FACTORY_RESET = "factoryReset";
    public final static String SUPPLY_AIR_FAN_STAGE = "supplyAirFanStage";
    public final static String EXTRACT_AIR_FAN_STAGE = "extractAirFanStage";
    public final static String FAN_STAGE_STEPPED_0TO2V = "fanStageStepped0to2v";
    public final static String FAN_STAGE_STEPPED_2TO4V = "fanStageStepped2to4v";
    public final static String FAN_STAGE_STEPPED_4TO6V = "fanStageStepped4to6v";
    public final static String FAN_STAGE_STEPPED_6TO8V = "fanStageStepped6to8v";
    public final static String FAN_STAGE_STEPPED_8TO10V = "fanStageStepped8to10v";
    public final static String OFFSET_EXTRACT_AIR = "offsetExtractAir";
    public final static String ASSIGNMENT_FAN_STAGES = "assignmentFanStages";
    public final static String SENSOR_NAME_HUMIDITY_AND_TEMP_1 = "sensorNameHumidityAndTemp1";
    public final static String SENSOR_NAME_HUMIDITY_AND_TEMP_2 = "sensorNameHumidityAndTemp2";
    public final static String SENSOR_NAME_HUMIDITY_AND_TEMP_3 = "sensorNameHumidityAndTemp3";
    public final static String SENSOR_NAME_HUMIDITY_AND_TEMP_4 = "sensorNameHumidityAndTemp4";
    public final static String SENSOR_NAME_HUMIDITY_AND_TEMP_5 = "sensorNameHumidityAndTemp5";
    public final static String SENSOR_NAME_HUMIDITY_AND_TEMP_6 = "sensorNameHumidityAndTemp6";
    public final static String SENSOR_NAME_HUMIDITY_AND_TEMP_7 = "sensorNameHumidityAndTemp7";
    public final static String SENSOR_NAME_HUMIDITY_AND_TEMP_8 = "sensorNameHumidityAndTemp8";
    public final static String SENSOR_NAME_CO2_1 = "sensorNameCo21";
    public final static String SENSOR_NAME_CO2_2 = "sensorNameCo22";
    public final static String SENSOR_NAME_CO2_3 = "sensorNameCo23";
    public final static String SENSOR_NAME_CO2_4 = "sensorNameCo24";
    public final static String SENSOR_NAME_CO2_5 = "sensorNameCo25";
    public final static String SENSOR_NAME_CO2_6 = "sensorNameCo26";
    public final static String SENSOR_NAME_CO2_7 = "sensorNameCo27";
    public final static String SENSOR_NAME_CO2_8 = "sensorNameCo28";
    public final static String SENSOR_NAME_VOC_1 = "sensorNameVoc1";
    public final static String SENSOR_NAME_VOC_2 = "sensorNameVoc2";
    public final static String SENSOR_NAME_VOC_3 = "sensorNameVoc3";
    public final static String SENSOR_NAME_VOC_4 = "sensorNameVoc4";
    public final static String SENSOR_NAME_VOC_5 = "sensorNameVoc5";
    public final static String SENSOR_NAME_VOC_6 = "sensorNameVoc6";
    public final static String SENSOR_NAME_VOC_7 = "sensorNameVoc7";
    public final static String SENSOR_NAME_VOC_8 = "sensorNameVoc8";
    public final static String SOFTWARE_VERSION_BASIS = "softwareVersionBasis";
    public final static String OPERATING_HOURS_SUPPLY_AIR_VENT = "operatingHoursSupplyAirVent";
    public final static String OPERATING_HOURS_EXTRACT_AIR_VENT = "operatingHoursExtractAirVent";
    public final static String OPERATING_HOURS_VHZ = "operatingHoursVhz";
    public final static String OPERATING_HOURS_NHZ = "operatingHoursNhz";
    public final static String OUTPUT_POWER_VHZ = "outputPowerVhz";
    public final static String OUTPUT_POWER_NHZ = "outputPowerNhz";
    public final static String RESET_FLAG = "resetFlag";
    public final static String ERRORS = "errors";
    public final static String WARNINGS = "warnings";
    public final static String INFOS = "infos";
    public final static String NO_OF_ERRORS = "noOfErrors";
    public final static String NO_OF_WARNINGS = "noOfWarnings";
    public final static String NO_OF_INFOS = "noOfInfos";
    public final static String ERRORS_MSG = "errorsMsg";
    public final static String WARNINGS_MSG = "warningsMsg";
    public final static String INFOS_MSG = "infosMsg";
    public final static String STATUS_FLAGS = "statusFlags";
    public final static String SENSOR_CONFIG_KWL_FTF_1 = "sensorConfigKwlFtf1";
    public final static String SENSOR_CONFIG_KWL_FTF_2 = "sensorConfigKwlFtf2";
    public final static String SENSOR_CONFIG_KWL_FTF_3 = "sensorConfigKwlFtf3";
    public final static String SENSOR_CONFIG_KWL_FTF_4 = "sensorConfigKwlFtf4";
    public final static String SENSOR_CONFIG_KWL_FTF_5 = "sensorConfigKwlFtf5";
    public final static String SENSOR_CONFIG_KWL_FTF_6 = "sensorConfigKwlFtf6";
    public final static String SENSOR_CONFIG_KWL_FTF_7 = "sensorConfigKwlFtf7";
    public final static String SENSOR_CONFIG_KWL_FTF_8 = "sensorConfigKwlFtf8";
    public final static String GLOBAL_MANUAL_WEB_UPDATE = "globalManualWebUpdate";
    public final static String PORTAL_GLOBALS_ERROR_FOR_WEB = "portalGlobalsErrorForWeb";
    public final static String CLEAR_ERROR = "clearError";
    public final static String BYPASS_STATUS = "bypassStatus";
    public final static String BYPASS_FROM_DAY = "bypassFromDay";
    public final static String BYPASS_FROM_MONTH = "bypassFromMonth";
    public final static String BYPASS_TO_DAY = "bypassToDay";
    public final static String BYPASS_TO_MONTH = "bypassToMonth";

    // List of all Configuration Parameters
    public final static String CONFIG_REFRESH_INTERVAL = "refreshInterval";

    // Other constants
    public final static int UNIT_ID = 180;
    public final static int START_ADDRESS = 1;
    public final static String VARIABLES_DEFINITION_FILE = "variables.json";
    public final static int MAX_TRIES = 5;
}
