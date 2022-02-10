/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
package org.openhab.binding.e3dc.internal;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.openhab.binding.e3dc.internal.rscp.RSCPData;
import org.openhab.binding.e3dc.internal.rscp.RSCPFrame;
import org.openhab.binding.e3dc.internal.rscp.RSCPFrame.Builder;
import org.openhab.binding.e3dc.internal.rscp.RSCPTag;
import org.openhab.binding.e3dc.internal.rscp.util.ByteUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link E3DCRequests} is responsible for creating a sample request.
 *
 * @author Brendon Votteler - Initial Contribution
 * @author Björn Brings - Minor Adjustments
 * @author Marco Loose - Extensions & Refactorings
 */
public class E3DCRequests {
    private static final Logger logger = LoggerFactory.getLogger(E3DCRequests.class);

    private static final int MAX_PM_COUNT = 8;
    private static final int MAX_WB_COUNT = 8;

    private static int tempCount = 4;
    private static int acCount = 3;
    private static int dcCount = 2;
    private static int pmCount = 1;
    private static int wbCount = 0;
    private static int pviCount = 2;

    public static int getTempCount() {
        return tempCount;
    }

    public static int getAcCount() {
        return acCount;
    }

    public static int getDcCount() {
        return dcCount;
    }

    public static int getPviCount() {
        return pviCount;
    }

    public static int getWbCount() {
        return wbCount;
    }

    public static int getPmCount() {
        return pmCount;
    }

    public static void setTempCount(int value) {
        tempCount = value;
    }

    public static void setAcCount(int value) {
        acCount = value;
    }

    public static void setDcCount(int value) {
        dcCount = value;
    }

    public static void setPmCount(int value) {
        pmCount = value;
    }

    public static void setWbCount(int value) {
        wbCount = value;
    }

    public static void setPviCount(int value) {
        pviCount = value;
    }

    public static byte[] buildAuthenticationMessage(String user, String password) {
        RSCPData authUser = RSCPData.builder().tag(RSCPTag.TAG_RSCP_AUTHENTICATION_USER).stringValue(user).build();

        RSCPData authPwd = RSCPData.builder().tag(RSCPTag.TAG_RSCP_AUTHENTICATION_PASSWORD).stringValue(password)
                .build();

        RSCPData authContainer = RSCPData.builder().tag(RSCPTag.TAG_RSCP_REQ_AUTHENTICATION)
                .containerValues(Arrays.asList(authUser, authPwd)).build();

        RSCPFrame authFrame = RSCPFrame.builder().timestamp(Instant.now()).addData(authContainer).build();

        return authFrame.getAsByteArray();
    }

    public static void buildFrame00(Builder buildFrame) {
        MultiReqGen(buildFrame, new RSCPTag[] { RSCPTag.TAG_RSCP_REQ_USER_LEVEL });
    }

    public static void buildFrame01(Builder buildFrame) {
        MultiReqGen(buildFrame,
                new RSCPTag[] { RSCPTag.TAG_EMS_REQ_POWER_PV, RSCPTag.TAG_EMS_REQ_POWER_BAT,
                        RSCPTag.TAG_EMS_REQ_POWER_HOME, RSCPTag.TAG_EMS_REQ_POWER_GRID, RSCPTag.TAG_EMS_REQ_POWER_ADD,
                        RSCPTag.TAG_EMS_REQ_AUTARKY, RSCPTag.TAG_EMS_REQ_SELF_CONSUMPTION, RSCPTag.TAG_EMS_REQ_BAT_SOC,
                        RSCPTag.TAG_EMS_REQ_COUPLING_MODE, RSCPTag.TAG_EMS_REQ_STORED_ERRORS, RSCPTag.TAG_EMS_REQ_MODE,
                        RSCPTag.TAG_EMS_REQ_BALANCED_PHASES, RSCPTag.TAG_EMS_REQ_INSTALLED_PEAK_POWER,
                        RSCPTag.TAG_EMS_REQ_DERATE_AT_PERCENT_VALUE, RSCPTag.TAG_EMS_REQ_DERATE_AT_POWER_VALUE,
                        RSCPTag.TAG_EMS_REQ_POWER_WB_ALL, RSCPTag.TAG_EMS_REQ_POWER_WB_SOLAR,
                        RSCPTag.TAG_EMS_REQ_EXT_SRC_AVAILABLE });

        MultiReqGen(buildFrame, new RSCPTag[] { RSCPTag.TAG_EMS_REQ_STATUS, RSCPTag.TAG_EMS_REQ_USED_CHARGE_LIMIT,
                RSCPTag.TAG_EMS_REQ_BAT_CHARGE_LIMIT, RSCPTag.TAG_EMS_REQ_DCDC_CHARGE_LIMIT,
                RSCPTag.TAG_EMS_REQ_USER_CHARGE_LIMIT, RSCPTag.TAG_EMS_REQ_USED_DISCHARGE_LIMIT,
                RSCPTag.TAG_EMS_REQ_BAT_DISCHARGE_LIMIT, RSCPTag.TAG_EMS_REQ_DCDC_DISCHARGE_LIMIT,
                RSCPTag.TAG_EMS_REQ_USER_DISCHARGE_LIMIT, RSCPTag.TAG_EMS_REQ_REMAINING_BAT_CHARGE_POWER,
                RSCPTag.TAG_EMS_REQ_REMAINING_BAT_DISCHARGE_POWER, RSCPTag.TAG_EMS_REQ_EMERGENCY_POWER_STATUS });

        MultiReqGen(buildFrame,
                new RSCPTag[] { RSCPTag.TAG_EMS_REQ_BATTERY_TO_CAR_MODE, RSCPTag.TAG_EMS_REQ_BATTERY_BEFORE_CAR_MODE });

        MultiReqGen(buildFrame,
                new RSCPTag[] { RSCPTag.TAG_EMS_REQ_GET_IDLE_PERIODS /* ? */, RSCPTag.TAG_EMS_REQ_GET_POWER_SETTINGS,
                        RSCPTag.TAG_EMS_REQ_GET_MANUAL_CHARGE, RSCPTag.TAG_EMS_REQ_GET_GENERATOR_STATE,
                        RSCPTag.TAG_EMS_REQ_EMERGENCYPOWER_TEST_STATUS });

        MultiReqGen(buildFrame, new RSCPTag[] { RSCPTag.TAG_EMS_REQ_GET_SYS_SPECS, RSCPTag.TAG_EMS_REQ_ALIVE });
    }

