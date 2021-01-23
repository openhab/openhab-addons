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

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.openhab.binding.e3dc.internal.rscp.RSCPData;
import org.openhab.binding.e3dc.internal.rscp.RSCPFrame;
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

    /**
     * Builds a sample request frame to request E3DC history data, starting from start epoch secs, for a duration of
     * intervalSeconds each, for a number of intervals.
     *
     * @param startEpochSeconds Epoch seconds as start time to request data for.
     * @param intervalSeconds How many seconds to put in each interval.
     * @param numberOfIntervals How many intervals to request.
     * @return A byte array ready to be encrypted and sent.
     */
    public static byte[] buildRequestFrame() {
        // build parameters

        RSCPData reqTimeStart = RSCPData.builder().tag(RSCPTag.TAG_DB_REQ_HISTORY_TIME_START)
                .timestampValue(
                        Instant.now().atZone(ZoneId.of("CET")).truncatedTo(ChronoUnit.DAYS).minusDays(1).toInstant())
                .build();

        RSCPData reqInterval = RSCPData.builder().tag(RSCPTag.TAG_DB_REQ_HISTORY_TIME_INTERVAL)
                .timestampValue(Duration.ofSeconds(24 * 60 * 60)).build();

        RSCPData reqTimeSpan = RSCPData.builder().tag(RSCPTag.TAG_DB_REQ_HISTORY_TIME_SPAN)
                .timestampValue(Duration.ofSeconds(24 * 60 * 60)).build();

        // build request starting with a container
        RSCPData reqHistContainer = RSCPData.builder().tag(RSCPTag.TAG_DB_REQ_HISTORY_DATA_DAY)
                .containerValues(Arrays.asList(reqTimeStart, reqInterval, reqTimeSpan)).build();

        RSCPData req1 = RSCPData.builder().tag(RSCPTag.TAG_EMS_REQ_POWER_PV).boolValue(true).build();
        RSCPData req2 = RSCPData.builder().tag(RSCPTag.TAG_EMS_REQ_POWER_BAT).boolValue(true).build();
        RSCPData req3 = RSCPData.builder().tag(RSCPTag.TAG_EMS_REQ_POWER_HOME).boolValue(true).build();
        RSCPData req4 = RSCPData.builder().tag(RSCPTag.TAG_EMS_REQ_POWER_GRID).boolValue(true).build();
        RSCPData req5 = RSCPData.builder().tag(RSCPTag.TAG_EMS_REQ_POWER_ADD).boolValue(true).build();
        RSCPData req6 = RSCPData.builder().tag(RSCPTag.TAG_EMS_REQ_EMERGENCY_POWER_STATUS).boolValue(true).build();
        RSCPData req7 = RSCPData.builder().tag(RSCPTag.TAG_EMS_REQ_AUTARKY).boolValue(true).build();
        RSCPData req8 = RSCPData.builder().tag(RSCPTag.TAG_EMS_REQ_BAT_SOC).boolValue(true).build();

        RSCPData reqPM1 = RSCPData.builder().tag(RSCPTag.TAG_PM_INDEX).char8Value((char) 0).build();
        RSCPData reqPM2 = RSCPData.builder().tag(RSCPTag.TAG_PM_REQ_POWER_L1).boolValue(true).build();
        RSCPData reqPM3 = RSCPData.builder().tag(RSCPTag.TAG_PM_REQ_POWER_L2).boolValue(true).build();
        RSCPData reqPM4 = RSCPData.builder().tag(RSCPTag.TAG_PM_REQ_POWER_L3).boolValue(true).build();
        RSCPData reqPM5 = RSCPData.builder().tag(RSCPTag.TAG_PM_REQ_VOLTAGE_L1).boolValue(true).build();
        RSCPData reqPM6 = RSCPData.builder().tag(RSCPTag.TAG_PM_REQ_VOLTAGE_L2).boolValue(true).build();
        RSCPData reqPM7 = RSCPData.builder().tag(RSCPTag.TAG_PM_REQ_VOLTAGE_L3).boolValue(true).build();
        RSCPData reqPM = RSCPData.builder().tag(RSCPTag.TAG_PM_REQ_DATA)
                .containerValues(Arrays.asList(reqPM1, reqPM2, reqPM3, reqPM4, reqPM5, reqPM6, reqPM7)).build();

        RSCPData reqDC1 = RSCPData.builder().tag(RSCPTag.TAG_PVI_INDEX).char8Value((char) 0).build();
        RSCPData reqDC2 = RSCPData.builder().tag(RSCPTag.TAG_PVI_REQ_DC_POWER).char8Value((char) 0).build();
        RSCPData reqDC3 = RSCPData.builder().tag(RSCPTag.TAG_PVI_REQ_DC_VOLTAGE).char8Value((char) 0).build();
        RSCPData reqDC4 = RSCPData.builder().tag(RSCPTag.TAG_PVI_REQ_DC_CURRENT).char8Value((char) 0).build();
        RSCPData reqDC5 = RSCPData.builder().tag(RSCPTag.TAG_PVI_REQ_DC_POWER).char8Value((char) 1).build();
        RSCPData reqDC6 = RSCPData.builder().tag(RSCPTag.TAG_PVI_REQ_DC_VOLTAGE).char8Value((char) 1).build();
        RSCPData reqDC7 = RSCPData.builder().tag(RSCPTag.TAG_PVI_REQ_DC_CURRENT).char8Value((char) 1).build();
        RSCPData reqDC = RSCPData.builder().tag(RSCPTag.TAG_PVI_REQ_DATA)
                .containerValues(Arrays.asList(reqDC1, reqDC2, reqDC3, reqDC4, reqDC5, reqDC6, reqDC7)).build();

        // build frame and append the request container
        RSCPFrame reqFrame1 = RSCPFrame.builder().timestamp(Instant.now()).addData(req1).addData(req2).addData(req3)
                .addData(req4).addData(req5).addData(req6).addData(req7).addData(req8).addData(reqPM).addData(reqDC)
                .addData(reqHistContainer).build();

        return reqFrame1.getAsByteArray();
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
