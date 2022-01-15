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
package org.openhab.binding.kostalinverter.internal.thirdgeneration;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link ThirdGenerationBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author René Stakemeier - Initial contribution
 */
@NonNullByDefault
public class ThirdGenerationBindingConstants {

    private static final String BINDING_ID = "kostalinverter";

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
    public static final String COMMUNICATION_ERROR_USER_ACCOUNT_LOCKED = "Your user account on the device is locked. Please reset the password by following the instructions on the device´s web frontend";
    public static final String CONFIGURATION_ERROR_PASSWORD = "Wrong password";

    // List of all Channel uids
    public static final String CHANNEL_DEVICE_LOCAL_DC_POWER = "deviceLocalDCPower";
    public static final String CHANNEL_DEVICE_LOCAL_HOMECONSUMPTION_FROM_BATTERY = "deviceLocalHomeconsumptionFromBattery";
    public static final String CHANNEL_DEVICE_LOCAL_HOMECONSUMPTION_FROM_GRID = "deviceLocalHomeconsumptionFromGrid";
    public static final String CHANNEL_DEVICE_LOCAL_OWNCONSUMPTION = "deviceLocalOwnconsumption";
    public static final String CHANNEL_DEVICE_LOCAL_HOMECONSUMPTION_FROM_PV = "deviceLocalHomeconsumptionFromPV";
    public static final String CHANNEL_DEVICE_LOCAL_HOMECONSUMPTION_TOTAL = "deviceLocalHomeconsumptionTotal";
    public static final String CHANNEL_DEVICE_LOCAL_LIMIT_EVU_ABSOLUTE = "deviceLocalLimitEVUAbsolute";
    public static final String CHANNEL_DEVICE_LOCAL_LIMIT_EVU_RELATIV = "deviceLocalLimitEVURelativ";
    public static final String CHANNEL_DEVICE_LOCAL_WORKTIME = "deviceLocalWorktime";
    public static final String CHANNEL_DEVICE_LOCAL_AC_COS_PHI = "deviceLocalACCosPhi";
    public static final String CHANNEL_DEVICE_LOCAL_AC_FREQUENCY = "deviceLocalACFrequency";
    public static final String CHANNEL_DEVICE_LOCAL_AC_PHASE_1_CURRENT_AMPERAGE = "deviceLocalACPhase1CurrentAmperage";
    public static final String CHANNEL_DEVICE_LOCAL_AC_PHASE_1_CURRENT_POWER = "deviceLocalACPhase1CurrentPower";
    public static final String CHANNEL_DEVICE_LOCAL_AC_PHASE_1_CURRENT_VOLTAGE = "deviceLocalACPhase1CurrentVoltage";
    public static final String CHANNEL_DEVICE_LOCAL_AC_PHASE_2_CURRENT_AMPERAGE = "deviceLocalACPhase2CurrentAmperage";
    public static final String CHANNEL_DEVICE_LOCAL_AC_PHASE_2_CURRENT_POWER = "deviceLocalACPhase2CurrentPower";
    public static final String CHANNEL_DEVICE_LOCAL_AC_PHASE_2_CURRENT_VOLTAGE = "deviceLocalACPhase2CurrentVoltage";
    public static final String CHANNEL_DEVICE_LOCAL_AC_PHASE_3_CURRENT_AMPERAGE = "deviceLocalACPhase3CurrentAmperage";
    public static final String CHANNEL_DEVICE_LOCAL_AC_PHASE_3_CURRENT_POWER = "deviceLocalACPhase3CurrentPower";
    public static final String CHANNEL_DEVICE_LOCAL_AC_PHASE_3_CURRENT_VOLTAGE = "deviceLocalACPhase3CurrentVoltage";
    public static final String CHANNEL_DEVICE_LOCAL_AC_CURRENT_POWER = "deviceLocalACCurrentPower";
    public static final String CHANNEL_DEVICE_LOCAL_BATTERY_LOADING_CYCLES = "deviceLocalBatteryLoadingCycles";
    public static final String CHANNEL_DEVICE_LOCAL_BATTERY_FULL_CHARGE_CAPACITY = "deviceLocalBatteryFullChargeCapacity";
    public static final String CHANNEL_DEVICE_LOCAL_BATTERY_AMPERAGE = "deviceLocalBatteryAmperage";
    public static final String CHANNEL_DEVICE_LOCAL_BATTERY_POWER = "deviceLocalBatteryPower";
    public static final String CHANNEL_DEVICE_LOCAL_BATTERY_STATE_OF_CHARGE = "deviceLocalBatteryStageOfCharge";
    public static final String CHANNEL_DEVICE_LOCAL_BATTERY_VOLTAGE = "deviceLocalBatteryVoltage";
    public static final String CHANNEL_DEVICE_LOCAL_PVSTRING_1_AMPERAGE = "deviceLocalPVString1Amperage";
    public static final String CHANNEL_DEVICE_LOCAL_PVSTRING_1_POWER = "deviceLocalPVString1Power";
    public static final String CHANNEL_DEVICE_LOCAL_PVSTRING_1_VOLTAGE = "deviceLocalPVString1Voltage";
    public static final String CHANNEL_DEVICE_LOCAL_PVSTRING_2_AMPERAGE = "deviceLocalPVString2Amperage";
    public static final String CHANNEL_DEVICE_LOCAL_PVSTRING_2_POWER = "deviceLocalPVString2Power";
    public static final String CHANNEL_DEVICE_LOCAL_PVSTRING_2_VOLTAGE = "deviceLocalPVString2Voltage";
    public static final String CHANNEL_DEVICE_LOCAL_PVSTRING_3_AMPERAGE = "deviceLocalPVString3Amperage";
    public static final String CHANNEL_DEVICE_LOCAL_PVSTRING_3_POWER = "deviceLocalPVString3Power";
    public static final String CHANNEL_DEVICE_LOCAL_PVSTRING_3_VOLTAGE = "deviceLocalPVString3Voltage";
    public static final String CHANNEL_SCB_EVENT_ERROR_COUNT_MC = "SCBEventErrorCountMc";
    public static final String CHANNEL_SCB_EVENT_ERROR_COUNT_SFH = "SCBEventErrorCountSFH";
    public static final String CHANNEL_SCB_EVENT_ERROR_COUNT_SCB = "SCBEventErrorCountSCB";
    public static final String CHANNEL_SCB_EVENT_WARNING_COUNT_SCB = "SCBEventWarningCountSCB";
    public static final String CHANNEL_STATISTIC_AUTARKY_DAY = "statisticAutarkyDay";
    public static final String CHANNEL_STATISTIC_AUTARKY_MONTH = "statisticAutarkyMonth";
    public static final String CHANNEL_STATISTIC_AUTARKY_TOTAL = "statisticAutarkyTotal";
    public static final String CHANNEL_STATISTIC_AUTARKY_YEAR = "statisticAutarkyYear";
    public static final String CHANNEL_STATISTIC_CO2SAVING_DAY = "statisticCo2SavingDay";
    public static final String CHANNEL_STATISTIC_CO2SAVING_MONTH = "statisticCo2SavingMonth";
    public static final String CHANNEL_STATISTIC_CO2SAVING_TOTAL = "statisticCo2SavingTotal";
    public static final String CHANNEL_STATISTIC_CO2SAVING_YEAR = "statisticCo2SavingYear";
    public static final String CHANNEL_STATISTIC_HOMECONSUMPTION_DAY = "statisticHomeconsumptionDay";
    public static final String CHANNEL_STATISTIC_HOMECONSUMPTION_MONTH = "statisticHomeconsumptionMonth";
    public static final String CHANNEL_STATISTIC_HOMECONSUMPTION_TOTAL = "statisticHomeconsumptionTotal";
    public static final String CHANNEL_STATISTIC_HOMECONSUMPTION_YEAR = "statisticHomeconsumptionYear";
    public static final String CHANNEL_STATISTIC_HOMECONSUMPTION_FROM_BATTERIE_DAY = "statisticHomeconsumptionFromBatteryDay";
    public static final String CHANNEL_STATISTIC_HOMECONSUMPTION_FROM_BATTERIE_MONTH = "statisticHomeconsumptionFromBatteryMonth";
    public static final String CHANNEL_STATISTIC_HOMECONSUMPTION_FROM_BATTERIE_TOTAL = "statisticHomeconsumptionFromBatteryTotal";
    public static final String CHANNEL_STATISTIC_HOMECONSUMPTION_FROM_BATTERIE_YEAR = "statisticHomeconsumptionFromBatteryYear";
    public static final String CHANNEL_STATISTIC_HOMECONSUMPTION_FROM_GRID_DAY = "statisticHomeconsumptionFromGridDay";
    public static final String CHANNEL_STATISTIC_HOMECONSUMPTION_FROM_GRID_MONTH = "statisticHomeconsumptionFromGridMonth";
    public static final String CHANNEL_STATISTIC_HOMECONSUMPTION_FROM_GRID_TOTAL = "statisticHomeconsumptionFromGridTotal";
    public static final String CHANNEL_STATISTIC_HOMECONSUMPTION_FROM_GRID_YEAR = "statisticHomeconsumptionFromGridYear";
    public static final String CHANNEL_STATISTIC_HOMECONSUMPTION_FROM_PV_DAY = "statisticHomeconsumptionFromPVDay";
    public static final String CHANNEL_STATISTIC_HOMECONSUMPTION_FROM_PV_MONTH = "statisticHomeconsumptionFromPVMonth";
    public static final String CHANNEL_STATISTIC_HOMECONSUMPTION_FROM_PV_TOTAL = "statisticHomeconsumptionFromPVTotal";
    public static final String CHANNEL_STATISTIC_HOMECONSUMPTION_FROM_PV_YEAR = "statisticHomeconsumptionFromPVYear";
    public static final String CHANNEL_STATISTIC_OWNCONSUMPTION_RATE_DAY = "statisticOwnconsumptionRateDay";
    public static final String CHANNEL_STATISTIC_OWNCONSUMPTION_RATE_MONTH = "statisticOwnconsumptionRateMonth";
    public static final String CHANNEL_STATISTIC_OWNCONSUMPTION_RATE_TOTAL = "statisticOwnconsumptionRateTotal";
    public static final String CHANNEL_STATISTIC_OWNCONSUMPTION_RATE_YEAR = "statisticOwnconsumptionRateYear";
    public static final String CHANNEL_STATISTIC_YIELD_DAY = "statisticYieldDay";
    public static final String CHANNEL_STATISTIC_YIELD_MONTH = "statisticYieldMonth";
    public static final String CHANNEL_STATISTIC_YIELD_TOTAL = "statisticYieldTotal";
    public static final String CHANNEL_STATISTIC_YIELD_YEAR = "statisticYieldYear";
}