    public static void buildFrame01_Test(Builder buildFrame) {
        MultiReqGen(buildFrame, new RSCPTag[] { RSCPTag.TAG_EMS_WEATHER_FORECAST_MODE });
    }

    public static void buildFrame02(Builder buildFrame) {
        // INSTALLER TAG_PVI_REQ_SET_COS_PHI

        for (int pviIndex = 0; pviIndex < pviCount; pviIndex++) {
            logger.trace("PVI request {}/{}", pviIndex + 1, pviCount);

        List<RSCPData> reqList = new ArrayList<RSCPData>();
        reqList.add(RSCPData.builder().tag(RSCPTag.TAG_PVI_INDEX).int16Value((short) pviIndex).build());
        reqList.add(ReqGen(RSCPTag.TAG_PVI_REQ_ON_GRID));
        reqList.add(ReqGen(RSCPTag.TAG_PVI_REQ_STATE));
        reqList.add(ReqGen(RSCPTag.TAG_PVI_REQ_LAST_ERROR));
        reqList.add(ReqGen(RSCPTag.TAG_PVI_REQ_IS_FLASHING));
        reqList.add(ReqGen(RSCPTag.TAG_PVI_REQ_SERVICE_PROGRESS_STATE));
        reqList.add(ReqGen(RSCPTag.TAG_PVI_REQ_DEVICE_STATE));
        reqList.add(ReqGen(RSCPTag.TAG_PVI_REQ_TYPE));
        reqList.add(ReqGen(RSCPTag.TAG_PVI_REQ_LAND_CODE_LIST));
        reqList.add(ReqGen(RSCPTag.TAG_PVI_REQ_UZK_VOLTAGE));
        reqList.add(ReqGen(RSCPTag.TAG_PVI_REQ_COS_PHI));
        reqList.add(ReqGen(RSCPTag.TAG_PVI_REQ_VOLTAGE_MONITORING));
        reqList.add(ReqGen(RSCPTag.TAG_PVI_REQ_FREQUENCY_UNDER_OVER));
        reqList.add(ReqGen(RSCPTag.TAG_PVI_REQ_SYSTEM_MODE));
        reqList.add(ReqGen(RSCPTag.TAG_PVI_REQ_POWER_MODE));
        reqList.add(ReqGen(RSCPTag.TAG_PVI_REQ_USED_STRING_COUNT));
        reqList.add(ReqGen(RSCPTag.TAG_PVI_REQ_DERATE_TO_POWER));
        reqList.add(ReqGen(RSCPTag.TAG_PVI_REQ_TEMPERATURE_COUNT));

        for (int i = 0; i < tempCount; i++) {
            reqList.add(ReqGeni(RSCPTag.TAG_PVI_REQ_TEMPERATURE, (short) i));
            reqList.add(ReqGeni(RSCPTag.TAG_PVI_REQ_MAX_TEMPERATURE, (short) i));
            reqList.add(ReqGeni(RSCPTag.TAG_PVI_REQ_MIN_TEMPERATURE, (short) i));
        }

        reqList.add(ReqGen(RSCPTag.TAG_PVI_REQ_VERSION));
        reqList.add(ReqGen(RSCPTag.TAG_PVI_REQ_AC_MAX_PHASE_COUNT));

        for (int i = 0; i < acCount; i++) {
            reqList.add(ReqGeni(RSCPTag.TAG_PVI_REQ_AC_POWER, (short) i));
            reqList.add(ReqGeni(RSCPTag.TAG_PVI_REQ_AC_VOLTAGE, (short) i));
            reqList.add(ReqGeni(RSCPTag.TAG_PVI_REQ_AC_CURRENT, (short) i));
            reqList.add(ReqGeni(RSCPTag.TAG_PVI_REQ_AC_APPARENTPOWER, (short) i));
            reqList.add(ReqGeni(RSCPTag.TAG_PVI_REQ_AC_REACTIVEPOWER, (short) i));
            reqList.add(ReqGeni(RSCPTag.TAG_PVI_REQ_AC_ENERGY_ALL, (short) i));
            reqList.add(ReqGeni(RSCPTag.TAG_PVI_REQ_AC_MAX_APPARENTPOWER, (short) i));
            reqList.add(ReqGeni(RSCPTag.TAG_PVI_REQ_AC_ENERGY_DAY, (short) i));
            reqList.add(ReqGeni(RSCPTag.TAG_PVI_REQ_AC_ENERGY_GRID_CONSUMPTION, (short) i));
        }

        reqList.add(ReqGen(RSCPTag.TAG_PVI_REQ_DC_MAX_STRING_COUNT));

        for (int i = 0; i < dcCount; i++) {
            reqList.add(ReqGeni(RSCPTag.TAG_PVI_REQ_DC_POWER, (short) i));
            reqList.add(ReqGeni(RSCPTag.TAG_PVI_REQ_DC_VOLTAGE, (short) i));
            reqList.add(ReqGeni(RSCPTag.TAG_PVI_REQ_DC_CURRENT, (short) i));
            reqList.add(ReqGeni(RSCPTag.TAG_PVI_REQ_DC_MAX_POWER, (short) i));
            reqList.add(ReqGeni(RSCPTag.TAG_PVI_REQ_DC_MAX_VOLTAGE, (short) i));
            reqList.add(ReqGeni(RSCPTag.TAG_PVI_REQ_DC_MIN_VOLTAGE, (short) i));
            reqList.add(ReqGeni(RSCPTag.TAG_PVI_REQ_DC_MAX_CURRENT, (short) i));
            reqList.add(ReqGeni(RSCPTag.TAG_PVI_REQ_DC_MIN_CURRENT, (short) i));
            reqList.add(ReqGeni(RSCPTag.TAG_PVI_REQ_DC_STRING_ENERGY_ALL, (short) i));
        }

        buildFrame = buildFrame
                .addData(RSCPData.builder().tag(RSCPTag.TAG_PVI_REQ_DATA).containerValues(reqList).build());
    }
    }

