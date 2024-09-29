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
package org.openhab.binding.fineoffsetweatherstation.internal.service;

import static org.openhab.binding.fineoffsetweatherstation.internal.Utils.toUInt16;
import static org.openhab.binding.fineoffsetweatherstation.internal.Utils.toUInt32;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.fineoffsetweatherstation.internal.Utils;
import org.openhab.binding.fineoffsetweatherstation.internal.domain.ConversionContext;
import org.openhab.binding.fineoffsetweatherstation.internal.domain.DebugDetails;
import org.openhab.binding.fineoffsetweatherstation.internal.domain.Measurand;
import org.openhab.binding.fineoffsetweatherstation.internal.domain.Protocol;
import org.openhab.binding.fineoffsetweatherstation.internal.domain.SensorGatewayBinding;
import org.openhab.binding.fineoffsetweatherstation.internal.domain.response.BatteryStatus;
import org.openhab.binding.fineoffsetweatherstation.internal.domain.response.MeasuredValue;
import org.openhab.binding.fineoffsetweatherstation.internal.domain.response.SensorDevice;
import org.openhab.binding.fineoffsetweatherstation.internal.domain.response.SystemInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class to Convert the protocol data
 *
 * @author Andreas Berger - Initial contribution
 */
@NonNullByDefault
public class FineOffsetDataParser {
    private final Logger logger = LoggerFactory.getLogger(FineOffsetDataParser.class);
    private final Protocol protocol;

    public FineOffsetDataParser(Protocol protocol) {
        this.protocol = protocol;
    }

    public @Nullable String getFirmwareVersion(byte[] data) {
        if (data.length > 0) {
            return new String(data, 5, data[4]);
        }
        return null;
    }

    public Map<SensorGatewayBinding, SensorDevice> getRegisteredSensors(byte[] data,
            Supplier<@Nullable Boolean> isUseWh24) {
        /*
         * Pos | Length | Description
         * -------------------------------------------------
         * 0 | 2 | fixed header (0xffff)
         * 2 | 1 | command (0x3c)
         * 3 | 2 | size
         * -------------------------------------------------
         * (n * 7) + 5 | 1 | index of sensor n
         * (n * 7) + 6 | 4 | id of sensor n
         * (n * 7) + 10 | 1 | battery status of sensor n
         * (n * 7) + 11 | 1 | signal of sensor n
         * -------------------------------------------------
         * (n * 7) + 12 | 1 | checksum
         */

        Map<SensorGatewayBinding, SensorDevice> result = new HashMap<>();
        var len = toUInt16(data, 3);
        int entry = 0;
        int entrySize = 7;
        while (entry * entrySize + 11 <= len) {
            int idx = entry++ * entrySize + 5;
            int id = toUInt32(data, idx + 1);
            List<SensorGatewayBinding> sensorCandidates = SensorGatewayBinding.forIndex(data[idx]);
            if (sensorCandidates == null || sensorCandidates.isEmpty()) {
                logger.debug("unknown sensor (id={}) for index {}", id, data[idx]);
                continue;
            }
            SensorGatewayBinding sensorGatewayBinding = null;
            if (sensorCandidates.size() == 1) {
                sensorGatewayBinding = sensorCandidates.get(0);
            } else if (sensorCandidates.size() == 2 && data[idx] == 0) {
                sensorGatewayBinding = Boolean.TRUE.equals(isUseWh24.get()) ? SensorGatewayBinding.WH24
                        : SensorGatewayBinding.WH65;
            }
            if (sensorGatewayBinding == null) {
                logger.debug("too many sensor candidates for (id={}) and index {}: {}", id, data[idx],
                        sensorCandidates);
                continue;
            }
            switch (id) {
                case 0xFFFFFFFE:
                    logger.trace("sensor {} = disabled", sensorGatewayBinding);
                    continue;
                case 0xFFFFFFFF:
                    logger.trace("sensor {} = registering", sensorGatewayBinding);
                    continue;
            }

            BatteryStatus batteryStatus = sensorGatewayBinding.getBatteryStatus(data[idx + 5]);
            int signal = Utils.toUInt8(data[idx + 6]);

            result.put(sensorGatewayBinding, new SensorDevice(id, sensorGatewayBinding, batteryStatus, signal));
        }
        return result;
    }

    public @Nullable SystemInfo fetchSystemInfo(byte[] data) {
        // expected response
        // 0 - 0xff - header
        // 1 - 0xff - header
        // 2 - 0x30 - system info
        // 3 - 0x?? - size of response
        // 4 - frequency - 0=433, 1=868MHz, 2=915MHz, 3=920MHz
        // 5 - sensor type - 0=WH24, 1=WH65
        // 6-9 - UTC time
        // 10 - time zone index (?)
        // 11 - DST 0-1 - false/true
        // 12 - 0x?? - checksum
        Integer frequency = null;
        switch (data[4]) {
            case 0:
                frequency = 433;
                break;
            case 1:
                frequency = 868;
                break;
            case 2:
                frequency = 915;
                break;
            case 3:
                frequency = 920;
                break;

        }
        boolean useWh24 = data[5] == 0;
        var unix = toUInt32(data, 6);
        var date = LocalDateTime.ofEpochSecond(unix, 0, ZoneOffset.UTC);
        var dst = data[11] != 0;
        return new SystemInfo(frequency, date, dst, useWh24);
    }

    List<MeasuredValue> getMeasuredValues(byte[] data, ConversionContext context, DebugDetails debugDetails) {
        /*
         * Pos| Length | Description
         * -------------------------------------------------
         * 0 | 2 | fixed header (0xffff)
         * 2 | 1 | command (0x27)
         * 3 | 2 | size
         * -------------------------------------------------
         * 5 | 1 | code of item (item defines n)
         * 6 | n | value of item
         * -------------------------------------------------
         * 6 + n | 1 | code of item (item defines m)
         * 7 + n | m | value of item
         * -------------------------------------------------
         * ...
         * -------------------------------------------------
         *
         * | 1 | checksum
         */
        var idx = 5;
        if (protocol == Protocol.ELV) {
            idx++; // at index 5 there is an additional Byte being set to 0x04
            debugDetails.addDebugDetails(5, 1, "ELV extra byte");
        }
        return readMeasuredValues(data, idx, context, protocol.getParserCustomizationType(), debugDetails);
    }

    List<MeasuredValue> getRainData(byte[] data, ConversionContext context, DebugDetails debugDetails) {
        return readMeasuredValues(data, 5, context, Measurand.ParserCustomizationType.RAIN_READING, debugDetails);
    }

    private List<MeasuredValue> readMeasuredValues(byte[] data, int idx, ConversionContext context,
            Measurand.@Nullable ParserCustomizationType protocol, DebugDetails debugDetails) {
        var size = toUInt16(data, 3);

        List<MeasuredValue> result = new ArrayList<>();
        while (idx < size) {
            byte code = data[idx++];
            Measurand.SingleChannelMeasurand measurand = Measurand.getByCode(code);
            if (measurand == null) {
                logger.warn("failed to get measurand 0x{}", Integer.toHexString(code));
                debugDetails.addDebugDetails(idx - 1, 1, "unknown measurand");
                return result;
            } else {
                debugDetails.addDebugDetails(idx - 1, 1, "measurand " + measurand.getDebugString());
            }
            idx += measurand.extractMeasuredValues(data, idx, context, protocol, result, debugDetails);
        }
        return result;
    }
}
