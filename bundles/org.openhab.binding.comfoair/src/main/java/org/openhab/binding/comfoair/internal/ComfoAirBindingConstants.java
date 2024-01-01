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
package org.openhab.binding.comfoair.internal;

import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link ComfoAirBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Hans Böhm - Initial contribution
 */
@NonNullByDefault
public class ComfoAirBindingConstants {

    private static final String BINDING_ID = "comfoair";

    public static final ThingTypeUID THING_TYPE_COMFOAIR_GENERIC = new ThingTypeUID(BINDING_ID, "comfoair");
    public static final ThingTypeUID THING_TYPE_COMFOAIR_WHR930 = new ThingTypeUID(BINDING_ID, "WHR930");

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Set.of(THING_TYPE_COMFOAIR_GENERIC,
            THING_TYPE_COMFOAIR_WHR930);

    // Thing properties
    public static final String PROPERTY_SOFTWARE_MAIN_VERSION = "SOFTWARE_VERSION_MAIN";
    public static final String PROPERTY_SOFTWARE_MINOR_VERSION = "SOFTWARE_VERSION_MINOR";
    public static final String PROPERTY_DEVICE_NAME = "DEVICE_NAME";

    // Channel groups
    public static final String CG_CONTROL_PREFIX = "bindingControl#";
    public static final String CG_VENTILATION_PREFIX = "ventilation#";
    public static final String CG_TEMPS_PREFIX = "temperatures#";
    public static final String CG_TIMES_PREFIX = "times#";
    public static final String CG_BYPASS_PREFIX = "bypass#";
    public static final String CG_PREHEATER_PREFIX = "preheater#";
    public static final String CG_GHX_PREFIX = "ewt#";
    public static final String CG_HEATER_PREFIX = "heater#";
    public static final String CG_COOKERHOOD_PREFIX = "cookerhood#";
    public static final String CG_ENTHALPY_PREFIX = "enthalpy#";
    public static final String CG_OPTIONS_PREFIX = "options#";
    public static final String CG_MENUP1_PREFIX = "menuP1#";
    public static final String CG_MENUP2_PREFIX = "menuP2#";
    public static final String CG_MENUP9_PREFIX = "menuP9#";
    public static final String CG_INPUTS_PREFIX = "inputs#";
    public static final String CG_ANALOG1_PREFIX = "analog1#";
    public static final String CG_ANALOG2_PREFIX = "analog2#";
    public static final String CG_ANALOG3_PREFIX = "analog3#";
    public static final String CG_ANALOG4_PREFIX = "analog4#";
    public static final String CG_ANALOGRF_PREFIX = "analogRF#";
    public static final String CG_ERRORS_PREFIX = "errors#";
    public static final String CG_RESETS_PREFIX = "resets#";