    public static void buildFrame03(Builder buildFrame) {
        List<RSCPData> reqList = new ArrayList<RSCPData>();

        final var maxBAT = 2;

        for (int batIndex = 0; batIndex < maxBAT; batIndex++) {

            logger.trace("BAT request {}/{}", batIndex + 1, maxBAT);

            reqList.add(RSCPData.builder().tag(RSCPTag.TAG_DCDC_INDEX).int16Value((short) batIndex).build());

        reqList.add(ReqGen(RSCPTag.TAG_BAT_REQ_RSOC));
        reqList.add(ReqGen(RSCPTag.TAG_BAT_REQ_MODULE_VOLTAGE));
        reqList.add(ReqGen(RSCPTag.TAG_BAT_REQ_CURRENT));
        reqList.add(ReqGen(RSCPTag.TAG_BAT_REQ_MAX_BAT_VOLTAGE));
        reqList.add(ReqGen(RSCPTag.TAG_BAT_REQ_MAX_CHARGE_CURRENT));
        reqList.add(ReqGen(RSCPTag.TAG_BAT_REQ_EOD_VOLTAGE));
        reqList.add(ReqGen(RSCPTag.TAG_BAT_REQ_MAX_DISCHARGE_CURRENT));
        reqList.add(ReqGen(RSCPTag.TAG_BAT_REQ_CHARGE_CYCLES));
        reqList.add(ReqGen(RSCPTag.TAG_BAT_REQ_TERMINAL_VOLTAGE));
        reqList.add(ReqGen(RSCPTag.TAG_BAT_REQ_STATUS_CODE));
        reqList.add(ReqGen(RSCPTag.TAG_BAT_REQ_ERROR_CODE));
        reqList.add(ReqGen(RSCPTag.TAG_BAT_REQ_DEVICE_NAME));
        reqList.add(ReqGen(RSCPTag.TAG_BAT_REQ_DCB_COUNT));
        reqList.add(ReqGen(RSCPTag.TAG_BAT_REQ_RSOC_REAL));
        reqList.add(ReqGen(RSCPTag.TAG_BAT_REQ_ASOC));
        reqList.add(ReqGen(RSCPTag.TAG_BAT_REQ_FCC));
        reqList.add(ReqGen(RSCPTag.TAG_BAT_REQ_RC));
        reqList.add(ReqGen(RSCPTag.TAG_BAT_REQ_MAX_DCB_CELL_CURRENT));
        reqList.add(ReqGen(RSCPTag.TAG_BAT_REQ_FIRMWARE_VERSION));
        reqList.add(ReqGen(RSCPTag.TAG_BAT_REQ_INFO));
        reqList.add(ReqGen(RSCPTag.TAG_BAT_REQ_TRAINING_MODE));
        reqList.add(ReqGen(RSCPTag.TAG_BAT_REQ_UPDATE_STATUS));
        reqList.add(ReqGen(RSCPTag.TAG_BAT_REQ_TIME_LAST_RESPONSE));
        reqList.add(ReqGen(RSCPTag.TAG_BAT_REQ_MANUFACTURER_NAME));
        reqList.add(ReqGen(RSCPTag.TAG_BAT_REQ_USABLE_CAPACITY));
        reqList.add(ReqGen(RSCPTag.TAG_BAT_REQ_USABLE_REMAINING_CAPACITY));
        reqList.add(ReqGen(RSCPTag.TAG_BAT_REQ_CONTROL_CODE));
        reqList.add(ReqGen(RSCPTag.TAG_BAT_REQ_MAX_DCB_CELL_TEMPERATURE));
        reqList.add(ReqGen(RSCPTag.TAG_BAT_REQ_MIN_DCB_CELL_TEMPERATURE));
        reqList.add(ReqGen(RSCPTag.TAG_BAT_REQ_READY_FOR_SHUTDOWN));

        // TODO
            // MultiReqGen(buildFrame, new RSCPTag[] { RSCPTag.TAG_BAT_REQ_DCB_INFO,
            // RSCPTag.TAG_BAT_REQ_DEVICE_STATE });

        buildFrame = buildFrame
                .addData(RSCPData.builder().tag(RSCPTag.TAG_BAT_REQ_DATA).containerValues(reqList).build());

        }
    }

