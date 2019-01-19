/**
 * Copyright (c) 2010-2019 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.kostalpikoiqplenticore.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
 * The {@link KostalPikoIqPlenticoreBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author René Stakemeier - Initial contribution
 */
@NonNullByDefault
public class KostalPikoIqPlenticoreBindingConstants {

    private static final String BINDING_ID = "kostalpikoiqplenticore";

    // List of all constants used for the authentication
    static final String USER_TYPE = "user";
    static final String HMAC_SHA256_ALGORITHM = "HMACSHA256";
    static final String SHA_256_HASH = "SHA-256";
    static final int AES_GCM_TAG_LENGTH = 128; // bit count

    // List of all Thing Type UIDs
    public static final ThingTypeUID PIKOIQ42 = new ThingTypeUID(BINDING_ID, "PIKOIQ42");
    public static final ThingTypeUID PIKOIQ55 = new ThingTypeUID(BINDING_ID, "PIKOIQ55");
    public static final ThingTypeUID PIKOIQ70 = new ThingTypeUID(BINDING_ID, "PIKOIQ70");
    public static final ThingTypeUID PIKOIQ85 = new ThingTypeUID(BINDING_ID, "PIKOIQ85");
    public static final ThingTypeUID PIKOIQ100 = new ThingTypeUID(BINDING_ID, "PIKOIQ100");
    public static final ThingTypeUID PLENTICOREPLUS42WITHOUTBATTERY = new ThingTypeUID(BINDING_ID,
            "PLENTICOREPLUS42WITHOUTBATTERY");
    public static final ThingTypeUID PLENTICOREPLUS55WITHOUTBATTERY = new ThingTypeUID(BINDING_ID,
            "PLENTICOREPLUS55WITHOUTBATTERY");
    public static final ThingTypeUID PLENTICOREPLUS70WITHOUTBATTERY = new ThingTypeUID(BINDING_ID,
            "PLENTICOREPLUS70WITHOUTBATTERY");
    public static final ThingTypeUID PLENTICOREPLUS85WITHOUTBATTERY = new ThingTypeUID(BINDING_ID,
            "PLENTICOREPLUS85WITHOUTBATTERY");
    public static final ThingTypeUID PLENTICOREPLUS100WITHOUTBATTERY = new ThingTypeUID(BINDING_ID,
            "PLENTICOREPLUS100WITHOUTBATTERY");
    public static final ThingTypeUID PLENTICOREPLUS42WITHBATTERY = new ThingTypeUID(BINDING_ID,
            "PLENTICOREPLUS42WITHBATTERY");
    public static final ThingTypeUID PLENTICOREPLUS55WITHBATTERY = new ThingTypeUID(BINDING_ID,
            "PLENTICOREPLUS55WITHBATTERY");
    public static final ThingTypeUID PLENTICOREPLUS70WITHBATTERY = new ThingTypeUID(BINDING_ID,
            "PLENTICOREPLUS70WITHBATTERY");
    public static final ThingTypeUID PLENTICOREPLUS85WITHBATTERY = new ThingTypeUID(BINDING_ID,
            "PLENTICOREPLUS85WITHBATTERY");
    public static final ThingTypeUID PLENTICOREPLUS100WITHBATTERY = new ThingTypeUID(BINDING_ID,
            "PLENTICOREPLUS100WITHBATTERY");

    // List of error messages
    public static final String COMMUNICATION_ERROR_AUTHENTICATION = "Error during the initialisation of the authentication";
    public static final String COMMUNICATION_ERROR_UNSUPPORTED_ENCODING = "The text encoding is not supported by this openHAB installation";
    public static final String COMMUNICATION_ERROR_AES_ERROR = "The java installation does not support AES encryption in GCM mode";
    public static final String COMMUNICATION_ERROR_HTTP = "HTTP communication error: No response from device";
    public static final String COMMUNICATION_ERROR_JSON = "HTTP communication error: answer did not match the expected format";
    public static final String COMMUNICATION_ERROR_API_CHANGED = "The API seems to have changed :-( Maybe this implementation has become incompatible with the device";
    public static final String COMMUNICATION_ERROR_INCOMPATIBLE_DEVICE = "The device could not provide the required information. Please check, if you selected the right thing for your device!";
    public static final String COMMUNICATION_ERROR_USER_ACCOUNT_LOCKED = "Your user account on the device is logged. Please reset the password by following the instructions on the device´s web frontend";
    public static final String CONFIGURATION_ERROR_PASSWORD = "Wrong password";

