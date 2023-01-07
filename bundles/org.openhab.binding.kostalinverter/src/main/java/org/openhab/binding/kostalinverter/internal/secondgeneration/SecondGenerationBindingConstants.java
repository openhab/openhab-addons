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
package org.openhab.binding.kostalinverter.internal.secondgeneration;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link SecondGenerationBindingConstants} class defines channel constants, which are
 * used in the second generation part of the binding.
 *
 * @author Örjan Backsell - Initial contribution Piko1020, Piko New Generation
 */

@NonNullByDefault
public class SecondGenerationBindingConstants {

    private static final String BINDING_ID = "kostalinverter";

    // List of all Thing Type UIDs
    public static final ThingTypeUID SECOND_GENERATION_INVERTER = new ThingTypeUID(BINDING_ID, "piko1020");

    // List of all Channel ids
    public static final String CHANNEL_GRIDOUTPUTPOWER = "gridOutputPower";
    public static final String CHANNEL_YIELD_DAY_SECOND_GEN = "yieldDaySecondGen";
    public static final String CHANNEL_YIELD_TOTAL_SECOND_GEN = "yieldTotalSecondGen";
    public static final String CHANNEL_OPERATING_STATUS = "operatingStatus";
    public static final String CHANNEL_GRIDVOLTAGEL1 = "gridVoltageL1";
    public static final String CHANNEL_GRIDCURRENTL1 = "gridCurrentL1";
    public static final String CHANNEL_GRIDPOWERL1 = "gridPowerL1";
    public static final String CHANNEL_GRIDVOLTAGEL2 = "gridVoltageL2";
    public static final String CHANNEL_GRIDCURRENTL2 = "gridCurrentL2";
    public static final String CHANNEL_GRIDPOWERL2 = "gridPowerL2";
    public static final String CHANNEL_GRIDVOLTAGEL3 = "gridVoltageL3";
    public static final String CHANNEL_GRIDCURRENTL3 = "gridCurrentL3";
    public static final String CHANNEL_GRIDPOWERL3 = "gridPowerL3";
    public static final String CHANNEL_DCPOWERPV = "dcPowerPV";
    public static final String CHANNEL_DC1VOLTAGE = "dc1Voltage";
    public static final String CHANNEL_DC1CURRENT = "dc1Current";
    public static final String CHANNEL_DC1POWER = "dc1Power";
    public static final String CHANNEL_DC2VOLTAGE = "dc2Voltage";
    public static final String CHANNEL_DC2CURRENT = "dc2Current";
    public static final String CHANNEL_DC2POWER = "dc2Power";
    public static final String CHANNEL_DC3VOLTAGE = "dc3Voltage";
    public static final String CHANNEL_DC3CURRENT = "dc3Current";
    public static final String CHANNEL_DC3POWER = "dc3Power";

    public static final String CHANNEL_AKTHOMECONSUMTIONSOLAR = "aktHomeConsumptionSolar";
    public static final String CHANNEL_AKTHOMECONSUMPTIONBAT = "aktHomeConsumptionBat";
    public static final String CHANNEL_AKTHOMECONSUMPTIONGRID = "aktHomeConsumptionGrid";
    public static final String CHANNEL_PHASESELHOMECONSUMPL1 = "phaseSelHomeConsumpL1";
    public static final String CHANNEL_PHASESELHOMECONSUMPL2 = "phaseSelHomeConsumpL2";
    public static final String CHANNEL_PHASESELHOMECONSUMPL3 = "phaseSelHomeConsumpL3";
    public static final String CHANNEL_GRIDFREQ = "gridFreq";
    public static final String CHANNEL_GRIDCOSPHI = "gridCosPhi";
    public static final String CHANNEL_HOMECONSUMPTION_DAY = "homeConsumptionDay";
    public static final String CHANNEL_OWNCONSUMPTION_DAY = "ownConsumptionDay";
    public static final String CHANNEL_OWNCONSRATE_DAY = "ownConsRateDay";
    public static final String CHANNEL_AUTONOMYDEGREE_DAY = "autonomyDegreeDay";
    public static final String CHANNEL_HOMECONSUMPTION_TOTAL = "homeConsumptionTotal";
    public static final String CHANNEL_OWNCONSUMPTION_TOTAL = "ownConsumptionTotal";
    public static final String CHANNEL_OPERATINGTIME_TOTAL = "operatingTimeTotal";
    public static final String CHANNEL_CURRENT = "current";
    public static final String CHANNEL_CURRENTDIR = "currentDir";
    public static final String CHANNEL_CHARGECYCLES = "chargeCycles";
    public static final String CHANNEL_BATTERYTEMPERATURE = "batteryTemperature";
    public static final String CHANNEL_LOGINTERVAL = "loginterval";
    public static final String CHANNEL_S0INPULSECNT = "s0InPulseCnt";
    public static final String CHANNEL_OWNCONSRATE_TOTAL = "ownConsRateTotal";
    public static final String CHANNEL_AUTONOMYDEGREE_TOTAL = "autonomyDegreeTotal";

    public static final String CHANNEL_BATTERYVOLTAGE = "batteryVoltage";
    public static final String CHANNEL_BATSTATEOFCHARGE = "batStateOfCharge";
    public static final String CHANNEL_SELFCONSUMPTION = "selfConsumption";

    public static final String CHANNEL_BATTERYUSAGECONSUMPTION = "batteryUsageConsumption";
    public static final String CHANNEL_SMARTBATTERYCONTROL = "smartBatteryControl";
    public static final String CHANNEL_MAXDEPTHOFDISCHARGE = "maxDepthOfDischarge";
    public static final String CHANNEL_SHADOWMANAGEMENT = "shadowManagement";
    public static final String CHANNEL_EXTERNALMODULECONTROL = "externalModuleControl";

    public static final String CHANNEL_BATTERYUSAGECONSUMPTIONSET = "batteryUsageConsumptionSet";
    public static final String CHANNEL_BATTERYUSAGESTRATEGYSET = "batteryUsageStrategySet";
    public static final String CHANNEL_SMARTBATTERYCONTROLSET = "smartBatteryControlSet";
    public static final String CHANNEL_BATTERYCHARGETIMEFROMSET = "batteryChargeTimeFromSet";
    public static final String CHANNEL_BATTERYCHARGETIMETOSET = "batteryChargeTimeToSet";
    public static final String CHANNEL_MAXDEPTHOFDISCHARGESET = "maxDepthOfDischargeSet";
    public static final String CHANNEL_SHADOWMANAGEMENTSET = "shadowManagementSet";
    public static final String CHANNEL_EXTERNALMODULECONTROLSET = "externalModuleControlSet";
}