    public static void buildFrame04(Builder buildFrame) {
        final List<RSCPData> reqList = new ArrayList<RSCPData>();

        final var maxDCDC = 3;

        for (int dcdcIndex = 0; dcdcIndex < maxDCDC; dcdcIndex++) {

            logger.trace("DCDC request {}/{}", dcdcIndex + 1, maxDCDC);

            reqList.add(RSCPData.builder().tag(RSCPTag.TAG_DCDC_INDEX).int16Value((short) dcdcIndex).build());

        reqList.add(ReqGen(RSCPTag.TAG_DCDC_REQ_I_BAT));
        reqList.add(ReqGen(RSCPTag.TAG_DCDC_REQ_U_BAT));
        reqList.add(ReqGen(RSCPTag.TAG_DCDC_REQ_P_BAT));
        reqList.add(ReqGen(RSCPTag.TAG_DCDC_REQ_I_DCL));
        reqList.add(ReqGen(RSCPTag.TAG_DCDC_REQ_U_DCL));
        reqList.add(ReqGen(RSCPTag.TAG_DCDC_REQ_P_DCL));
        reqList.add(ReqGen(RSCPTag.TAG_DCDC_REQ_FIRMWARE_VERSION));
        reqList.add(ReqGen(RSCPTag.TAG_DCDC_REQ_FPGA_FIRMWARE));
        reqList.add(ReqGen(RSCPTag.TAG_DCDC_REQ_SERIAL_NUMBER));
        reqList.add(ReqGen(RSCPTag.TAG_DCDC_REQ_BOARD_VERSION));
        reqList.add(ReqGen(RSCPTag.TAG_DCDC_REQ_IS_FLASHING));
        reqList.add(ReqGen(RSCPTag.TAG_DCDC_REQ_STATUS));
        reqList.add(ReqGen(RSCPTag.TAG_DCDC_REQ_STATUS_AS_STRING));
        reqList.add(ReqGen(RSCPTag.TAG_DCDC_REQ_DEVICE_STATE));

        buildFrame = buildFrame
                .addData(RSCPData.builder().tag(RSCPTag.TAG_DCDC_REQ_DATA).containerValues(reqList).build());
    }
    }

    public static void buildFrame05(Builder buildFrame) {

        if (pmCount <= 0 || pmCount > MAX_PM_COUNT)
            return;

        for (int pmIndex = 0; pmIndex < pmCount; pmIndex++) {
            logger.trace("PM request {}/{}", pmIndex + 1, pmCount);

            final List<RSCPData> reqList = new ArrayList<RSCPData>();

            reqList.add(RSCPData.builder().tag(RSCPTag.TAG_PM_INDEX).int16Value((short) pmIndex).build());

            reqList.add(ReqGen(RSCPTag.TAG_PM_REQ_POWER_L1));
            reqList.add(ReqGen(RSCPTag.TAG_PM_REQ_POWER_L2));
            reqList.add(ReqGen(RSCPTag.TAG_PM_REQ_POWER_L3));
            reqList.add(ReqGen(RSCPTag.TAG_PM_REQ_ACTIVE_PHASES));
            reqList.add(ReqGen(RSCPTag.TAG_PM_REQ_MODE));
            reqList.add(ReqGen(RSCPTag.TAG_PM_REQ_ENERGY_L1));
            reqList.add(ReqGen(RSCPTag.TAG_PM_REQ_ENERGY_L2));
            reqList.add(ReqGen(RSCPTag.TAG_PM_REQ_ENERGY_L3));
            reqList.add(ReqGen(RSCPTag.TAG_PM_REQ_DEVICE_ID));
            reqList.add(ReqGen(RSCPTag.TAG_PM_REQ_ERROR_CODE));
            reqList.add(ReqGen(RSCPTag.TAG_PM_REQ_GET_PHASE_ELIMINATION));
            reqList.add(ReqGen(RSCPTag.TAG_PM_REQ_VOLTAGE_L1));
            reqList.add(ReqGen(RSCPTag.TAG_PM_REQ_VOLTAGE_L2));
            reqList.add(ReqGen(RSCPTag.TAG_PM_REQ_VOLTAGE_L3));
            reqList.add(ReqGen(RSCPTag.TAG_PM_REQ_TYPE));
            reqList.add(ReqGen(RSCPTag.TAG_PM_REQ_COMM_STATE));
            reqList.add(ReqGen(RSCPTag.TAG_PM_REQ_DEVICE_STATE));

        buildFrame = buildFrame
                    .addData(RSCPData.builder().tag(RSCPTag.TAG_PM_REQ_DATA).containerValues(reqList).build());
        }
    }