    // List of all Channel uids
    public static final String CHANNEL_DEVICE_LOCAL_INTERVERTER_STATE = "DEVICE_LOCAL_INTERVERTER_STATE";
    public static final String CHANNEL_DEVICE_LOCAL_DC_POWER = "DEVICE_LOCAL_DC_POWER";
    public static final String CHANNEL_DEVICE_LOCAL_HOMECONSUMPTION_FROM_BATTERY = "DEVICE_LOCAL_HOMECONSUMPTION_FROM_BATTERY";
    public static final String CHANNEL_DEVICE_LOCAL_HOMECONSUMPTION_FROM_GRID = "DEVICE_LOCAL_HOMECONSUMPTION_FROM_GRID";
    public static final String CHANNEL_DEVICE_LOCAL_OWNCONSUMPTION = "DEVICE_LOCAL_OWNCONSUMPTION";
    public static final String CHANNEL_DEVICE_LOCAL_HOMECONSUMPTION_FROM_PV = "DEVICE_LOCAL_HOMECONSUMPTION_FROM_PV";
    public static final String CHANNEL_DEVICE_LOCAL_HOMECONSUMPTION_TOTAL = "DEVICE_LOCAL_HOMECONSUMPTION_TOTAL";
    public static final String CHANNEL_DEVICE_LOCAL_LIMIT_EVU_ABSOLUTE = "DEVICE_LOCAL_LIMIT_EVU_ABSOLUTE";
    public static final String CHANNEL_DEVICE_LOCAL_LIMIT_EVU_RELATIV = "DEVICE_LOCAL_LIMIT_EVU_RELATIV";
    public static final String CHANNEL_DEVICE_LOCAL_WORKTIME = "DEVICE_LOCAL_WORKTIME";
    public static final String CHANNEL_DEVICE_LOCAL_AC_PHASE_1_CURRENT_AMPERAGE = "DEVICE_LOCAL_AC_PHASE_1_CURRENT_AMPERAGE";
    public static final String CHANNEL_DEVICE_LOCAL_AC_PHASE_1_CURRENT_POWER = "DEVICE_LOCAL_AC_PHASE_1_CURRENT_POWER";
    public static final String CHANNEL_DEVICE_LOCAL_AC_PHASE_1_CURRENT_VOLTAGE = "DEVICE_LOCAL_AC_PHASE_1_CURRENT_VOLTAGE";
    public static final String CHANNEL_DEVICE_LOCAL_AC_PHASE_2_CURRENT_AMPERAGE = "DEVICE_LOCAL_AC_PHASE_2_CURRENT_AMPERAGE";
    public static final String CHANNEL_DEVICE_LOCAL_AC_PHASE_2_CURRENT_POWER = "DEVICE_LOCAL_AC_PHASE_2_CURRENT_POWER";
    public static final String CHANNEL_DEVICE_LOCAL_AC_PHASE_2_CURRENT_VOLTAGE = "DEVICE_LOCAL_AC_PHASE_2_CURRENT_VOLTAGE";
    public static final String CHANNEL_DEVICE_LOCAL_AC_PHASE_3_CURRENT_AMPERAGE = "DEVICE_LOCAL_AC_PHASE_3_CURRENT_AMPERAGE";
    public static final String CHANNEL_DEVICE_LOCAL_AC_PHASE_3_CURRENT_POWER = "DEVICE_LOCAL_AC_PHASE_3_CURRENT_POWER";
    public static final String CHANNEL_DEVICE_LOCAL_AC_PHASE_3_CURRENT_VOLTAGE = "DEVICE_LOCAL_AC_PHASE_3_CURRENT_VOLTAGE";
    public static final String CHANNEL_DEVICE_LOCAL_AC_CURRENT_POWER = "DEVICE_LOCAL_AC_CURRENT_POWER";
    public static final String CHANNEL_DEVICE_LOCAL_BATTERY_LOADING_CYCLES = "DEVICE_LOCAL_BATTERY_LOADING_CYCLES";
    public static final String CHANNEL_DEVICE_LOCAL_BATTERY_FULL_CHARGE_CAPACITY = "DEVICE_LOCAL_BATTERY_FULL_CHARGE_CAPACITY";
    public static final String CHANNEL_DEVICE_LOCAL_BATTERY_AMPERAGE = "DEVICE_LOCAL_BATTERY_AMPERAGE";
    public static final String CHANNEL_DEVICE_LOCAL_BATTERY_POWER = "DEVICE_LOCAL_BATTERY_POWER";
    public static final String CHANNEL_DEVICE_LOCAL_BATTERY_STATE_OF_CHARGE = "DEVICE_LOCAL_BATTERY_STATE_OF_CHARGE";
    public static final String CHANNEL_DEVICE_LOCAL_BATTERY_VOLTAGE = "DEVICE_LOCAL_BATTERY_VOLTAGE";
    public static final String CHANNEL_DEVICE_LOCAL_PVSTRING_1_AMPERAGE = "DEVICE_LOCAL_PVSTRING_1_AMPERAGE";
    public static final String CHANNEL_DEVICE_LOCAL_PVSTRING_1_POWER = "DEVICE_LOCAL_PVSTRING_1_POWER";
    public static final String CHANNEL_DEVICE_LOCAL_PVSTRING_1_VOLTAGE = "DEVICE_LOCAL_PVSTRING_1_VOLTAGE";
    public static final String CHANNEL_DEVICE_LOCAL_PVSTRING_2_AMPERAGE = "DEVICE_LOCAL_PVSTRING_2_AMPERAGE";
    public static final String CHANNEL_DEVICE_LOCAL_PVSTRING_2_POWER = "DEVICE_LOCAL_PVSTRING_2_POWER";
    public static final String CHANNEL_DEVICE_LOCAL_PVSTRING_2_VOLTAGE = "DEVICE_LOCAL_PVSTRING_2_VOLTAGE";
    public static final String CHANNEL_DEVICE_LOCAL_PVSTRING_3_AMPERAGE = "DEVICE_LOCAL_PVSTRING_3_AMPERAGE";
    public static final String CHANNEL_DEVICE_LOCAL_PVSTRING_3_POWER = "DEVICE_LOCAL_PVSTRING_3_POWER";
    public static final String CHANNEL_DEVICE_LOCAL_PVSTRING_3_VOLTAGE = "DEVICE_LOCAL_PVSTRING_3_VOLTAGE";
    public static final String CHANNEL_SCB_EVENT_ERROR_COUNT_MC = "SCB_EVENT_ERROR_COUNT_MC";
    public static final String CHANNEL_SCB_EVENT_ERROR_COUNT_SFH = "SCB_EVENT_ERROR_COUNT_SFH";
    public static final String CHANNEL_SCB_EVENT_ERROR_COUNT_SCB = "SCB_EVENT_ERROR_COUNT_SCB";
    public static final String CHANNEL_SCB_EVENT_WARNING_COUNT_SCB = "SCB_EVENT_WARNING_COUNT_SCB";
    public static final String CHANNEL_STATISTIC_AUTARKY_DAY = "STATISTIC_AUTARKY_DAY";
    public static final String CHANNEL_STATISTIC_AUTARKY_MONTH = "STATISTIC_AUTARKY_MONTH";
    public static final String CHANNEL_STATISTIC_AUTARKY_TOTAL = "STATISTIC_AUTARKY_TOTAL";
    public static final String CHANNEL_STATISTIC_AUTARKY_YEAR = "STATISTIC_AUTARKY_YEAR";
    public static final String CHANNEL_STATISTIC_CO2SAVING_DAY = "STATISTIC_CO2SAVING_DAY";
    public static final String CHANNEL_STATISTIC_CO2SAVING_MONTH = "STATISTIC_CO2SAVING_MONTH";
    public static final String CHANNEL_STATISTIC_CO2SAVING_TOTAL = "STATISTIC_CO2SAVING_TOTAL";
    public static final String CHANNEL_STATISTIC_CO2SAVING_YEAR = "STATISTIC_CO2SAVING_YEAR";
    public static final String CHANNEL_STATISTIC_HOMECONSUMPTION_DAY = "STATISTIC_HOMECONSUMPTION_DAY";
    public static final String CHANNEL_STATISTIC_HOMECONSUMPTION_MONTH = "STATISTIC_HOMECONSUMPTION_MONTH";
    public static final String CHANNEL_STATISTIC_HOMECONSUMPTION_TOTAL = "STATISTIC_HOMECONSUMPTION_TOTAL";
    public static final String CHANNEL_STATISTIC_HOMECONSUMPTION_YEAR = "STATISTIC_HOMECONSUMPTION_YEAR";
    public static final String CHANNEL_STATISTIC_HOMECONSUMPTION_FROM_BATTERIE_DAY = "STATISTIC_HOMECONSUMPTION_FROM_BATTERIE_DAY";
    public static final String CHANNEL_STATISTIC_HOMECONSUMPTION_FROM_BATTERIE_MONTH = "STATISTIC_HOMECONSUMPTION_FROM_BATTERIE_MONTH";
    public static final String CHANNEL_STATISTIC_HOMECONSUMPTION_FROM_BATTERIE_TOTAL = "STATISTIC_HOMECONSUMPTION_FROM_BATTERIE_TOTAL";
    public static final String CHANNEL_STATISTIC_HOMECONSUMPTION_FROM_BATTERIE_YEAR = "STATISTIC_HOMECONSUMPTION_FROM_BATTERIE_YEAR";
    public static final String CHANNEL_STATISTIC_HOMECONSUMPTION_FROM_GRID_DAY = "STATISTIC_HOMECONSUMPTION_FROM_GRID_DAY";
    public static final String CHANNEL_STATISTIC_HOMECONSUMPTION_FROM_GRID_MONTH = "STATISTIC_HOMECONSUMPTION_FROM_GRID_MONTH";
    public static final String CHANNEL_STATISTIC_HOMECONSUMPTION_FROM_GRID_TOTAL = "STATISTIC_HOMECONSUMPTION_FROM_GRID_TOTAL";
    public static final String CHANNEL_STATISTIC_HOMECONSUMPTION_FROM_GRID_YEAR = "STATISTIC_HOMECONSUMPTION_FROM_GRID_YEAR";
    public static final String CHANNEL_STATISTIC_HOMECONSUMPTION_FROM_PV_DAY = "STATISTIC_HOMECONSUMPTION_FROM_PV_DAY";
    public static final String CHANNEL_STATISTIC_HOMECONSUMPTION_FROM_PV_MONTH = "STATISTIC_HOMECONSUMPTION_FROM_PV_MONTH";
    public static final String CHANNEL_STATISTIC_HOMECONSUMPTION_FROM_PV_TOTAL = "STATISTIC_HOMECONSUMPTION_FROM_PV_TOTAL";
    public static final String CHANNEL_STATISTIC_HOMECONSUMPTION_FROM_PV_YEAR = "STATISTIC_HOMECONSUMPTION_FROM_PV_YEAR";
    public static final String CHANNEL_STATISTIC_OWNCONSUMPTION_RATE_DAY = "STATISTIC_OWNCONSUMPTION_RATE_DAY";
    public static final String CHANNEL_STATISTIC_OWNCONSUMPTION_RATE_MONTH = "STATISTIC_OWNCONSUMPTION_RATE_MONTH";
    public static final String CHANNEL_STATISTIC_OWNCONSUMPTION_RATE_TOTAL = "STATISTIC_OWNCONSUMPTION_RATE_TOTAL";
    public static final String CHANNEL_STATISTIC_OWNCONSUMPTION_RATE_YEAR = "STATISTIC_OWNCONSUMPTION_RATE_YEAR";
    public static final String CHANNEL_STATISTIC_YIELD_DAY = "STATISTIC_YIELD_DAY";
    public static final String CHANNEL_STATISTIC_YIELD_MONTH = "STATISTIC_YIELD_MONTH";
    public static final String CHANNEL_STATISTIC_YIELD_TOTAL = "STATISTIC_YIELD_TOTAL";
    public static final String CHANNEL_STATISTIC_YIELD_YEAR = "STATISTIC_YIELD_YEAR";

}