    // Channels
    // Control channels
    public static final String CHANNEL_ACTIVATE = "activate";
    // Ventilation channels
    public static final String CHANNEL_FAN_LEVEL = "fanLevel";
    public static final String CHANNEL_FAN_OUT_0 = "fanOut0";
    public static final String CHANNEL_FAN_OUT_1 = "fanOut1";
    public static final String CHANNEL_FAN_OUT_2 = "fanOut2";
    public static final String CHANNEL_FAN_OUT_3 = "fanOut3";
    public static final String CHANNEL_FAN_IN_0 = "fanIn0";
    public static final String CHANNEL_FAN_IN_1 = "fanIn1";
    public static final String CHANNEL_FAN_IN_2 = "fanIn2";
    public static final String CHANNEL_FAN_IN_3 = "fanIn3";
    public static final String CHANNEL_FAN_IN_PERCENT = "fanInPercent";
    public static final String CHANNEL_FAN_OUT_PERCENT = "fanOutPercent";
    public static final String CHANNEL_FAN_IN_RPM = "fanInRPM";
    public static final String CHANNEL_FAN_OUT_RPM = "fanOutRPM";
    // Temperature channels
    public static final String CHANNEL_TEMP_TARGET = "targetTemperature";
    public static final String CHANNEL_TEMP_OUTDOOR_IN = "outdoorTemperatureIn";
    public static final String CHANNEL_TEMP_OUTDOOR_OUT = "outdoorTemperatureOut";
    public static final String CHANNEL_TEMP_INDOOR_IN = "indoorTemperatureIn";
    public static final String CHANNEL_TEMP_INDOOR_OUT = "indoorTemperatureOut";
    public static final String CHANNEL_IS_SENSOR_T1 = "isT1Sensor";
    public static final String CHANNEL_IS_SENSOR_T2 = "isT2Sensor";
    public static final String CHANNEL_IS_SENSOR_T3 = "isT3Sensor";
    public static final String CHANNEL_IS_SENSOR_T4 = "isT4Sensor";
    public static final String CHANNEL_IS_SENSOR_GHX = "isEWTSensor";
    public static final String CHANNEL_IS_SENSOR_HEATER = "isHeaterSensor";
    public static final String CHANNEL_IS_SENSOR_COOKERHOOD = "isCookerhoodSensor";
    public static final String CHANNEL_TEMP_GHX = "ewtTemperature";
    public static final String CHANNEL_TEMP_HEATER = "heaterTemperature";
    public static final String CHANNEL_TEMP_COOKERHOOD = "cookerhoodTemperature";
    // Time channels
    public static final String CHANNEL_TIME_LEVEL0 = "level0Time";
    public static final String CHANNEL_TIME_LEVEL1 = "level1Time";
    public static final String CHANNEL_TIME_LEVEL2 = "level2Time";
    public static final String CHANNEL_TIME_LEVEL3 = "level3Time";
    public static final String CHANNEL_TIME_FREEZE = "freezeTime";
    public static final String CHANNEL_TIME_PREHEATER = "preheaterTime";
    public static final String CHANNEL_TIME_BYPASS = "bypassTime";
    public static final String CHANNEL_TIME_FILTER = "filterHours";
    // Bypass channels
    public static final String CHANNEL_BYPASS_FACTOR = "bypassFactor";
    public static final String CHANNEL_BYPASS_LEVEL = "bypassLevel";
    public static final String CHANNEL_BYPASS_CORRECTION = "bypassCorrection";
    public static final String CHANNEL_BYPASS_SUMMER = "bypassSummer";
    // Preheater channels
    public static final String CHANNEL_PREHEATER_VALVE = "preheaterValve";
    public static final String CHANNEL_PREHEATER_FROST_PROTECT = "preheaterFrostProtect";
    public static final String CHANNEL_PREHEATER_HEATING = "preheaterHeating";
    public static final String CHANNEL_PREHEATER_FROST_TIME = "preheaterFrostTime";
    public static final String CHANNEL_PREHEATER_SAFETY = "preheaterSafety";
    // GHX channels
    public static final String CHANNEL_GHX_TEMP_LOW = "ewtTemperatureLow";
    public static final String CHANNEL_GHX_TEMP_HIGH = "ewtTemperatureHigh";
    public static final String CHANNEL_GHX_SPEED = "ewtSpeed";
    // Heater channels
    public static final String CHANNEL_HEATER_POWER = "heaterPower";
    public static final String CHANNEL_HEATER_POWER_I = "heaterPowerI";
    public static final String CHANNEL_HEATER_TEMP_TARGET = "heaterTargetTemperature";
    // Cookerhood channels
    public static final String CHANNEL_COOKERHOOD_SPEED = "cookerhoodSpeed";
    // Enthalpy channels
    public static final String CHANNEL_ENTHALPY_TEMP = "enthalpyTemperature";
    public static final String CHANNEL_ENTHALPY_HUMIDITY = "enthalpyHumidity";
    public static final String CHANNEL_ENTHALPY_LEVEL = "enthalpyLevel";
    public static final String CHANNEL_ENTHALPY_TIME = "enthalpyTime";
    // Options channels
    public static final String CHANNEL_OPTION_PREHEATER = "isPreheater";
    public static final String CHANNEL_OPTION_BYPASS = "isBypass";
    public static final String CHANNEL_OPTION_RECU_TYPE = "recuType";
    public static final String CHANNEL_OPTION_RECU_SIZE = "recuSize";
    public static final String CHANNEL_OPTION_CHIMNEY = "isChimney";
    public static final String CHANNEL_OPTION_COOKERHOOD = "isCookerhood";
    public static final String CHANNEL_OPTION_HEATER = "isHeater";
    public static final String CHANNEL_OPTION_ENTHALPY = "isEnthalpy";
    public static final String CHANNEL_OPTION_GHX = "isEWT";
    public static final String CHANNEL_OPTION_PRIORITY = "analogPriority";
    // Menu P1 channels
    public static final String CHANNEL_MENU20_MODE = "menu20Mode";
    public static final String CHANNEL_MENU21_MODE = "menu21Mode";
    public static final String CHANNEL_MENU22_MODE = "menu22Mode";
    public static final String CHANNEL_MENU23_MODE = "menu23Mode";
    public static final String CHANNEL_MENU24_MODE = "menu24Mode";
    public static final String CHANNEL_MENU25_MODE = "menu25Mode";
    public static final String CHANNEL_MENU26_MODE = "menu26Mode";
    public static final String CHANNEL_MENU27_MODE = "menu27Mode";
    public static final String CHANNEL_MENU28_MODE = "menu28Mode";
    public static final String CHANNEL_MENU29_MODE = "menu29Mode";
    // Menu P2 channels
    public static final String CHANNEL_BR_START_DELAY = "bathroomStartDelay";
    public static final String CHANNEL_BR_END_DELAY = "bathroomEndDelay";
    public static final String CHANNEL_L1_END_DELAY = "L1EndDelay";
    public static final String CHANNEL_PULSE_VENTILATION = "pulseVentilation";
    public static final String CHANNEL_FILTER_WEEKS = "filterWeeks";
    public static final String CHANNEL_RF_SHORT_DELAY = "RFShortDelay";
    public static final String CHANNEL_RF_LONG_DELAY = "RFLongDelay";
    public static final String CHANNEL_COOKERHOOD_DELAY = "cookerhoodDelay";
    // Menu P9 channels
    public static final String CHANNEL_CHIMNEY_STATE = "chimneyState";
    public static final String CHANNEL_BYPASS_STATE = "bypassState";
    public static final String CHANNEL_GHX_STATE = "ewtState";
    public static final String CHANNEL_HEATER_STATE = "heaterState";
    public static final String CHANNEL_VCONTROL_STATE = "vControlState";
    public static final String CHANNEL_FROST_STATE = "frostState";
    public static final String CHANNEL_COOKERHOOD_STATE = "cookerhoodState";
    public static final String CHANNEL_ENTHALPY_STATE = "enthalpyState";
    // Inputs channels
    public static final String CHANNEL_IS_L1_SWITCH = "isL1Switch";
    public static final String CHANNEL_IS_L2_SWITCH = "isL2Switch";
    public static final String CHANNEL_IS_BATHROOM_SWITCH = "isBathroomSwitch";
    public static final String CHANNEL_IS_COOKERHOOD_SWITCH = "isCookerhoodSwitch";
    public static final String CHANNEL_IS_EXTERNAL_FILTER = "isExternalFilter";
    public static final String CHANNEL_IS_WTW = "isWTW";
    public static final String CHANNEL_IS_BATHROOM2_SWITCH = "isBathroom2Switch";
    // Analog channels
    public static final String CHANNEL_IS_ANALOG = "isAnalog";
    public static final String CHANNEL_ANALOG_MODE = "analogMode";
    public static final String CHANNEL_ANALOG_NEGATIVE = "analogNegative";
    public static final String CHANNEL_ANALOG_VOLT = "analogVolt";
    public static final String CHANNEL_ANALOG_MIN = "analogMin";
    public static final String CHANNEL_ANALOG_MAX = "analogMax";
    public static final String CHANNEL_ANALOG_VALUE = "analogValue";
    public static final String CHANNEL_IS_RF = "isRF";
    public static final String CHANNEL_RF_MODE = "RFMode";
    public static final String CHANNEL_RF_NEGATIVE = "RFNegative";
    public static final String CHANNEL_RF_MIN = "RFMin";
    public static final String CHANNEL_RF_MAX = "RFMax";
    public static final String CHANNEL_RF_VALUE = "RFValue";
    // Error channels
    public static final String CHANNEL_FILTER_ERROR = "filterError";
    public static final String CHANNEL_ERRORS_CURRENT = "errorsCurrent";
    public static final String CHANNEL_ERRORS_LAST = "errorsLast";
    public static final String CHANNEL_ERRORS_PRELAST = "errorsPrelast";
    public static final String CHANNEL_ERRORS_PREPRELAST = "errorsPrePrelast";
    // Reset channels
    public static final String CHANNEL_FILTER_RESET = "filterReset";
    public static final String CHANNEL_ERROR_RESET = "errorReset";
}