    public static void buildFrame_DeviceQuery(Builder buildFrame) {

        List<RSCPTag> requestList = new ArrayList<RSCPTag>();
        RSCPTag[] requestArray = new RSCPTag[requestList.size()];
        requestList.add(RSCPTag.TAG_PM_REQ_CONNECTED_DEVICES);
        requestList.add(RSCPTag.TAG_WB_REQ_CONNECTED_DEVICES);
        requestList.add(RSCPTag.TAG_FMS_REQ_CONNECTED_DEVICES);
        requestList.add(RSCPTag.TAG_FMS_REQ_CONNECTED_DEVICES_REV);
        requestList.add(RSCPTag.TAG_PVI_REQ_USED_STRING_COUNT);
        requestList.add(RSCPTag.TAG_QPI_REQ_INVERTER_COUNT);
        requestList.add(RSCPTag.TAG_SE_REQ_SE_COUNT);
        requestArray = requestList.toArray(requestArray);

        MultiReqGen(buildFrame, requestArray);
    }

    public static void buildFrame08(Builder buildFrame) {
        MultiReqGen(buildFrame, new RSCPTag[] { RSCPTag.TAG_SRV_REQ_IS_ONLINE, RSCPTag.TAG_SYS_SCRIPT_FILE_LIST });
    }

    public static void buildFrame1a_Test(Builder buildFrame) {
        MultiReqGen(buildFrame,
                new RSCPTag[] { RSCPTag.TAG_FMS_REQ_CONNECTED_DEVICES, RSCPTag.TAG_FMS_REQ_CONNECTED_DEVICES_REV });
    }

    public static void buildFrame15_Test(Builder buildFrame) {
        MultiReqGen(buildFrame,
                new RSCPTag[] { RSCPTag.TAG_UPNPC_REQ_DEFAULT_LIST, RSCPTag.TAG_UPNPC_REQ_SERVICE_LIST });
    }

    public static void buildFrame1bTest(Builder buildFrame) {
        MultiReqGen(buildFrame,
                new RSCPTag[] { RSCPTag.TAG_QPI_REQ_INVERTER_COUNT, RSCPTag.TAG_QPI_REQ_INVERTER_DATA });
    }

    public static void buildFrame1c_Test(Builder buildFrame) {
        MultiReqGen(buildFrame,
                new RSCPTag[] { RSCPTag.TAG_QPI_REQ_INVERTER_COUNT, RSCPTag.TAG_QPI_REQ_INVERTER_DATA });
    }

    public static void buildFrame22_Test(Builder buildFrame) {
        MultiReqGen(buildFrame, new RSCPTag[] { RSCPTag.TAG_OVP_REQ_STATUS });
    }

    public static void buildFrame09(Builder buildFrame) {
        MultiReqGen(buildFrame,
                new RSCPTag[] { RSCPTag.TAG_HA_REQ_DATAPOINT_LIST, RSCPTag.TAG_HA_REQ_ACTUATOR_STATES });
    }

    public static void buildFrame0A(Builder buildFrame) {
        MultiReqGen(buildFrame,
                new RSCPTag[] { RSCPTag.TAG_INFO_REQ_SERIAL_NUMBER, RSCPTag.TAG_INFO_REQ_PRODUCTION_DATE,
                        RSCPTag.TAG_INFO_REQ_IP_ADDRESS, RSCPTag.TAG_INFO_REQ_SUBNET_MASK,
                        RSCPTag.TAG_INFO_REQ_MAC_ADDRESS, RSCPTag.TAG_INFO_REQ_GATEWAY, RSCPTag.TAG_INFO_REQ_DNS,
                        RSCPTag.TAG_INFO_REQ_DHCP_STATUS, RSCPTag.TAG_INFO_REQ_TIME, RSCPTag.TAG_INFO_REQ_UTC_TIME,
                        RSCPTag.TAG_INFO_REQ_TIME_ZONE, RSCPTag.TAG_INFO_REQ_SW_RELEASE });
    }

    public static void buildFrame0B(Builder buildFrame) {
        MultiReqGen(buildFrame,
                new RSCPTag[] { RSCPTag.TAG_EP_REQ_IS_READY_FOR_SWITCH, RSCPTag.TAG_EP_REQ_IS_GRID_CONNECTED,
                        RSCPTag.TAG_EP_REQ_IS_ISLAND_GRID, RSCPTag.TAG_EP_REQ_IS_INVALID_STATE,
                        RSCPTag.TAG_EP_REQ_IS_POSSIBLE });
    }

    public static void buildFrame0B_Test(Builder buildFrame) {
        MultiReqGen(buildFrame, new RSCPTag[] { RSCPTag.TAG_WB_REQ_CONNECTED_DEVICES });
    }

    public static void buildFrame0C(Builder buildFrame) {
        MultiReqGen(buildFrame, new RSCPTag[] { RSCPTag.TAG_SYS_REQ_IS_SYSTEM_REBOOTING });
    }

