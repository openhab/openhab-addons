/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.e3dc.internal.rscp;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * RSCP tag identifiers used by this binding.
 *
 * <p>
 * This is a deliberate subset of the RSCP interface: the data points the binding reads and writes,
 * together with the tags needed for the handshake and for control. Tags that are not listed here are
 * still decoded from a response, they only render as their numeric id in logs.
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public final class RscpTag {

    /**
     * The marker bit that a response tag carries in addition to the request tag of the same data
     * point, for example {@code TAG_EMS_POWER_PV} (0x01800001) against {@code TAG_EMS_REQ_POWER_PV}
     * (0x01000001).
     */
    public static final int RESPONSE_MARKER = 0x00800000;

    public static final int RSCP_REQ_AUTHENTICATION = 0x00000001;
    public static final int RSCP_AUTHENTICATION_USER = 0x00000002;
    public static final int RSCP_AUTHENTICATION_PASSWORD = 0x00000003;
    public static final int RSCP_REQ_SUPPORTED_PROTOCOL_VERSIONS = 0x0000000A;
    public static final int RSCP_AUTHENTICATION = 0x00800001;
    public static final int RSCP_SET_PROTOCOL_VERSION = 0x00800009;
    public static final int RSCP_SUPPORTED_PROTOCOL_VERSIONS = 0x0080000A;
    public static final int RSCP_GENERAL_ERROR = 0x00FFFFFF;
    public static final int EMS_REQ_POWER_PV = 0x01000001;
    public static final int EMS_REQ_POWER_BAT = 0x01000002;
    public static final int EMS_REQ_POWER_HOME = 0x01000003;
    public static final int EMS_REQ_POWER_GRID = 0x01000004;
    public static final int EMS_REQ_POWER_ADD = 0x01000005;
    public static final int EMS_REQ_BAT_SOC = 0x01000008;
    public static final int EMS_REQ_COUPLING_MODE = 0x01000009;
    public static final int EMS_REQ_MODE = 0x01000011;
    public static final int EMS_REQ_BALANCED_PHASES = 0x01000012;
    public static final int EMS_REQ_INSTALLED_PEAK_POWER = 0x01000013;
    public static final int EMS_REQ_DERATE_AT_PERCENT_VALUE = 0x01000014;
    public static final int EMS_REQ_DERATE_AT_POWER_VALUE = 0x01000015;
    public static final int EMS_REQ_POWER_WB_ALL = 0x0100001F;
    public static final int EMS_REQ_POWER_WB_SOLAR = 0x01000020;
    public static final int EMS_REQ_SET_POWER = 0x01000030;
    public static final int EMS_REQ_SET_POWER_MODE = 0x01000031;
    public static final int EMS_REQ_GET_IDLE_PERIODS_ENABLE = 0x01000035;
    public static final int EMS_REQ_STATUS = 0x01000040;
    public static final int EMS_REQ_USED_CHARGE_LIMIT = 0x01000041;
    public static final int EMS_REQ_BAT_CHARGE_LIMIT = 0x01000042;
    public static final int EMS_REQ_DCDC_CHARGE_LIMIT = 0x01000043;
    public static final int EMS_REQ_USER_CHARGE_LIMIT = 0x01000044;
    public static final int EMS_REQ_USED_DISCHARGE_LIMIT = 0x01000045;
    public static final int EMS_REQ_BAT_DISCHARGE_LIMIT = 0x01000046;
    public static final int EMS_REQ_DCDC_DISCHARGE_LIMIT = 0x01000047;
    public static final int EMS_REQ_USER_DISCHARGE_LIMIT = 0x01000048;
    public static final int EMS_REQ_REMAINING_BAT_CHARGE_POWER = 0x01000071;
    public static final int EMS_REQ_REMAINING_BAT_DISCHARGE_POWER = 0x01000072;
    public static final int EMS_REQ_EMERGENCY_POWER_STATUS = 0x01000073;
    public static final int EMS_REQ_BATTERY_TO_CAR_MODE = 0x01000077;
    public static final int EMS_REQ_BATTERY_BEFORE_CAR_MODE = 0x01000079;
    public static final int EMS_REQ_GET_IDLE_PERIODS = 0x01000080;
    public static final int EMS_REQ_SET_IDLE_PERIODS = 0x01000081;
    public static final int EMS_REQ_IDLE_PERIOD_CHANGE_MARKER = 0x0100008A;
    public static final int EMS_REQ_GET_POWER_SETTINGS = 0x0100008B;
    public static final int EMS_REQ_SET_POWER_SETTINGS = 0x0100008C;
    public static final int EMS_REQ_GET_MANUAL_CHARGE = 0x0100008E;
    public static final int EMS_REQ_START_MANUAL_CHARGE = 0x0100008F;
    public static final int EMS_REQ_GET_SYS_SPECS = 0x01000097;
    public static final int EMS_POWER_LIMITS_USED = 0x01000100;
    public static final int EMS_MAX_CHARGE_POWER = 0x01000101;
    public static final int EMS_MAX_DISCHARGE_POWER = 0x01000102;
    public static final int EMS_DISCHARGE_START_POWER = 0x01000103;
    public static final int EMS_POWERSAVE_ENABLED = 0x01000104;
    public static final int EMS_WEATHER_REGULATED_CHARGE_ENABLED = 0x01000105;
    public static final int EMS_REQ_GET_WALLBOX_ENFORCE_POWER_ASSIGNMENT = 0x0100027B;
    public static final int EMS_REQ_GET_WB_DISCHARGE_BAT_UNTIL = 0x0100027D;
    public static final int EMS_POWER_PV = 0x01800001;
    public static final int EMS_POWER_BAT = 0x01800002;
    public static final int EMS_POWER_HOME = 0x01800003;
    public static final int EMS_POWER_GRID = 0x01800004;
    public static final int EMS_POWER_ADD = 0x01800005;
    public static final int EMS_BAT_SOC = 0x01800008;
    public static final int EMS_COUPLING_MODE = 0x01800009;
    public static final int EMS_MODE = 0x01800011;
    public static final int EMS_BALANCED_PHASES = 0x01800012;
    public static final int EMS_INSTALLED_PEAK_POWER = 0x01800013;
    public static final int EMS_DERATE_AT_PERCENT_VALUE = 0x01800014;
    public static final int EMS_DERATE_AT_POWER_VALUE = 0x01800015;
    public static final int EMS_POWER_WB_ALL = 0x0180001F;
    public static final int EMS_POWER_WB_SOLAR = 0x01800020;
    public static final int EMS_SET_POWER = 0x01800030;
    public static final int EMS_GET_IDLE_PERIODS_ENABLE = 0x01800035;
    public static final int EMS_GET_MANUAL_CHARGE = 0x0180008E;
    public static final int EMS_STATUS = 0x01800040;
    public static final int EMS_USED_CHARGE_LIMIT = 0x01800041;
    public static final int EMS_BAT_CHARGE_LIMIT = 0x01800042;
    public static final int EMS_DCDC_CHARGE_LIMIT = 0x01800043;
    public static final int EMS_USER_CHARGE_LIMIT = 0x01800044;
    public static final int EMS_USED_DISCHARGE_LIMIT = 0x01800045;
    public static final int EMS_BAT_DISCHARGE_LIMIT = 0x01800046;
    public static final int EMS_DCDC_DISCHARGE_LIMIT = 0x01800047;
    public static final int EMS_USER_DISCHARGE_LIMIT = 0x01800048;
    public static final int EMS_REMAINING_BAT_CHARGE_POWER = 0x01800071;
    public static final int EMS_REMAINING_BAT_DISCHARGE_POWER = 0x01800072;
    public static final int EMS_EMERGENCY_POWER_STATUS = 0x01800073;
    public static final int EMS_BATTERY_TO_CAR_MODE = 0x01800077;
    public static final int EMS_BATTERY_BEFORE_CAR_MODE = 0x01800079;
    public static final int EMS_IDLE_PERIOD_CHANGE_MARKER = 0x0180008A;
    public static final int EMS_GET_POWER_SETTINGS = 0x0180008B;
    public static final int EMS_SET_POWER_SETTINGS = 0x0180008C;
    public static final int EMS_RES_POWER_LIMITS_USED = 0x01800100;
    public static final int EMS_GET_WALLBOX_ENFORCE_POWER_ASSIGNMENT = 0x0180027B;
    public static final int EMS_GET_WB_DISCHARGE_BAT_UNTIL = 0x0180027D;

    /**
     * The idle periods of the second version, which name the periods and let the client manage the whole
     * list. The vendor header names the response with a {@code RES_} segment while the request is named
     * {@code REQ_GET_}, so both variants are declared.
     */
    public static final int EMS_REQ_GET_IDLE_PERIODS_2 = 0x01000033;
    public static final int EMS_GET_IDLE_PERIODS_2 = 0x01800033;

    /** The container holding one period of the second version inside {@link #EMS_GET_IDLE_PERIODS_2}. */
    public static final int EMS_IDLE_PERIOD_2 = 0x0140027A;

    /** The fields of one period of the second version. */
    public static final int EMS_PERIOD_NAME = 0x01800302;
    public static final int EMS_PERIOD_TYPE = 0x01800083;
    public static final int EMS_PERIOD_WEEKDAYS = 0x01800303;
    public static final int EMS_PERIOD_ACTIVE = 0x01800301;
    public static final int EMS_PERIOD_START = 0x01800304;
    public static final int EMS_PERIOD_STOP = 0x01800305;
    public static final int PVI_REQ_ON_GRID = 0x02000001;
    public static final int PVI_COS_PHI_VALUE = 0x02000062;
    public static final int PVI_COS_PHI_IS_AKTIV = 0x02000063;
    public static final int PVI_COS_PHI_EXCITED = 0x02000064;
    public static final int PVI_VOLTAGE_MONITORING_THRESHOLD_TOP = 0x02000072;
    public static final int PVI_VOLTAGE_MONITORING_THRESHOLD_BOTTOM = 0x02000073;
    public static final int PVI_VOLTAGE_MONITORING_SLOPE_UP = 0x02000074;
    public static final int PVI_VOLTAGE_MONITORING_SLOPE_DOWN = 0x02000075;
    public static final int PVI_FREQUENCY_UNDER = 0x02000082;
    public static final int PVI_FREQUENCY_OVER = 0x02000083;
    public static final int PVI_REQ_USED_STRING_COUNT = 0x02000090;
    public static final int PVI_REQ_TEMPERATURE_COUNT = 0x02000101;
    public static final int PVI_VALUE = 0x02040005;
    public static final int PVI_REQ_DATA = 0x02040000;
    public static final int PVI_DATA = 0x02840000;
    public static final int PVI_REQ_INVERTER_COUNT = 0x020CCCC9;
    public static final int PVI_ON_GRID = 0x02800001;
    public static final int PVI_USED_STRING_COUNT = 0x02800090;
    public static final int PVI_TEMPERATURE = 0x02800100;
    public static final int PVI_TEMPERATURE_COUNT = 0x02800101;
    public static final int PVI_AC_POWER = 0x028AC001;
    public static final int PVI_AC_VOLTAGE = 0x028AC002;
    public static final int PVI_AC_CURRENT = 0x028AC003;
    public static final int PVI_AC_ENERGY_ALL = 0x028AC006;
    public static final int PVI_AC_ENERGY_DAY = 0x028AC008;
    public static final int PVI_AC_FREQUENCY = 0x028AC00A;
    public static final int PVI_INVERTER_COUNT = 0x028CCCC9;
    public static final int PVI_DC_POWER = 0x028DC001;
    public static final int PVI_DC_VOLTAGE = 0x028DC002;
    public static final int PVI_DC_CURRENT = 0x028DC003;
    public static final int BAT_REQ_AVAILABLE_BATTERIES = 0x03000056;
    public static final int BAT_AVAILABLE_BATTERIES = 0x03800057;

    /**
     * The container the battery data arrives in, including the cell module values in
     * {@link #BAT_DCB_INFO}.
     */
    public static final int BAT_REQ_DATA = 0x03040000;
    public static final int PM_INDEX = 0x05040001;
    public static final int PM_POWER_L1 = 0x05800001;
    public static final int PM_POWER_L2 = 0x05800002;
    public static final int PM_POWER_L3 = 0x05800003;
    public static final int PM_ACTIVE_PHASES = 0x05800004;
    public static final int PM_ENERGY_L1 = 0x05800006;
    public static final int PM_ENERGY_L2 = 0x05800007;
    public static final int PM_ENERGY_L3 = 0x05800008;
    public static final int PM_MAX_PHASE_POWER = 0x0580000F;
    public static final int PM_VOLTAGE_L1 = 0x05800011;
    public static final int PM_VOLTAGE_L2 = 0x05800012;
    public static final int PM_VOLTAGE_L3 = 0x05800013;
    public static final int PM_DATA = 0x05840000;
    public static final int PM_REQ_DATA = 0x05040000;
    // Battery cell modules (DCB). Read from the response of the battery data request.

    public static final int BAT_DCB_ALARM = 0x03800308;
    public static final int BAT_DCB_CHARGE_HIGH_TEMPERATURE = 0x03800118;
    public static final int BAT_DCB_CHARGE_LOW_TEMPERATURE = 0x03800117;
    public static final int BAT_DCB_CURRENT = 0x03800111;
    public static final int BAT_DCB_CURRENT_AVG_30S = 0x03800113;
    public static final int BAT_DCB_CYCLE_COUNT = 0x03800110;
    public static final int BAT_DCB_DATA_TABLE_VERSION = 0x03800123;
    public static final int BAT_DCB_DESIGN_CAPACITY = 0x03800115;
    public static final int BAT_DCB_DESIGN_VOLTAGE = 0x03800116;
    public static final int BAT_DCB_DEVICE_NAME = 0x03800303;
    public static final int BAT_DCB_ERROR = 0x03800309;
    public static final int BAT_DCB_FULL_CHARGE_CAPACITY = 0x03800106;
    public static final int BAT_DCB_FW_VERSION = 0x03800122;
    public static final int BAT_DCB_INDEX = 0x03800100;
    public static final int BAT_DCB_INFO = 0x03800042;
    public static final int BAT_DCB_MANUFACTURE_DATE = 0x03800119;
    public static final int BAT_DCB_MANUFACTURE_NAME = 0x03800302;
    public static final int BAT_DCB_MAX_CHARGE_CURRENT = 0x03800103;
    public static final int BAT_DCB_MAX_CHARGE_VOLTAGE = 0x03800102;
    public static final int BAT_DCB_MAX_DISCHARGE_CURRENT = 0x03800105;
    public static final int BAT_DCB_NR_PARALLEL_CELL = 0x03800301;
    public static final int BAT_DCB_NR_SENSOR = 0x03800305;
    public static final int BAT_DCB_NR_SERIES_CELL = 0x03800300;
    public static final int BAT_DCB_PCB_VERSION = 0x03800124;
    public static final int BAT_DCB_PROTOCOL_VERSION = 0x03800121;
    public static final int BAT_DCB_REMAINING_CAPACITY = 0x03800107;
    public static final int BAT_DCB_SERIALCODE = 0x03800304;
    public static final int BAT_DCB_SERIALNO = 0x03800120;
    public static final int BAT_DCB_SOC = 0x03800108;
    public static final int BAT_DCB_SOH = 0x03800109;
    public static final int BAT_DCB_STATUS = 0x03800306;
    public static final int BAT_DCB_VOLTAGE = 0x03800112;
    public static final int BAT_DCB_VOLTAGE_AVG_30S = 0x03800114;
    public static final int BAT_DCB_WARNING = 0x03800307;
    public static final int DB_REQ_HISTORY_DATA_DAY = 0x06000100;
    public static final int DB_REQ_HISTORY_DATA_WEEK = 0x06000200;
    public static final int DB_REQ_HISTORY_DATA_MONTH = 0x06000300;
    public static final int DB_REQ_HISTORY_DATA_YEAR = 0x06000400;
    public static final int DB_REQ_HISTORY_TIME_START = 0x06000101;
    public static final int DB_REQ_HISTORY_TIME_INTERVAL = 0x06000102;
    public static final int DB_REQ_HISTORY_TIME_SPAN = 0x06000103;

    /** The container carrying the summed values of one history period. */
    public static final int DB_SUM_CONTAINER = 0x06800010;

    /** The history response of one day, whose sum container holds the values of that day. */
    public static final int DB_HISTORY_DATA_DAY = 0x06800100;
    public static final int DB_HISTORY_DATA_WEEK = 0x06800200;
    public static final int DB_HISTORY_DATA_MONTH = 0x06800300;
    public static final int DB_HISTORY_DATA_YEAR = 0x06800400;
    public static final int DB_BAT_POWER_IN = 0x06800002;
    public static final int DB_BAT_POWER_OUT = 0x06800003;
    public static final int DB_DC_POWER = 0x06800004;
    public static final int DB_GRID_POWER_IN = 0x06800005;
    public static final int DB_GRID_POWER_OUT = 0x06800006;
    public static final int DB_CONSUMPTION = 0x06800007;
    public static final int DB_PM_0_POWER = 0x06800008;
    public static final int DB_PM_1_POWER = 0x06800009;
    public static final int DB_BAT_CHARGE_LEVEL = 0x0680000A;
    public static final int DB_CONSUMED_PRODUCTION = 0x0680000C;
    public static final int DB_AUTARKY = 0x0680000D;
    public static final int HA_REQ_ACTUATOR_STATES = 0x09000010;
    public static final int INFO_REQ_SERIAL_NUMBER = 0x0A000001;
    public static final int INFO_REQ_PRODUCTION_DATE = 0x0A000002;
    public static final int INFO_REQ_TIME_ZONE = 0x0A000010;
    public static final int INFO_REQ_SW_RELEASE = 0x0A000019;
    public static final int INFO_SERIAL_NUMBER = 0x0A800001;
    public static final int INFO_PRODUCTION_DATE = 0x0A800002;
    public static final int INFO_TIME_ZONE = 0x0A800010;
    public static final int INFO_SW_RELEASE = 0x0A800019;
    public static final int WB_REQ_DATA = 0x0E040000;
    public static final int WB_INDEX = 0x0E040001;
    public static final int WB_REQ_SET_EXTERN = 0x0E041010;
    public static final int WB_REQ_EXTERN_DATA_SUN = 0x0E041011;
    public static final int WB_REQ_EXTERN_DATA_NET = 0x0E041012;
    public static final int WB_REQ_EXTERN_DATA_ALL = 0x0E041013;
    public static final int WB_REQ_SET_PARAM_1 = 0x0E041018;
    public static final int WB_REQ_SET_PARAM_2 = 0x0E041019;
    public static final int WB_EXTERN_DATA = 0x0E042010;
    public static final int WB_EXTERN_DATA_LEN = 0x0E042011;
    public static final int WB_ENERGY_ALL = 0x0E800001;
    public static final int WB_ENERGY_SOLAR = 0x0E800002;
    public static final int WB_SOC = 0x0E800003;
    public static final int WB_PM_POWER_L1 = 0x0E80000C;
    public static final int WB_PM_POWER_L2 = 0x0E80000D;
    public static final int WB_PM_POWER_L3 = 0x0E80000E;
    public static final int WB_DATA = 0x0E840000;
    public static final int WB_NUMBER_PHASES = 0x0E84103B;
    public static final int WB_MAX_CHARGE_CURRENT = 0x0E841047;
    public static final int WB_MIN_CHARGE_CURRENT = 0x0E841048;
    public static final int WB_KEY_STATE = 0x0E841050;
    public static final int SE_PARAM_EP_RESERVE = 0x1B040023;
    public static final int SE_PARAM_LAST_SOC = 0x1B040026;
    public static final int SE_PARAM_EP_RESERVE_W = 0x1B040033;
    public static final int SE_PARAM_EP_RESERVE_MAX_W = 0x1B040034;

    /**
     * The container carrying the reserve kept for emergency power.
     *
     * <p>
     * The interface uses one id for the request and the response of this data point, so this tag is
     * both the container to ask for and the container the values arrive in.
     */
    public static final int SE_REQ_EP_RESERVE = 0x1B000009;

    /** Writing the reserve as a share or as an amount of energy. */
    public static final int SE_REQ_SET_EP_RESERVE = 0x1B000010;

    private static final Map<Integer, String> NAMES = new HashMap<>();

    static {
        NAMES.put(RSCP_REQ_AUTHENTICATION, "RSCP_REQ_AUTHENTICATION");
        NAMES.put(RSCP_AUTHENTICATION_USER, "RSCP_AUTHENTICATION_USER");
        NAMES.put(RSCP_AUTHENTICATION_PASSWORD, "RSCP_AUTHENTICATION_PASSWORD");
        NAMES.put(RSCP_REQ_SUPPORTED_PROTOCOL_VERSIONS, "RSCP_REQ_SUPPORTED_PROTOCOL_VERSIONS");
        NAMES.put(RSCP_AUTHENTICATION, "RSCP_AUTHENTICATION");
        NAMES.put(RSCP_SET_PROTOCOL_VERSION, "RSCP_SET_PROTOCOL_VERSION");
        NAMES.put(RSCP_SUPPORTED_PROTOCOL_VERSIONS, "RSCP_SUPPORTED_PROTOCOL_VERSIONS");
        NAMES.put(RSCP_GENERAL_ERROR, "RSCP_GENERAL_ERROR");
        NAMES.put(EMS_REQ_POWER_PV, "EMS_REQ_POWER_PV");
        NAMES.put(EMS_REQ_POWER_BAT, "EMS_REQ_POWER_BAT");
        NAMES.put(EMS_REQ_POWER_HOME, "EMS_REQ_POWER_HOME");
        NAMES.put(EMS_REQ_POWER_GRID, "EMS_REQ_POWER_GRID");
        NAMES.put(EMS_REQ_POWER_ADD, "EMS_REQ_POWER_ADD");
        NAMES.put(EMS_REQ_BAT_SOC, "EMS_REQ_BAT_SOC");
        NAMES.put(EMS_REQ_COUPLING_MODE, "EMS_REQ_COUPLING_MODE");
        NAMES.put(EMS_REQ_MODE, "EMS_REQ_MODE");
        NAMES.put(EMS_REQ_BALANCED_PHASES, "EMS_REQ_BALANCED_PHASES");
        NAMES.put(EMS_REQ_INSTALLED_PEAK_POWER, "EMS_REQ_INSTALLED_PEAK_POWER");
        NAMES.put(EMS_REQ_DERATE_AT_PERCENT_VALUE, "EMS_REQ_DERATE_AT_PERCENT_VALUE");
        NAMES.put(EMS_REQ_DERATE_AT_POWER_VALUE, "EMS_REQ_DERATE_AT_POWER_VALUE");
        NAMES.put(EMS_REQ_POWER_WB_ALL, "EMS_REQ_POWER_WB_ALL");
        NAMES.put(EMS_REQ_POWER_WB_SOLAR, "EMS_REQ_POWER_WB_SOLAR");
        NAMES.put(EMS_REQ_SET_POWER, "EMS_REQ_SET_POWER");
        NAMES.put(EMS_REQ_SET_POWER_MODE, "EMS_REQ_SET_POWER_MODE");
        NAMES.put(EMS_REQ_GET_IDLE_PERIODS_ENABLE, "EMS_REQ_GET_IDLE_PERIODS_ENABLE");
        NAMES.put(EMS_REQ_STATUS, "EMS_REQ_STATUS");
        NAMES.put(EMS_REQ_USED_CHARGE_LIMIT, "EMS_REQ_USED_CHARGE_LIMIT");
        NAMES.put(EMS_REQ_BAT_CHARGE_LIMIT, "EMS_REQ_BAT_CHARGE_LIMIT");
        NAMES.put(EMS_REQ_DCDC_CHARGE_LIMIT, "EMS_REQ_DCDC_CHARGE_LIMIT");
        NAMES.put(EMS_REQ_USER_CHARGE_LIMIT, "EMS_REQ_USER_CHARGE_LIMIT");
        NAMES.put(EMS_REQ_USED_DISCHARGE_LIMIT, "EMS_REQ_USED_DISCHARGE_LIMIT");
        NAMES.put(EMS_REQ_BAT_DISCHARGE_LIMIT, "EMS_REQ_BAT_DISCHARGE_LIMIT");
        NAMES.put(EMS_REQ_DCDC_DISCHARGE_LIMIT, "EMS_REQ_DCDC_DISCHARGE_LIMIT");
        NAMES.put(EMS_REQ_USER_DISCHARGE_LIMIT, "EMS_REQ_USER_DISCHARGE_LIMIT");
        NAMES.put(EMS_REQ_REMAINING_BAT_CHARGE_POWER, "EMS_REQ_REMAINING_BAT_CHARGE_POWER");
        NAMES.put(EMS_REQ_REMAINING_BAT_DISCHARGE_POWER, "EMS_REQ_REMAINING_BAT_DISCHARGE_POWER");
        NAMES.put(EMS_REQ_EMERGENCY_POWER_STATUS, "EMS_REQ_EMERGENCY_POWER_STATUS");
        NAMES.put(EMS_REQ_BATTERY_TO_CAR_MODE, "EMS_REQ_BATTERY_TO_CAR_MODE");
        NAMES.put(EMS_REQ_BATTERY_BEFORE_CAR_MODE, "EMS_REQ_BATTERY_BEFORE_CAR_MODE");
        NAMES.put(EMS_REQ_GET_IDLE_PERIODS, "EMS_REQ_GET_IDLE_PERIODS");
        NAMES.put(EMS_REQ_SET_IDLE_PERIODS, "EMS_REQ_SET_IDLE_PERIODS");
        NAMES.put(EMS_REQ_IDLE_PERIOD_CHANGE_MARKER, "EMS_REQ_IDLE_PERIOD_CHANGE_MARKER");
        NAMES.put(EMS_REQ_GET_POWER_SETTINGS, "EMS_REQ_GET_POWER_SETTINGS");
        NAMES.put(EMS_REQ_SET_POWER_SETTINGS, "EMS_REQ_SET_POWER_SETTINGS");
        NAMES.put(EMS_REQ_GET_MANUAL_CHARGE, "EMS_REQ_GET_MANUAL_CHARGE");
        NAMES.put(EMS_REQ_START_MANUAL_CHARGE, "EMS_REQ_START_MANUAL_CHARGE");
        NAMES.put(EMS_REQ_GET_SYS_SPECS, "EMS_REQ_GET_SYS_SPECS");
        NAMES.put(EMS_POWER_LIMITS_USED, "EMS_POWER_LIMITS_USED");
        NAMES.put(EMS_MAX_CHARGE_POWER, "EMS_MAX_CHARGE_POWER");
        NAMES.put(EMS_MAX_DISCHARGE_POWER, "EMS_MAX_DISCHARGE_POWER");
        NAMES.put(EMS_DISCHARGE_START_POWER, "EMS_DISCHARGE_START_POWER");
        NAMES.put(EMS_POWERSAVE_ENABLED, "EMS_POWERSAVE_ENABLED");
        NAMES.put(EMS_WEATHER_REGULATED_CHARGE_ENABLED, "EMS_WEATHER_REGULATED_CHARGE_ENABLED");
        NAMES.put(EMS_REQ_GET_WALLBOX_ENFORCE_POWER_ASSIGNMENT, "EMS_REQ_GET_WALLBOX_ENFORCE_POWER_ASSIGNMENT");
        NAMES.put(EMS_REQ_GET_WB_DISCHARGE_BAT_UNTIL, "EMS_REQ_GET_WB_DISCHARGE_BAT_UNTIL");
        NAMES.put(EMS_POWER_PV, "EMS_POWER_PV");
        NAMES.put(EMS_POWER_BAT, "EMS_POWER_BAT");
        NAMES.put(EMS_POWER_HOME, "EMS_POWER_HOME");
        NAMES.put(EMS_POWER_GRID, "EMS_POWER_GRID");
        NAMES.put(EMS_POWER_ADD, "EMS_POWER_ADD");
        NAMES.put(EMS_BAT_SOC, "EMS_BAT_SOC");
        NAMES.put(EMS_COUPLING_MODE, "EMS_COUPLING_MODE");
        NAMES.put(EMS_MODE, "EMS_MODE");
        NAMES.put(EMS_BALANCED_PHASES, "EMS_BALANCED_PHASES");
        NAMES.put(EMS_INSTALLED_PEAK_POWER, "EMS_INSTALLED_PEAK_POWER");
        NAMES.put(EMS_DERATE_AT_PERCENT_VALUE, "EMS_DERATE_AT_PERCENT_VALUE");
        NAMES.put(EMS_DERATE_AT_POWER_VALUE, "EMS_DERATE_AT_POWER_VALUE");
        NAMES.put(EMS_POWER_WB_ALL, "EMS_POWER_WB_ALL");
        NAMES.put(EMS_POWER_WB_SOLAR, "EMS_POWER_WB_SOLAR");
        NAMES.put(EMS_SET_POWER, "EMS_SET_POWER");
        NAMES.put(EMS_GET_IDLE_PERIODS_ENABLE, "EMS_GET_IDLE_PERIODS_ENABLE");
        NAMES.put(EMS_GET_MANUAL_CHARGE, "EMS_GET_MANUAL_CHARGE");
        NAMES.put(EMS_STATUS, "EMS_STATUS");
        NAMES.put(EMS_USED_CHARGE_LIMIT, "EMS_USED_CHARGE_LIMIT");
        NAMES.put(EMS_BAT_CHARGE_LIMIT, "EMS_BAT_CHARGE_LIMIT");
        NAMES.put(EMS_DCDC_CHARGE_LIMIT, "EMS_DCDC_CHARGE_LIMIT");
        NAMES.put(EMS_USER_CHARGE_LIMIT, "EMS_USER_CHARGE_LIMIT");
        NAMES.put(EMS_USED_DISCHARGE_LIMIT, "EMS_USED_DISCHARGE_LIMIT");
        NAMES.put(EMS_BAT_DISCHARGE_LIMIT, "EMS_BAT_DISCHARGE_LIMIT");
        NAMES.put(EMS_DCDC_DISCHARGE_LIMIT, "EMS_DCDC_DISCHARGE_LIMIT");
        NAMES.put(EMS_USER_DISCHARGE_LIMIT, "EMS_USER_DISCHARGE_LIMIT");
        NAMES.put(EMS_REMAINING_BAT_CHARGE_POWER, "EMS_REMAINING_BAT_CHARGE_POWER");
        NAMES.put(EMS_REMAINING_BAT_DISCHARGE_POWER, "EMS_REMAINING_BAT_DISCHARGE_POWER");
        NAMES.put(EMS_EMERGENCY_POWER_STATUS, "EMS_EMERGENCY_POWER_STATUS");
        NAMES.put(EMS_BATTERY_TO_CAR_MODE, "EMS_BATTERY_TO_CAR_MODE");
        NAMES.put(EMS_BATTERY_BEFORE_CAR_MODE, "EMS_BATTERY_BEFORE_CAR_MODE");
        NAMES.put(EMS_IDLE_PERIOD_CHANGE_MARKER, "EMS_IDLE_PERIOD_CHANGE_MARKER");
        NAMES.put(EMS_GET_POWER_SETTINGS, "EMS_GET_POWER_SETTINGS");
        NAMES.put(EMS_SET_POWER_SETTINGS, "EMS_SET_POWER_SETTINGS");
        NAMES.put(EMS_RES_POWER_LIMITS_USED, "EMS_RES_POWER_LIMITS_USED");
        NAMES.put(EMS_GET_WALLBOX_ENFORCE_POWER_ASSIGNMENT, "EMS_GET_WALLBOX_ENFORCE_POWER_ASSIGNMENT");
        NAMES.put(EMS_GET_WB_DISCHARGE_BAT_UNTIL, "EMS_GET_WB_DISCHARGE_BAT_UNTIL");
        NAMES.put(EMS_REQ_GET_IDLE_PERIODS_2, "EMS_REQ_GET_IDLE_PERIODS_2");
        NAMES.put(EMS_GET_IDLE_PERIODS_2, "EMS_GET_IDLE_PERIODS_2");
        NAMES.put(EMS_IDLE_PERIOD_2, "EMS_IDLE_PERIOD_2");
        NAMES.put(EMS_PERIOD_NAME, "EMS_PERIOD_NAME");
        NAMES.put(EMS_PERIOD_TYPE, "EMS_PERIOD_TYPE");
        NAMES.put(EMS_PERIOD_WEEKDAYS, "EMS_PERIOD_WEEKDAYS");
        NAMES.put(EMS_PERIOD_ACTIVE, "EMS_PERIOD_ACTIVE");
        NAMES.put(EMS_PERIOD_START, "EMS_PERIOD_START");
        NAMES.put(EMS_PERIOD_STOP, "EMS_PERIOD_STOP");
        NAMES.put(PVI_REQ_ON_GRID, "PVI_REQ_ON_GRID");
        NAMES.put(PVI_COS_PHI_VALUE, "PVI_COS_PHI_VALUE");
        NAMES.put(PVI_COS_PHI_IS_AKTIV, "PVI_COS_PHI_IS_AKTIV");
        NAMES.put(PVI_COS_PHI_EXCITED, "PVI_COS_PHI_EXCITED");
        NAMES.put(PVI_VOLTAGE_MONITORING_THRESHOLD_TOP, "PVI_VOLTAGE_MONITORING_THRESHOLD_TOP");
        NAMES.put(PVI_VOLTAGE_MONITORING_THRESHOLD_BOTTOM, "PVI_VOLTAGE_MONITORING_THRESHOLD_BOTTOM");
        NAMES.put(PVI_VOLTAGE_MONITORING_SLOPE_UP, "PVI_VOLTAGE_MONITORING_SLOPE_UP");
        NAMES.put(PVI_VOLTAGE_MONITORING_SLOPE_DOWN, "PVI_VOLTAGE_MONITORING_SLOPE_DOWN");
        NAMES.put(PVI_FREQUENCY_UNDER, "PVI_FREQUENCY_UNDER");
        NAMES.put(PVI_FREQUENCY_OVER, "PVI_FREQUENCY_OVER");
        NAMES.put(PVI_REQ_USED_STRING_COUNT, "PVI_REQ_USED_STRING_COUNT");
        NAMES.put(PVI_REQ_TEMPERATURE_COUNT, "PVI_REQ_TEMPERATURE_COUNT");
        NAMES.put(PVI_VALUE, "PVI_VALUE");
        NAMES.put(PVI_REQ_DATA, "PVI_REQ_DATA");
        NAMES.put(PVI_DATA, "PVI_DATA");
        NAMES.put(PVI_REQ_INVERTER_COUNT, "PVI_REQ_INVERTER_COUNT");
        NAMES.put(PVI_ON_GRID, "PVI_ON_GRID");
        NAMES.put(PVI_USED_STRING_COUNT, "PVI_USED_STRING_COUNT");
        NAMES.put(PVI_TEMPERATURE, "PVI_TEMPERATURE");
        NAMES.put(PVI_TEMPERATURE_COUNT, "PVI_TEMPERATURE_COUNT");
        NAMES.put(PVI_AC_POWER, "PVI_AC_POWER");
        NAMES.put(PVI_AC_VOLTAGE, "PVI_AC_VOLTAGE");
        NAMES.put(PVI_AC_CURRENT, "PVI_AC_CURRENT");
        NAMES.put(PVI_AC_ENERGY_ALL, "PVI_AC_ENERGY_ALL");
        NAMES.put(PVI_AC_ENERGY_DAY, "PVI_AC_ENERGY_DAY");
        NAMES.put(PVI_AC_FREQUENCY, "PVI_AC_FREQUENCY");
        NAMES.put(PVI_INVERTER_COUNT, "PVI_INVERTER_COUNT");
        NAMES.put(PVI_DC_POWER, "PVI_DC_POWER");
        NAMES.put(PVI_DC_VOLTAGE, "PVI_DC_VOLTAGE");
        NAMES.put(PVI_DC_CURRENT, "PVI_DC_CURRENT");
        NAMES.put(BAT_REQ_AVAILABLE_BATTERIES, "BAT_REQ_AVAILABLE_BATTERIES");
        NAMES.put(BAT_AVAILABLE_BATTERIES, "BAT_AVAILABLE_BATTERIES");
        NAMES.put(BAT_REQ_DATA, "BAT_REQ_DATA");
        NAMES.put(PM_INDEX, "PM_INDEX");
        NAMES.put(PM_POWER_L1, "PM_POWER_L1");
        NAMES.put(PM_POWER_L2, "PM_POWER_L2");
        NAMES.put(PM_POWER_L3, "PM_POWER_L3");
        NAMES.put(PM_ACTIVE_PHASES, "PM_ACTIVE_PHASES");
        NAMES.put(PM_ENERGY_L1, "PM_ENERGY_L1");
        NAMES.put(PM_ENERGY_L2, "PM_ENERGY_L2");
        NAMES.put(PM_ENERGY_L3, "PM_ENERGY_L3");
        NAMES.put(PM_MAX_PHASE_POWER, "PM_MAX_PHASE_POWER");
        NAMES.put(PM_VOLTAGE_L1, "PM_VOLTAGE_L1");
        NAMES.put(PM_VOLTAGE_L2, "PM_VOLTAGE_L2");
        NAMES.put(PM_VOLTAGE_L3, "PM_VOLTAGE_L3");
        NAMES.put(PM_DATA, "PM_DATA");
        NAMES.put(PM_REQ_DATA, "PM_REQ_DATA");
        NAMES.put(BAT_DCB_ALARM, "BAT_DCB_ALARM");
        NAMES.put(BAT_DCB_CHARGE_HIGH_TEMPERATURE, "BAT_DCB_CHARGE_HIGH_TEMPERATURE");
        NAMES.put(BAT_DCB_CHARGE_LOW_TEMPERATURE, "BAT_DCB_CHARGE_LOW_TEMPERATURE");
        NAMES.put(BAT_DCB_CURRENT, "BAT_DCB_CURRENT");
        NAMES.put(BAT_DCB_CURRENT_AVG_30S, "BAT_DCB_CURRENT_AVG_30S");
        NAMES.put(BAT_DCB_CYCLE_COUNT, "BAT_DCB_CYCLE_COUNT");
        NAMES.put(BAT_DCB_DATA_TABLE_VERSION, "BAT_DCB_DATA_TABLE_VERSION");
        NAMES.put(BAT_DCB_DESIGN_CAPACITY, "BAT_DCB_DESIGN_CAPACITY");
        NAMES.put(BAT_DCB_DESIGN_VOLTAGE, "BAT_DCB_DESIGN_VOLTAGE");
        NAMES.put(BAT_DCB_DEVICE_NAME, "BAT_DCB_DEVICE_NAME");
        NAMES.put(BAT_DCB_ERROR, "BAT_DCB_ERROR");
        NAMES.put(BAT_DCB_FULL_CHARGE_CAPACITY, "BAT_DCB_FULL_CHARGE_CAPACITY");
        NAMES.put(BAT_DCB_FW_VERSION, "BAT_DCB_FW_VERSION");
        NAMES.put(BAT_DCB_INDEX, "BAT_DCB_INDEX");
        NAMES.put(BAT_DCB_INFO, "BAT_DCB_INFO");
        NAMES.put(BAT_DCB_MANUFACTURE_DATE, "BAT_DCB_MANUFACTURE_DATE");
        NAMES.put(BAT_DCB_MANUFACTURE_NAME, "BAT_DCB_MANUFACTURE_NAME");
        NAMES.put(BAT_DCB_MAX_CHARGE_CURRENT, "BAT_DCB_MAX_CHARGE_CURRENT");
        NAMES.put(BAT_DCB_MAX_CHARGE_VOLTAGE, "BAT_DCB_MAX_CHARGE_VOLTAGE");
        NAMES.put(BAT_DCB_MAX_DISCHARGE_CURRENT, "BAT_DCB_MAX_DISCHARGE_CURRENT");
        NAMES.put(BAT_DCB_NR_PARALLEL_CELL, "BAT_DCB_NR_PARALLEL_CELL");
        NAMES.put(BAT_DCB_NR_SENSOR, "BAT_DCB_NR_SENSOR");
        NAMES.put(BAT_DCB_NR_SERIES_CELL, "BAT_DCB_NR_SERIES_CELL");
        NAMES.put(BAT_DCB_PCB_VERSION, "BAT_DCB_PCB_VERSION");
        NAMES.put(BAT_DCB_PROTOCOL_VERSION, "BAT_DCB_PROTOCOL_VERSION");
        NAMES.put(BAT_DCB_REMAINING_CAPACITY, "BAT_DCB_REMAINING_CAPACITY");
        NAMES.put(BAT_DCB_SERIALCODE, "BAT_DCB_SERIALCODE");
        NAMES.put(BAT_DCB_SERIALNO, "BAT_DCB_SERIALNO");
        NAMES.put(BAT_DCB_SOC, "BAT_DCB_SOC");
        NAMES.put(BAT_DCB_SOH, "BAT_DCB_SOH");
        NAMES.put(BAT_DCB_STATUS, "BAT_DCB_STATUS");
        NAMES.put(BAT_DCB_VOLTAGE, "BAT_DCB_VOLTAGE");
        NAMES.put(BAT_DCB_VOLTAGE_AVG_30S, "BAT_DCB_VOLTAGE_AVG_30S");
        NAMES.put(BAT_DCB_WARNING, "BAT_DCB_WARNING");
        NAMES.put(DB_REQ_HISTORY_DATA_DAY, "DB_REQ_HISTORY_DATA_DAY");
        NAMES.put(DB_REQ_HISTORY_TIME_START, "DB_REQ_HISTORY_TIME_START");
        NAMES.put(DB_REQ_HISTORY_TIME_INTERVAL, "DB_REQ_HISTORY_TIME_INTERVAL");
        NAMES.put(DB_REQ_HISTORY_TIME_SPAN, "DB_REQ_HISTORY_TIME_SPAN");
        NAMES.put(DB_SUM_CONTAINER, "DB_SUM_CONTAINER");
        NAMES.put(DB_HISTORY_DATA_DAY, "DB_HISTORY_DATA_DAY");
        NAMES.put(DB_HISTORY_DATA_WEEK, "DB_HISTORY_DATA_WEEK");
        NAMES.put(DB_HISTORY_DATA_MONTH, "DB_HISTORY_DATA_MONTH");
        NAMES.put(DB_HISTORY_DATA_YEAR, "DB_HISTORY_DATA_YEAR");
        NAMES.put(DB_REQ_HISTORY_DATA_WEEK, "DB_REQ_HISTORY_DATA_WEEK");
        NAMES.put(DB_REQ_HISTORY_DATA_MONTH, "DB_REQ_HISTORY_DATA_MONTH");
        NAMES.put(DB_REQ_HISTORY_DATA_YEAR, "DB_REQ_HISTORY_DATA_YEAR");
        NAMES.put(DB_BAT_POWER_IN, "DB_BAT_POWER_IN");
        NAMES.put(DB_BAT_POWER_OUT, "DB_BAT_POWER_OUT");
        NAMES.put(DB_DC_POWER, "DB_DC_POWER");
        NAMES.put(DB_GRID_POWER_IN, "DB_GRID_POWER_IN");
        NAMES.put(DB_GRID_POWER_OUT, "DB_GRID_POWER_OUT");
        NAMES.put(DB_CONSUMPTION, "DB_CONSUMPTION");
        NAMES.put(DB_PM_0_POWER, "DB_PM_0_POWER");
        NAMES.put(DB_PM_1_POWER, "DB_PM_1_POWER");
        NAMES.put(DB_BAT_CHARGE_LEVEL, "DB_BAT_CHARGE_LEVEL");
        NAMES.put(DB_CONSUMED_PRODUCTION, "DB_CONSUMED_PRODUCTION");
        NAMES.put(DB_AUTARKY, "DB_AUTARKY");
        NAMES.put(HA_REQ_ACTUATOR_STATES, "HA_REQ_ACTUATOR_STATES");
        NAMES.put(INFO_REQ_SERIAL_NUMBER, "INFO_REQ_SERIAL_NUMBER");
        NAMES.put(INFO_REQ_PRODUCTION_DATE, "INFO_REQ_PRODUCTION_DATE");
        NAMES.put(INFO_REQ_TIME_ZONE, "INFO_REQ_TIME_ZONE");
        NAMES.put(INFO_REQ_SW_RELEASE, "INFO_REQ_SW_RELEASE");
        NAMES.put(INFO_SERIAL_NUMBER, "INFO_SERIAL_NUMBER");
        NAMES.put(INFO_PRODUCTION_DATE, "INFO_PRODUCTION_DATE");
        NAMES.put(INFO_TIME_ZONE, "INFO_TIME_ZONE");
        NAMES.put(INFO_SW_RELEASE, "INFO_SW_RELEASE");
        NAMES.put(WB_REQ_DATA, "WB_REQ_DATA");
        NAMES.put(WB_INDEX, "WB_INDEX");
        NAMES.put(WB_REQ_SET_EXTERN, "WB_REQ_SET_EXTERN");
        NAMES.put(WB_REQ_EXTERN_DATA_SUN, "WB_REQ_EXTERN_DATA_SUN");
        NAMES.put(WB_REQ_EXTERN_DATA_NET, "WB_REQ_EXTERN_DATA_NET");
        NAMES.put(WB_REQ_EXTERN_DATA_ALL, "WB_REQ_EXTERN_DATA_ALL");
        NAMES.put(WB_REQ_SET_PARAM_1, "WB_REQ_SET_PARAM_1");
        NAMES.put(WB_REQ_SET_PARAM_2, "WB_REQ_SET_PARAM_2");
        NAMES.put(WB_EXTERN_DATA, "WB_EXTERN_DATA");
        NAMES.put(WB_EXTERN_DATA_LEN, "WB_EXTERN_DATA_LEN");
        NAMES.put(WB_ENERGY_ALL, "WB_ENERGY_ALL");
        NAMES.put(WB_ENERGY_SOLAR, "WB_ENERGY_SOLAR");
        NAMES.put(WB_SOC, "WB_SOC");
        NAMES.put(WB_PM_POWER_L1, "WB_PM_POWER_L1");
        NAMES.put(WB_PM_POWER_L2, "WB_PM_POWER_L2");
        NAMES.put(WB_PM_POWER_L3, "WB_PM_POWER_L3");
        NAMES.put(WB_DATA, "WB_DATA");
        NAMES.put(WB_NUMBER_PHASES, "WB_NUMBER_PHASES");
        NAMES.put(WB_MAX_CHARGE_CURRENT, "WB_MAX_CHARGE_CURRENT");
        NAMES.put(WB_MIN_CHARGE_CURRENT, "WB_MIN_CHARGE_CURRENT");
        NAMES.put(WB_KEY_STATE, "WB_KEY_STATE");
        NAMES.put(SE_PARAM_EP_RESERVE, "SE_PARAM_EP_RESERVE");
        NAMES.put(SE_PARAM_LAST_SOC, "SE_PARAM_LAST_SOC");
        NAMES.put(SE_PARAM_EP_RESERVE_W, "SE_PARAM_EP_RESERVE_W");
        NAMES.put(SE_PARAM_EP_RESERVE_MAX_W, "SE_PARAM_EP_RESERVE_MAX_W");
        NAMES.put(SE_REQ_EP_RESERVE, "SE_REQ_EP_RESERVE");
        NAMES.put(SE_REQ_SET_EP_RESERVE, "SE_REQ_SET_EP_RESERVE");
    }

    private RscpTag() {
    }

    /** The symbolic name of a tag, or a hex rendering when the tag is not part of this catalogue. */
    public static String nameOf(int tag) {
        String name = NAMES.get(tag);
        return name != null ? name : String.format("UNKNOWN_0x%08X", tag);
    }

    /**
     * The request tag to ask the device for the value that arrives under the given response tag.
     *
     * <p>
     * A channel spec names the tag the device answers with, but a request has to carry the matching
     * {@code *_REQ_*} tag: the device does not recognise a response tag in a request. The catalogue
     * declares the request tags of the values the binding writes, but not one for every value it
     * reads, so the tag is derived by the rule the interface follows: a response tag is the request
     * tag with {@link #RESPONSE_MARKER} set, which is how the vendor header pairs
     * {@code TAG_EMS_POWER_PV} (0x01800001) with {@code TAG_EMS_REQ_POWER_PV} (0x01000001). The same
     * rule holds for containers, for example {@code TAG_PM_DATA} (0x05840000) against
     * {@code TAG_PM_REQ_DATA} (0x05040000).
     *
     * <p>
     * A few data points do not follow the rule. The wallbox external data is the case here: the device
     * names the packed status struct only as a response, and it is requested by asking the wallbox
     * container, so it maps to {@link #WB_REQ_DATA} rather than to a tag of its own.
     *
     * @return the request tag, or {@code 0} when the given tag does not carry the response marker
     */
    public static int requestFor(int responseTag) {
        if (responseTag == WB_EXTERN_DATA) {
            return WB_REQ_DATA;
        }
        if (responseTag == BAT_AVAILABLE_BATTERIES) {
            // this pair is the exception to the marker rule: the request id is not the marked response
            return BAT_REQ_AVAILABLE_BATTERIES;
        }
        if ((responseTag & RESPONSE_MARKER) == 0) {
            // already a request tag, so there is nothing to derive
            return 0;
        }
        return responseTag & ~RESPONSE_MARKER;
    }

    /**
     * The tag to send when asking the device for the given data point.
     *
     * <p>
     * A tag that already names a request is sent as it is. Some data points, the emergency power
     * reserve among them, use the same id for the request and the response, so their channel specs
     * carry the request tag directly instead of a response tag.
     */
    public static int pollTagFor(int tag) {
        int requestTag = requestFor(tag);
        return requestTag != 0 ? requestTag : tag;
    }

    /** True when the given tag is already the request variant of a data point. */
    public static boolean isRequestTag(int tag) {
        String name = NAMES.get(tag);
        return name != null && name.contains("REQ");
    }
}
