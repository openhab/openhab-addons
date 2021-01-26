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
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.openhab.binding.e3dc.internal.rscp.RSCPData;
import org.openhab.binding.e3dc.internal.rscp.RSCPFrame;
import org.openhab.binding.e3dc.internal.rscp.RSCPFrame.Builder;
import org.openhab.binding.e3dc.internal.rscp.RSCPTag;
import org.openhab.binding.e3dc.internal.rscp.util.ByteUtils;

/**
 * The {@link E3DCRequests} is responsible for creating a sample request.
 *
 * @author Brendon Votteler - Initial Contribution
 * @author Björn Brings - Minor Adjustments
 */
public class E3DCRequests {
    public static byte[] buildAuthenticationMessage(String user, String password) {
        RSCPData authUser = RSCPData.builder().tag(RSCPTag.TAG_RSCP_AUTHENTICATION_USER).stringValue(user).build();

        RSCPData authPwd = RSCPData.builder().tag(RSCPTag.TAG_RSCP_AUTHENTICATION_PASSWORD).stringValue(password)
                .build();

        RSCPData authContainer = RSCPData.builder().tag(RSCPTag.TAG_RSCP_REQ_AUTHENTICATION)
                .containerValues(Arrays.asList(authUser, authPwd)).build();

        RSCPFrame authFrame = RSCPFrame.builder().timestamp(Instant.now()).addData(authContainer).build();

        return authFrame.getAsByteArray();
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

    public static void buildFrame02(Builder buildFrame) {
        buildFrame = buildFrame.addData(RSCPData.builder().tag(RSCPTag.TAG_PVI_REQ_DATA).containerValues(Arrays.asList(
                RSCPData.builder().tag(RSCPTag.TAG_PVI_INDEX).int16Value((short) 0).build(),
                ReqGen(RSCPTag.TAG_PVI_REQ_ON_GRID), ReqGen(RSCPTag.TAG_PVI_REQ_STATE),
                ReqGen(RSCPTag.TAG_PVI_REQ_LAST_ERROR), ReqGen(RSCPTag.TAG_PVI_REQ_IS_FLASHING),
                ReqGen(RSCPTag.TAG_PVI_REQ_SERVICE_PROGRESS_STATE), ReqGen(RSCPTag.TAG_PVI_REQ_DEVICE_STATE),
                ReqGen(RSCPTag.TAG_PVI_REQ_TYPE), ReqGen(RSCPTag.TAG_PVI_REQ_LAND_CODE_LIST),
                ReqGen(RSCPTag.TAG_PVI_REQ_UZK_VOLTAGE), ReqGen(RSCPTag.TAG_PVI_REQ_COS_PHI),
                ReqGen(RSCPTag.TAG_PVI_REQ_VOLTAGE_MONITORING), ReqGen(RSCPTag.TAG_PVI_REQ_FREQUENCY_UNDER_OVER),
                ReqGen(RSCPTag.TAG_PVI_REQ_SYSTEM_MODE), ReqGen(RSCPTag.TAG_PVI_REQ_POWER_MODE),
                ReqGen(RSCPTag.TAG_PVI_REQ_USED_STRING_COUNT), ReqGen(RSCPTag.TAG_PVI_REQ_DERATE_TO_POWER),
                ReqGen(RSCPTag.TAG_PVI_REQ_TEMPERATURE), ReqGen(RSCPTag.TAG_PVI_REQ_TEMPERATURE_COUNT),
                ReqGen(RSCPTag.TAG_PVI_REQ_MAX_TEMPERATURE), ReqGen(RSCPTag.TAG_PVI_REQ_MIN_TEMPERATURE),
                ReqGen(RSCPTag.TAG_PVI_REQ_VERSION), ReqGen(RSCPTag.TAG_PVI_REQ_AC_MAX_PHASE_COUNT),
                ReqGen(RSCPTag.TAG_PVI_REQ_AC_POWER), ReqGen(RSCPTag.TAG_PVI_REQ_AC_VOLTAGE),
                ReqGen(RSCPTag.TAG_PVI_REQ_AC_CURRENT), ReqGen(RSCPTag.TAG_PVI_REQ_AC_APPARENTPOWER),
                ReqGen(RSCPTag.TAG_PVI_REQ_AC_REACTIVEPOWER), ReqGen(RSCPTag.TAG_PVI_REQ_AC_ENERGY_ALL),
                ReqGen(RSCPTag.TAG_PVI_REQ_AC_MAX_APPARENTPOWER), ReqGen(RSCPTag.TAG_PVI_REQ_AC_ENERGY_DAY),
                ReqGen(RSCPTag.TAG_PVI_REQ_AC_ENERGY_GRID_CONSUMPTION), ReqGen(RSCPTag.TAG_PVI_REQ_DC_MAX_STRING_COUNT),
                ReqGeni(RSCPTag.TAG_PVI_REQ_DC_POWER, (short) 0), ReqGeni(RSCPTag.TAG_PVI_REQ_DC_POWER, (short) 1),
                ReqGeni(RSCPTag.TAG_PVI_REQ_DC_VOLTAGE, (short) 0), ReqGeni(RSCPTag.TAG_PVI_REQ_DC_VOLTAGE, (short) 1),
                ReqGeni(RSCPTag.TAG_PVI_REQ_DC_CURRENT, (short) 0), ReqGeni(RSCPTag.TAG_PVI_REQ_DC_CURRENT, (short) 1),
                ReqGeni(RSCPTag.TAG_PVI_REQ_DC_MAX_POWER, (short) 0),
                ReqGeni(RSCPTag.TAG_PVI_REQ_DC_MAX_POWER, (short) 1),
                ReqGeni(RSCPTag.TAG_PVI_REQ_DC_MAX_VOLTAGE, (short) 0),
                ReqGeni(RSCPTag.TAG_PVI_REQ_DC_MAX_VOLTAGE, (short) 1),
                ReqGeni(RSCPTag.TAG_PVI_REQ_DC_MIN_VOLTAGE, (short) 0),
                ReqGeni(RSCPTag.TAG_PVI_REQ_DC_MIN_VOLTAGE, (short) 1),
                ReqGeni(RSCPTag.TAG_PVI_REQ_DC_MAX_CURRENT, (short) 0),
                ReqGeni(RSCPTag.TAG_PVI_REQ_DC_MAX_CURRENT, (short) 1),
                ReqGeni(RSCPTag.TAG_PVI_REQ_DC_MIN_CURRENT, (short) 0),
                ReqGeni(RSCPTag.TAG_PVI_REQ_DC_MIN_CURRENT, (short) 1),
                ReqGeni(RSCPTag.TAG_PVI_REQ_DC_STRING_ENERGY_ALL, (short) 0),
                ReqGeni(RSCPTag.TAG_PVI_REQ_DC_STRING_ENERGY_ALL, (short) 1))).build());
    }

    public static void buildFrame03(Builder buildFrame) {
        buildFrame = buildFrame.addData(RSCPData.builder().tag(RSCPTag.TAG_BAT_REQ_DATA).containerValues(Arrays.asList(
                RSCPData.builder().tag(RSCPTag.TAG_BAT_INDEX).int16Value((short) 0).build(),
                ReqGen(RSCPTag.TAG_BAT_REQ_RSOC), ReqGen(RSCPTag.TAG_BAT_REQ_MODULE_VOLTAGE),
                ReqGen(RSCPTag.TAG_BAT_REQ_CURRENT), ReqGen(RSCPTag.TAG_BAT_REQ_MAX_BAT_VOLTAGE),
                ReqGen(RSCPTag.TAG_BAT_REQ_MAX_CHARGE_CURRENT), ReqGen(RSCPTag.TAG_BAT_REQ_EOD_VOLTAGE),
                ReqGen(RSCPTag.TAG_BAT_REQ_MAX_DISCHARGE_CURRENT), ReqGen(RSCPTag.TAG_BAT_REQ_CHARGE_CYCLES),
                ReqGen(RSCPTag.TAG_BAT_REQ_TERMINAL_VOLTAGE), ReqGen(RSCPTag.TAG_BAT_REQ_STATUS_CODE),
                ReqGen(RSCPTag.TAG_BAT_REQ_ERROR_CODE), ReqGen(RSCPTag.TAG_BAT_REQ_DEVICE_NAME),
                ReqGen(RSCPTag.TAG_BAT_REQ_DCB_COUNT), ReqGen(RSCPTag.TAG_BAT_REQ_RSOC_REAL),
                ReqGen(RSCPTag.TAG_BAT_REQ_ASOC), ReqGen(RSCPTag.TAG_BAT_REQ_FCC), ReqGen(RSCPTag.TAG_BAT_REQ_RC),
                ReqGen(RSCPTag.TAG_BAT_REQ_MAX_DCB_CELL_CURRENT), ReqGen(RSCPTag.TAG_BAT_REQ_FIRMWARE_VERSION),
                ReqGen(RSCPTag.TAG_BAT_REQ_INFO), ReqGen(RSCPTag.TAG_BAT_REQ_TRAINING_MODE),
                ReqGen(RSCPTag.TAG_BAT_REQ_UPDATE_STATUS), ReqGen(RSCPTag.TAG_BAT_REQ_TIME_LAST_RESPONSE),
                ReqGen(RSCPTag.TAG_BAT_REQ_MANUFACTURER_NAME), ReqGen(RSCPTag.TAG_BAT_REQ_USABLE_CAPACITY),
                ReqGen(RSCPTag.TAG_BAT_REQ_USABLE_REMAINING_CAPACITY), ReqGen(RSCPTag.TAG_BAT_REQ_CONTROL_CODE)))
                .build());
        // MultiReqGen(buildFrame, new RSCPTag[] { RSCPTag.TAG_BAT_REQ_DCB_INFO, RSCPTag.TAG_BAT_REQ_DEVICE_STATE });
    }

    public static void buildFrame04(Builder buildFrame) {
        buildFrame = buildFrame.addData(RSCPData.builder().tag(RSCPTag.TAG_DCDC_REQ_DATA)
                .containerValues(Arrays.asList(
                        RSCPData.builder().tag(RSCPTag.TAG_DCDC_INDEX).int16Value((short) 0).build(),
                        ReqGen(RSCPTag.TAG_DCDC_REQ_I_BAT), ReqGen(RSCPTag.TAG_DCDC_REQ_U_BAT),
                        ReqGen(RSCPTag.TAG_DCDC_REQ_P_BAT), ReqGen(RSCPTag.TAG_DCDC_REQ_I_DCL),
                        ReqGen(RSCPTag.TAG_DCDC_REQ_U_DCL), ReqGen(RSCPTag.TAG_DCDC_REQ_P_DCL),
                        ReqGen(RSCPTag.TAG_DCDC_REQ_FIRMWARE_VERSION), ReqGen(RSCPTag.TAG_DCDC_REQ_FPGA_FIRMWARE),
                        ReqGen(RSCPTag.TAG_DCDC_REQ_SERIAL_NUMBER), ReqGen(RSCPTag.TAG_DCDC_REQ_BOARD_VERSION),
                        ReqGen(RSCPTag.TAG_DCDC_REQ_IS_FLASHING), ReqGen(RSCPTag.TAG_DCDC_REQ_STATUS),
                        ReqGen(RSCPTag.TAG_DCDC_REQ_STATUS_AS_STRING), ReqGen(RSCPTag.TAG_DCDC_REQ_DEVICE_STATE)))
                .build());
    }

    public static void buildFrame05(Builder buildFrame) {
        buildFrame = buildFrame
                .addData(RSCPData.builder().tag(RSCPTag.TAG_PM_REQ_DATA)
                        .containerValues(Arrays.asList(
                                RSCPData.builder().tag(RSCPTag.TAG_PM_INDEX).int16Value((short) 0).build(),
                                ReqGen(RSCPTag.TAG_PM_REQ_POWER_L1), ReqGen(RSCPTag.TAG_PM_REQ_POWER_L2),
                                ReqGen(RSCPTag.TAG_PM_REQ_POWER_L3), ReqGen(RSCPTag.TAG_PM_REQ_ACTIVE_PHASES),
                                ReqGen(RSCPTag.TAG_PM_REQ_MODE), ReqGen(RSCPTag.TAG_PM_REQ_ENERGY_L1),
                                ReqGen(RSCPTag.TAG_PM_REQ_ENERGY_L2), ReqGen(RSCPTag.TAG_PM_REQ_ENERGY_L3),
                                ReqGen(RSCPTag.TAG_PM_REQ_DEVICE_ID), ReqGen(RSCPTag.TAG_PM_REQ_ERROR_CODE),
                                ReqGen(RSCPTag.TAG_PM_REQ_GET_PHASE_ELIMINATION), ReqGen(RSCPTag.TAG_PM_REQ_VOLTAGE_L1),
                                ReqGen(RSCPTag.TAG_PM_REQ_VOLTAGE_L2), ReqGen(RSCPTag.TAG_PM_REQ_VOLTAGE_L3),
                                ReqGen(RSCPTag.TAG_PM_REQ_TYPE), ReqGen(RSCPTag.TAG_PM_REQ_COMM_STATE),
                                ReqGen(RSCPTag.TAG_PM_REQ_DEVICE_STATE)))
                        .build());
    }

    public static void buildFrame08(Builder buildFrame) {
        MultiReqGen(buildFrame, new RSCPTag[] { RSCPTag.TAG_SRV_REQ_IS_ONLINE });
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

    public static void buildFrame0E(Builder buildFrame) {
        buildFrame = buildFrame.addData(RSCPData.builder().tag(RSCPTag.TAG_WB_REQ_DATA)
                .containerValues(Arrays.asList(
                        RSCPData.builder().tag(RSCPTag.TAG_WB_INDEX).int16Value((short) 0).build(),
                        ReqGen(RSCPTag.TAG_WB_REQ_ENERGY_ALL), ReqGen(RSCPTag.TAG_WB_REQ_ENERGY_SOLAR),
                        ReqGen(RSCPTag.TAG_WB_REQ_SOC), ReqGen(RSCPTag.TAG_WB_REQ_STATUS),
                        ReqGen(RSCPTag.TAG_WB_REQ_ERROR_CODE), ReqGen(RSCPTag.TAG_WB_REQ_MODE),
                        ReqGen(RSCPTag.TAG_WB_REQ_APP_SOFTWARE), ReqGen(RSCPTag.TAG_WB_REQ_BOOTLOADER_SOFTWARE),
                        ReqGen(RSCPTag.TAG_WB_REQ_HW_VERSION), ReqGen(RSCPTag.TAG_WB_REQ_FLASH_VERSION),
                        ReqGen(RSCPTag.TAG_WB_REQ_DEVICE_ID), ReqGen(RSCPTag.TAG_WB_REQ_DEVICE_STATE),
                        ReqGen(RSCPTag.TAG_WB_REQ_PM_POWER_L1), ReqGen(RSCPTag.TAG_WB_REQ_PM_POWER_L2),
                        ReqGen(RSCPTag.TAG_WB_REQ_PM_POWER_L3), ReqGen(RSCPTag.TAG_WB_REQ_PM_ACTIVE_PHASES),
                        ReqGen(RSCPTag.TAG_WB_REQ_PM_MODE), ReqGen(RSCPTag.TAG_WB_REQ_PM_ENERGY_L1),
                        ReqGen(RSCPTag.TAG_WB_REQ_PM_ENERGY_L2), ReqGen(RSCPTag.TAG_WB_REQ_PM_ENERGY_L3),
                        ReqGen(RSCPTag.TAG_WB_REQ_PM_DEVICE_ID), ReqGen(RSCPTag.TAG_WB_REQ_PM_ERROR_CODE),
                        ReqGen(RSCPTag.TAG_WB_REQ_PM_DEVICE_STATE), ReqGen(RSCPTag.TAG_WB_REQ_PM_FIRMWARE_VERSION),
                        ReqGen(RSCPTag.TAG_WB_REQ_DIAG_DEVICE_ID), ReqGen(RSCPTag.TAG_WB_REQ_DIAG_BAT_CAPACITY),
                        ReqGen(RSCPTag.TAG_WB_REQ_DIAG_USER_PARAM), ReqGen(RSCPTag.TAG_WB_REQ_DIAG_MAX_CURRENT),
                        ReqGen(RSCPTag.TAG_WB_REQ_DIAG_PHASE_VOLTAGE), ReqGen(RSCPTag.TAG_WB_REQ_DIAG_DISPLAY_SPEECH),
                        ReqGen(RSCPTag.TAG_WB_REQ_DIAG_DESIGN), ReqGen(RSCPTag.TAG_WB_REQ_DIAG_INFOS),
                        ReqGen(RSCPTag.TAG_WB_REQ_DIAG_WARNINGS), ReqGen(RSCPTag.TAG_WB_REQ_DIAG_ERRORS),
                        ReqGen(RSCPTag.TAG_WB_REQ_DIAG_TEMP_1), ReqGen(RSCPTag.TAG_WB_REQ_DIAG_TEMP_2),
                        ReqGen(RSCPTag.TAG_WB_REQ_DIAG_CP_PEGEL), ReqGen(RSCPTag.TAG_WB_REQ_DIAG_PP_IN_A),
                        ReqGen(RSCPTag.TAG_WB_REQ_DIAG_STATUS_DIODE), ReqGen(RSCPTag.TAG_WB_REQ_DIAG_DIG_IN_1),
                        ReqGen(RSCPTag.TAG_WB_REQ_DIAG_DIG_IN_2)))
                .build());
    }

    public static byte[] buildRequestFrame() {
        Builder buildFrame = RSCPFrame.builder().timestamp(Instant.now());

        // Full request:
        // buildFrame01(buildFrame); // "01" - Requests - Basis details
        // buildFrame02(buildFrame); // "02" - Requests
        // buildFrame03(buildFrame); // "03" - Requests - Battery
        // buildFrame04(buildFrame); // "04" - Request
        // buildFrame05(buildFrame); // "05" - Request
        // buildFrame08(buildFrame); // "08" - Requests - Server Online
        // buildFrame09(buildFrame); // "09" - Requests - Connected Homeautomation devices
        // buildFrame0A(buildFrame); // "0A" - Requests - E3DC network/status details
        // buildFrame0B(buildFrame); // "0B" - Requests - Switch between Grid/Island
        // buildFrame0E(buildFrame); // "0E" - Request - Wallbox

        MultiReqGen(buildFrame,
                new RSCPTag[] { RSCPTag.TAG_EMS_REQ_POWER_PV, RSCPTag.TAG_EMS_REQ_POWER_BAT,
                        RSCPTag.TAG_EMS_REQ_POWER_HOME, RSCPTag.TAG_EMS_REQ_POWER_GRID, RSCPTag.TAG_EMS_REQ_POWER_ADD,
                        RSCPTag.TAG_EMS_REQ_AUTARKY, RSCPTag.TAG_EMS_REQ_SELF_CONSUMPTION, RSCPTag.TAG_EMS_REQ_BAT_SOC,
                        RSCPTag.TAG_EMS_REQ_GET_POWER_SETTINGS, RSCPTag.TAG_EMS_REQ_EMERGENCY_POWER_STATUS,
                        RSCPTag.TAG_EP_REQ_IS_GRID_CONNECTED, RSCPTag.TAG_INFO_REQ_SW_RELEASE });

        buildFrame = buildFrame
                .addData(RSCPData.builder().tag(RSCPTag.TAG_PM_REQ_DATA)
                        .containerValues(Arrays.asList(
                                RSCPData.builder().tag(RSCPTag.TAG_PM_INDEX).int16Value((short) 0).build(),
                                ReqGen(RSCPTag.TAG_PM_REQ_POWER_L1), ReqGen(RSCPTag.TAG_PM_REQ_POWER_L2),
                                ReqGen(RSCPTag.TAG_PM_REQ_POWER_L3), ReqGen(RSCPTag.TAG_PM_REQ_ACTIVE_PHASES),
                                ReqGen(RSCPTag.TAG_PM_REQ_MODE), ReqGen(RSCPTag.TAG_PM_REQ_ENERGY_L1),
                                ReqGen(RSCPTag.TAG_PM_REQ_ENERGY_L2), ReqGen(RSCPTag.TAG_PM_REQ_ENERGY_L3),
                                ReqGen(RSCPTag.TAG_PM_REQ_VOLTAGE_L1), ReqGen(RSCPTag.TAG_PM_REQ_VOLTAGE_L2),
                                ReqGen(RSCPTag.TAG_PM_REQ_VOLTAGE_L3)))
                        .build());

        RSCPFrame reqFrame1 = buildFrame.build();
        return reqFrame1.getAsByteArray();
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