    public static void buildFrame0D(Builder buildFrame) {
        MultiReqGen(buildFrame, new RSCPTag[] { RSCPTag.TAG_UM_REQ_UPDATE_STATUS });
    }

    public static void buildFrame13_Test(Builder buildFrame) {
        MultiReqGen(buildFrame,
                new RSCPTag[] { RSCPTag.TAG_MBS_REQ_MODBUS_ENABLED, RSCPTag.TAG_MBS_REQ_MODBUS_CONNECTORS });
    }

    public static void buildFrame13(Builder buildFrame) {
        MultiReqGen(buildFrame, new RSCPTag[] { RSCPTag.TAG_MBS_REQ_MODBUS_ENABLED });
    }

    public static void buildFrame0E(Builder buildFrame) {

        if (wbCount <= 0 || wbCount > MAX_WB_COUNT)
            return;

        for (int wbIndex = 0; wbIndex < wbCount; wbIndex++) {
            logger.trace("WB request {}/{}", wbIndex, wbCount);

            List<RSCPData> reqList = new ArrayList<RSCPData>();

            reqList.add(RSCPData.builder().tag(RSCPTag.TAG_WB_INDEX).int16Value((short) wbIndex).build());

            reqList.add(ReqGen(RSCPTag.TAG_WB_REQ_ENERGY_ALL));
            reqList.add(ReqGen(RSCPTag.TAG_WB_REQ_ENERGY_SOLAR));
            reqList.add(ReqGen(RSCPTag.TAG_WB_REQ_SOC));
            reqList.add(ReqGen(RSCPTag.TAG_WB_REQ_STATUS));
            reqList.add(ReqGen(RSCPTag.TAG_WB_REQ_ERROR_CODE));
            reqList.add(ReqGen(RSCPTag.TAG_WB_REQ_MODE));
            reqList.add(ReqGen(RSCPTag.TAG_WB_REQ_APP_SOFTWARE));
            reqList.add(ReqGen(RSCPTag.TAG_WB_REQ_BOOTLOADER_SOFTWARE));
            reqList.add(ReqGen(RSCPTag.TAG_WB_REQ_HW_VERSION));
            reqList.add(ReqGen(RSCPTag.TAG_WB_REQ_FLASH_VERSION));
            reqList.add(ReqGen(RSCPTag.TAG_WB_REQ_DEVICE_ID));
            reqList.add(ReqGen(RSCPTag.TAG_WB_REQ_DEVICE_STATE));
            reqList.add(ReqGen(RSCPTag.TAG_WB_REQ_PM_POWER_L1));
            reqList.add(ReqGen(RSCPTag.TAG_WB_REQ_PM_POWER_L2));
            reqList.add(ReqGen(RSCPTag.TAG_WB_REQ_PM_POWER_L3));
            reqList.add(ReqGen(RSCPTag.TAG_WB_REQ_PM_ACTIVE_PHASES));
            reqList.add(ReqGen(RSCPTag.TAG_WB_REQ_PM_MODE));
            reqList.add(ReqGen(RSCPTag.TAG_WB_REQ_PM_ENERGY_L1));
            reqList.add(ReqGen(RSCPTag.TAG_WB_REQ_PM_ENERGY_L2));
            reqList.add(ReqGen(RSCPTag.TAG_WB_REQ_PM_ENERGY_L3));
            reqList.add(ReqGen(RSCPTag.TAG_WB_REQ_PM_DEVICE_ID));
            reqList.add(ReqGen(RSCPTag.TAG_WB_REQ_PM_ERROR_CODE));
            reqList.add(ReqGen(RSCPTag.TAG_WB_REQ_PM_DEVICE_STATE));
            reqList.add(ReqGen(RSCPTag.TAG_WB_REQ_PM_FIRMWARE_VERSION));
            reqList.add(ReqGen(RSCPTag.TAG_WB_REQ_DIAG_DEVICE_ID));
            reqList.add(ReqGen(RSCPTag.TAG_WB_REQ_DIAG_BAT_CAPACITY));
            reqList.add(ReqGen(RSCPTag.TAG_WB_REQ_DIAG_USER_PARAM));
            reqList.add(ReqGen(RSCPTag.TAG_WB_REQ_DIAG_MAX_CURRENT));
            reqList.add(ReqGen(RSCPTag.TAG_WB_REQ_DIAG_PHASE_VOLTAGE));
            reqList.add(ReqGen(RSCPTag.TAG_WB_REQ_DIAG_DISPLAY_SPEECH));
            reqList.add(ReqGen(RSCPTag.TAG_WB_REQ_DIAG_DESIGN));
            reqList.add(ReqGen(RSCPTag.TAG_WB_REQ_DIAG_INFOS));
            reqList.add(ReqGen(RSCPTag.TAG_WB_REQ_DIAG_WARNINGS));
            reqList.add(ReqGen(RSCPTag.TAG_WB_REQ_DIAG_ERRORS));
            reqList.add(ReqGen(RSCPTag.TAG_WB_REQ_DIAG_TEMP_1));
            reqList.add(ReqGen(RSCPTag.TAG_WB_REQ_DIAG_TEMP_2));
            reqList.add(ReqGen(RSCPTag.TAG_WB_REQ_DIAG_CP_PEGEL));
            reqList.add(ReqGen(RSCPTag.TAG_WB_REQ_DIAG_PP_IN_A));
            reqList.add(ReqGen(RSCPTag.TAG_WB_REQ_DIAG_STATUS_DIODE));
            reqList.add(ReqGen(RSCPTag.TAG_WB_REQ_DIAG_DIG_IN_1));
            reqList.add(ReqGen(RSCPTag.TAG_WB_REQ_DIAG_DIG_IN_2));

            buildFrame = buildFrame
                    .addData(RSCPData.builder().tag(RSCPTag.TAG_WB_REQ_DATA).containerValues(reqList).build());
        }
    }

