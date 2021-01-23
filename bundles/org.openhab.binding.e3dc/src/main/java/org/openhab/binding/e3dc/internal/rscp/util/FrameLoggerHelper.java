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
package org.openhab.binding.e3dc.internal.rscp.util;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.openhab.binding.e3dc.internal.rscp.RSCPData;
import org.openhab.binding.e3dc.internal.rscp.RSCPDataType;
import org.openhab.binding.e3dc.internal.rscp.RSCPFrame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link FrameLoggerHelper} is responsible for supporting the output of frames
 *
 * @author Brendon Votteler - Initial Contribution
 */
public class FrameLoggerHelper {
    private static final Logger logger = LoggerFactory.getLogger(FrameLoggerHelper.class);
    private static final DateTimeFormatter isoFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME
            .withZone(ZoneId.from(ZoneOffset.UTC));
    private static final String dataPattern = "Data with Tag: %s; Type: %s ; %s";

    public static void logFrame(RSCPFrame frame) {
        if (frame == null) {
            logger.error("Frame is null, nothing logged!");
            return;
        }

        Instant timestamp = frame.getTimestamp();

        logger.warn("Frame with timestamp: {} ; data follows below", isoFormatter.format(timestamp));

        List<RSCPData> dataList = frame.getData();
        for (RSCPData data : dataList) {
            logData(data, 1);
        }
    }

    private static void logData(RSCPData data, int indentation) {
        String value;
        if (data.getDataType() == RSCPDataType.CONTAINER) {
            value = "contained data follows below";
        } else {
            value = data.getValueAsString().map(stringValue -> "Value (as string): " + stringValue).orElseGet(() -> {
                byte[] rawValue = data.getValueAsByteArray();
                return "Raw value (hex): " + ByteUtils.byteArrayToHexString(rawValue);
            });
        }

        StringBuilder sb = new StringBuilder();
        sb.append(StringUtils.leftPad("", indentation, "-")) // prepend '-' character times the indentation
                .append(" ")
                .append(String.format(dataPattern, data.getDataTag().name(), data.getDataType().name(), value));

        String out = sb.toString();
        logger.warn(out);

        if (data.getDataType() == RSCPDataType.CONTAINER) {
            // log data inside
            List<RSCPData> containedDataList = data.getContainerData();
            for (RSCPData containedData : containedDataList) {
                logData(containedData, indentation + 1);
            }
        }
    }
}