    public static byte[] buildRequestDevices() {
        Builder buildFrame = RSCPFrame.builder().timestamp(Instant.now());
        buildFrame_DeviceQuery(buildFrame);
        buildFrame00(buildFrame);

        RSCPFrame reqFrame1 = buildFrame.build();
        return reqFrame1.getAsByteArray();
    }

    // Full request:
    public static byte[] buildRequestFrameDebug(int set) {
        Builder buildFrame = RSCPFrame.builder().timestamp(Instant.now());

        // Full request:
        switch (set) {
            case 0:
                buildFrame01(buildFrame); // "01" - Requests - EMS
        buildFrame02(buildFrame); // "02" - Requests - PV
        buildFrame03(buildFrame); // "03" - Requests - Battery
                break;
            case 1:
        buildFrame04(buildFrame); // "04" - Requests - DCDC
        buildFrame05(buildFrame); // "05" - Requests - Power Meter
                break;
            case 2:
        buildFrame08(buildFrame); // "08" - Requests - Server Online
        buildFrame09(buildFrame); // "09" - Requests - Connected Homeautomation devices
        buildFrame0A(buildFrame); // "0A" - Requests - E3DC network/status details
                buildFrame0C(buildFrame); // "0C" - Requests - System (restart/reboot)
                buildFrame0D(buildFrame); // "0D" - Requests - Update Manager
                break;
            case 3:
        buildFrame0B(buildFrame); // "0B" - Requests - Switch between Grid/Island
        buildFrame0E(buildFrame); // "0E" - Request - Wallbox
                break;
            case 4:
                buildFrame00(buildFrame); // "00" - Requests - RSCP (User Level)
                buildFrame0A(buildFrame); // "0A" - Requests - E3DC network/status details
            case 5:

                buildFrame01(buildFrame); // "01" - Requests - Basis details
                buildFrame05(buildFrame); // "05" - Requests - Power Meter
                buildFrame0A(buildFrame); // "0A" - Requests - E3DC network/status details
                buildFrame0D(buildFrame); // "0D" - Requests - Update Manager
                break;

            case 10:
                buildFrame1a_Test(buildFrame); // "1A" - Requests - Farming system
                buildFrame13_Test(buildFrame); // "13" - Requests - Modbus
                break;
            case 11:
                buildFrame0B_Test(buildFrame); // "0b" - Requests - Wallbox++
                break;
            case 12:
                buildFrame1c_Test(buildFrame); // "1C" - Requests - QPI
                break;
            case 13:
                buildFrame01_Test(buildFrame); // "01" - Requests - EMS++
                break;
            case 14:
                buildFrame22_Test(buildFrame); // "22" - Requests - OVP
                break;
            case 15:
                buildFrame15_Test(buildFrame); // "1C" - Requests - UPNPC
                break;

            default:
                break;
        }

        RSCPFrame reqFrame1 = buildFrame.build();
        return reqFrame1.getAsByteArray();
    }

    public static byte[] buildRequestFrameBase() {
        Builder buildFrame = RSCPFrame.builder().timestamp(Instant.now());

        // MultiReqGen(buildFrame,
        // new RSCPTag[] { RSCPTag.TAG_EMS_REQ_POWER_PV, RSCPTag.TAG_EMS_REQ_POWER_BAT,
        // RSCPTag.TAG_EMS_REQ_POWER_HOME, RSCPTag.TAG_EMS_REQ_POWER_GRID, RSCPTag.TAG_EMS_REQ_POWER_ADD,
        // RSCPTag.TAG_EMS_REQ_AUTARKY, RSCPTag.TAG_EMS_REQ_SELF_CONSUMPTION, RSCPTag.TAG_EMS_REQ_BAT_SOC,
        // RSCPTag.TAG_EMS_REQ_GET_POWER_SETTINGS, RSCPTag.TAG_EMS_REQ_EMERGENCY_POWER_STATUS,
        // RSCPTag.TAG_EP_REQ_IS_GRID_CONNECTED, RSCPTag.TAG_INFO_REQ_SW_RELEASE });

        // buildFrame = buildFrame
        // .addData(RSCPData.builder().tag(RSCPTag.TAG_PM_REQ_DATA)
        // .containerValues(Arrays.asList(
        // RSCPData.builder().tag(RSCPTag.TAG_PM_INDEX).int16Value((short) 0).build(),
        // ReqGen(RSCPTag.TAG_PM_REQ_POWER_L1), ReqGen(RSCPTag.TAG_PM_REQ_POWER_L2),
        // ReqGen(RSCPTag.TAG_PM_REQ_POWER_L3),
        // ReqGen(RSCPTag.TAG_PM_REQ_ACTIVE_PHASES),
        // ReqGen(RSCPTag.TAG_PM_REQ_MODE), ReqGen(RSCPTag.TAG_PM_REQ_ENERGY_L1),
        // ReqGen(RSCPTag.TAG_PM_REQ_ENERGY_L2), ReqGen(RSCPTag.TAG_PM_REQ_ENERGY_L3),
        // ReqGen(RSCPTag.TAG_PM_REQ_DEVICE_ID), ReqGen(RSCPTag.TAG_PM_REQ_ERROR_CODE),
        // ReqGen(RSCPTag.TAG_PM_REQ_FIRMWARE_VERSION),
        // ReqGen(RSCPTag.TAG_PM_REQ_VOLTAGE_L1),
        // ReqGen(RSCPTag.TAG_PM_REQ_VOLTAGE_L2), ReqGen(RSCPTag.TAG_PM_REQ_VOLTAGE_L3),
        // ReqGen(RSCPTag.TAG_PM_REQ_TYPE)))
        // .build());

        buildFrame01(buildFrame);
        buildFrame05(buildFrame);
        buildFrame13(buildFrame);
        buildFrame0A(buildFrame);
        buildFrame0C(buildFrame);
        buildFrame0D(buildFrame);
        buildFrame0E(buildFrame);

        RSCPFrame reqFrame1 = buildFrame.build();
        return reqFrame1.getAsByteArray();
    }

    public static byte[] buildRequestSetFrame(RSCPTag containerTag, RSCPTag tag, Boolean value) {
        Builder buildFrame = RSCPFrame.builder().timestamp(Instant.now()).addData(
                RSCPData.builder().tag(containerTag).containerValues(Arrays.asList(ReqGeni(tag, value))).build());
        return requestFrameFromBuildFrame(buildFrame);
    }

    public static byte[] buildRequestSetFrame(RSCPTag containerTag, RSCPTag tag, char value) {
        Builder buildFrame = RSCPFrame.builder().timestamp(Instant.now()).addData(
                RSCPData.builder().tag(containerTag).containerValues(Arrays.asList(ReqGeni(tag, value))).build());
        return requestFrameFromBuildFrame(buildFrame);
    }

    public static byte[] buildRequestSetFrame(RSCPTag containerTag, RSCPTag tag, int value) {
        Builder buildFrame = RSCPFrame.builder().timestamp(Instant.now()).addData(
                RSCPData.builder().tag(containerTag).containerValues(Arrays.asList(ReqGeni(tag, value))).build());
        return requestFrameFromBuildFrame(buildFrame);
    }

    public static byte[] requestFrameFromBuildFrame(Builder buildFrame) {
        RSCPFrame reqFrame = buildFrame.build();
        return reqFrame.getAsByteArray();
    }

    public static RSCPData ReqGeni(RSCPTag tag, Boolean i) {
        return RSCPData.builder().tag(tag).boolValue(i).build();
    }

    public static RSCPData ReqGeni(RSCPTag tag, int i) {
        return RSCPData.builder().tag(tag).uint32Value(i).build();
    }

    public static RSCPData ReqGeni(RSCPTag tag, char c) {
        return RSCPData.builder().tag(tag).uchar8Value(c).build();
    }

    public static RSCPData ReqGeni(RSCPTag tag, short s) {
        return RSCPData.builder().tag(tag).int16Value(s).build();
    }

    public static RSCPData ReqGen(RSCPTag tag) {
        return RSCPData.builder().tag(tag).noneValue().build();
    }

    public static void MultiReqGen(Builder buildFrame, RSCPTag[] TagsToAdd) {
        for (RSCPTag tag : TagsToAdd) {
            buildFrame = buildFrame.addData(ReqGen(tag));
        }
    }

    public static boolean isAuthenticationRequestReplyFrameComplete(byte[] frame) {
        // need a frame object
        if (frame == null) {
            return false;
        }

        // minimum size 27
        if (frame.length < 27) {
            return false;
        }

        // check byte array starts with "E3 DC" bytes
        if (frame[0] != (byte) 0xe3 || frame[1] != (byte) 0xdc) {
            return false;
        }

        // find location of reply tag "01 00 80 00 03"
        byte[] pattern = ByteUtils.hexStringToByteArray("0100800003");
        int positionStart = ByteUtils.arrayPosition(frame, pattern);

        if (positionStart < 0) {
            return false;
        }

        return true;
    }

    public static short getAuthenticationLevel(byte[] frame) {
        // we've got a reply from an authentication request
        // check the authentication level and return that
        // return -1 if unable to retrieve authentication level

        RSCPFrame rscpFrame = RSCPFrame.builder().buildFromRawBytes(frame);
        List<RSCPData> dataList = rscpFrame.getData();
        // find authentication data in the frame
        Optional<RSCPData> authenticationData = dataList.stream()
                .filter(data -> data.getDataTag() == RSCPTag.TAG_RSCP_AUTHENTICATION).findFirst();

        // read value as short (it is stored as UCHAR8 internally)
        return authenticationData.flatMap(RSCPData::getValueAsShort).orElseGet(() -> (short) -1);
    }
}
